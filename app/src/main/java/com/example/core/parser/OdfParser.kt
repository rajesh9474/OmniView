package com.example.core.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipInputStream

object OdfParser {

    fun parseOdt(inputStream: InputStream): ParsedDocx {
        val xml = extractContentXml(inputStream) ?: return ParsedDocx("Document", emptyList())
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        val elements = mutableListOf<DocxElement>()
        var inParagraph = false
        var inHeading = false
        val currentText = StringBuilder()

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (name == "p") {
                        inParagraph = true
                        currentText.clear()
                    } else if (name == "h") {
                        inHeading = true
                        currentText.clear()
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inParagraph || inHeading) {
                        currentText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name == "p" || name == "h") {
                        val text = currentText.toString().trim()
                        if (text.isNotEmpty()) {
                            elements.add(
                                DocxElement.Paragraph(
                                    DocxParagraph(
                                        text = text,
                                        isHeading = inHeading,
                                        headingLevel = if (inHeading) 1 else 0
                                    )
                                )
                            )
                        }
                        inParagraph = false
                        inHeading = false
                    }
                }
            }
            event = parser.next()
        }

        return ParsedDocx(
            title = elements.firstOrNull()?.let { (it as? DocxElement.Paragraph)?.p?.text } ?: "OpenDocument Text",
            elements = elements
        )
    }

    fun parseOds(inputStream: InputStream): ParsedExcel {
        val xml = extractContentXml(inputStream) ?: return ParsedExcel(emptyList())
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        val sheets = mutableListOf<ExcelSheet>()
        var currentSheetName = "Sheet 1"
        val currentRows = mutableListOf<ExcelRow>()
        val currentCells = mutableListOf<ExcelCell>()
        var rowIndex = 1
        var colIndex = 0
        var maxCol = 1
        var inCell = false
        val cellText = StringBuilder()

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (name) {
                        "table" -> {
                            currentSheetName = parser.getAttributeValue(null, "name") ?: "Sheet ${sheets.size + 1}"
                            currentRows.clear()
                            rowIndex = 1
                        }
                        "table-row" -> {
                            currentCells.clear()
                            colIndex = 0
                        }
                        "table-cell" -> {
                            inCell = true
                            cellText.clear()
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inCell) cellText.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (name) {
                        "table-cell" -> {
                            val text = cellText.toString().trim()
                            if (text.isNotEmpty()) {
                                currentCells.add(ExcelCell(colIndex = colIndex, value = text, isHeader = (rowIndex == 1)))
                                if (colIndex + 1 > maxCol) maxCol = colIndex + 1
                            }
                            colIndex++
                            inCell = false
                        }
                        "table-row" -> {
                            if (currentCells.isNotEmpty()) {
                                currentRows.add(ExcelRow(rowIndex, currentCells.toList()))
                            }
                            rowIndex++
                        }
                        "table" -> {
                            if (currentRows.isNotEmpty()) {
                                sheets.add(ExcelSheet(name = currentSheetName, maxCols = maxCol, rows = currentRows.toList()))
                            }
                        }
                    }
                }
            }
            event = parser.next()
        }

        return ParsedExcel(sheets)
    }

    fun parseOdp(inputStream: InputStream): ParsedPresentation {
        val xml = extractContentXml(inputStream) ?: return ParsedPresentation("Presentation", emptyList())
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        val slides = mutableListOf<PresentationSlide>()
        var slideIndex = 1
        var currentTitle = ""
        val bulletPoints = mutableListOf<String>()
        var inText = false
        val currentText = StringBuilder()

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (name == "page") {
                        currentTitle = ""
                        bulletPoints.clear()
                    } else if (name == "p" || name == "h") {
                        inText = true
                        currentText.clear()
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inText) currentText.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (name == "p" || name == "h") {
                        val text = currentText.toString().trim()
                        if (text.isNotEmpty()) {
                            if (currentTitle.isEmpty()) {
                                currentTitle = text
                            } else {
                                bulletPoints.add(text)
                            }
                        }
                        inText = false
                    } else if (name == "page") {
                        slides.add(
                            PresentationSlide(
                                slideNumber = slideIndex,
                                title = if (currentTitle.isNotBlank()) currentTitle else "Slide $slideIndex",
                                bulletPoints = bulletPoints.toList(),
                                additionalTexts = emptyList()
                            )
                        )
                        slideIndex++
                    }
                }
            }
            event = parser.next()
        }

        return ParsedPresentation(
            title = slides.firstOrNull()?.title ?: "Presentation",
            slides = slides
        )
    }

    private fun extractContentXml(inputStream: InputStream): String? {
        val zip = ZipInputStream(inputStream)
        var entry = zip.nextEntry
        while (entry != null) {
            if (entry.name == "content.xml") {
                return zip.bufferedReader(Charsets.UTF_8).readText()
            }
            entry = zip.nextEntry
        }
        return null
    }
}
