package com.example.core.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipInputStream

object XlsxParser {

    fun parse(inputStream: InputStream): ParsedExcel {
        val bytes = inputStream.readBytes()
        val sharedStrings = extractSharedStrings(ByteArrayInputStream(bytes))
        val sheetNames = extractSheetNames(ByteArrayInputStream(bytes))

        val sheets = mutableListOf<ExcelSheet>()
        val zip = ZipInputStream(ByteArrayInputStream(bytes))
        var entry = zip.nextEntry

        val sheetEntries = mutableMapOf<String, ByteArray>()
        while (entry != null) {
            if (entry.name.startsWith("xl/worksheets/sheet") && entry.name.endsWith(".xml")) {
                sheetEntries[entry.name] = zip.readBytes()
            }
            entry = zip.nextEntry
        }

        // Match sheet names to entries, or default to Sheet 1, Sheet 2
        var index = 1
        for (sheetName in (if (sheetNames.isNotEmpty()) sheetNames else listOf("Sheet1"))) {
            val key1 = "xl/worksheets/sheet$index.xml"
            val key2 = sheetEntries.keys.firstOrNull { it.endsWith("sheet$index.xml") } ?: sheetEntries.keys.elementAtOrNull(index - 1)
            val sheetData = sheetEntries[key1] ?: (if (key2 != null) sheetEntries[key2] else null)

            if (sheetData != null) {
                val sheet = parseWorksheetXml(sheetName, ByteArrayInputStream(sheetData), sharedStrings)
                sheets.add(sheet)
            }
            index++
        }

        if (sheets.isEmpty()) {
            sheets.add(
                ExcelSheet(
                    name = "Sheet 1",
                    maxCols = 3,
                    rows = listOf(
                        ExcelRow(1, listOf(ExcelCell(0, "A1"), ExcelCell(1, "B1"), ExcelCell(2, "C1"))),
                        ExcelRow(2, listOf(ExcelCell(0, "Empty or unsupported spreadsheet data")))
                    )
                )
            )
        }

        return ParsedExcel(sheets = sheets)
    }

    private fun extractSharedStrings(inputStream: InputStream): List<String> {
        val zip = ZipInputStream(inputStream)
        var entry = zip.nextEntry
        val strings = mutableListOf<String>()

        while (entry != null) {
            if (entry.name == "xl/sharedStrings.xml") {
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val parser = factory.newPullParser()
                parser.setInput(StringReader(zip.bufferedReader(Charsets.UTF_8).readText()))

                var inText = false
                val currentText = StringBuilder()
                var event = parser.eventType

                while (event != XmlPullParser.END_DOCUMENT) {
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "t") inText = true
                        }
                        XmlPullParser.TEXT -> {
                            if (inText) currentText.append(parser.text)
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "t") inText = false
                            if (parser.name == "si") {
                                strings.add(currentText.toString())
                                currentText.clear()
                            }
                        }
                    }
                    event = parser.next()
                }
                break
            }
            entry = zip.nextEntry
        }
        return strings
    }

    private fun extractSheetNames(inputStream: InputStream): List<String> {
        val zip = ZipInputStream(inputStream)
        var entry = zip.nextEntry
        val names = mutableListOf<String>()

        while (entry != null) {
            if (entry.name == "xl/workbook.xml") {
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val parser = factory.newPullParser()
                parser.setInput(StringReader(zip.bufferedReader(Charsets.UTF_8).readText()))

                var event = parser.eventType
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG && parser.name == "sheet") {
                        val name = parser.getAttributeValue(null, "name")
                        if (!name.isNullOrBlank()) {
                            names.add(name)
                        }
                    }
                    event = parser.next()
                }
                break
            }
            entry = zip.nextEntry
        }
        return names
    }

    private fun parseWorksheetXml(
        sheetName: String,
        inputStream: InputStream,
        sharedStrings: List<String>
    ): ExcelSheet {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(inputStream.reader(Charsets.UTF_8))

        val rows = mutableListOf<ExcelRow>()
        var currentRowIndex = 0
        val currentCells = mutableListOf<ExcelCell>()
        var currentCellRef = ""
        var currentCellType = ""
        var currentCellValue = StringBuilder()
        var maxCol = 1

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name ?: ""
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (tag) {
                        "row" -> {
                            currentRowIndex = parser.getAttributeValue(null, "r")?.toIntOrNull() ?: (currentRowIndex + 1)
                            currentCells.clear()
                        }
                        "c" -> {
                            currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                            currentCellType = parser.getAttributeValue(null, "t") ?: ""
                            currentCellValue.clear()
                        }
                        "v", "t" -> {
                            currentCellValue.append(parser.nextText())
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (tag) {
                        "c" -> {
                            val colIndex = colIndexFromRef(currentCellRef)
                            if (colIndex + 1 > maxCol) maxCol = colIndex + 1
                            val rawVal = currentCellValue.toString()
                            val displayVal = if (currentCellType == "s") {
                                val sIndex = rawVal.toIntOrNull()
                                if (sIndex != null && sIndex in sharedStrings.indices) {
                                    sharedStrings[sIndex]
                                } else {
                                    rawVal
                                }
                            } else {
                                rawVal
                            }
                            currentCells.add(
                                ExcelCell(
                                    colIndex = colIndex,
                                    value = displayVal,
                                    isHeader = (currentRowIndex == 1)
                                )
                            )
                        }
                        "row" -> {
                            if (currentCells.isNotEmpty()) {
                                rows.add(ExcelRow(currentRowIndex, currentCells.sortedBy { it.colIndex }))
                            }
                        }
                    }
                }
            }
            event = parser.next()
        }

        return ExcelSheet(name = sheetName, maxCols = maxCol.coerceAtLeast(1), rows = rows)
    }

    private fun colIndexFromRef(ref: String): Int {
        val letters = ref.takeWhile { it.isLetter() }.uppercase()
        if (letters.isEmpty()) return 0
        var result = 0
        for (ch in letters) {
            result = result * 26 + (ch - 'A' + 1)
        }
        return (result - 1).coerceAtLeast(0)
    }
}
