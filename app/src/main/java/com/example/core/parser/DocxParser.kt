package com.example.core.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipInputStream

object DocxParser {

    fun parse(inputStream: InputStream): ParsedDocx {
        val zip = ZipInputStream(inputStream)
        var documentXml: String? = null

        var entry = zip.nextEntry
        while (entry != null) {
            if (entry.name == "word/document.xml") {
                documentXml = zip.bufferedReader(Charsets.UTF_8).readText()
                break
            }
            entry = zip.nextEntry
        }

        if (documentXml == null) {
            return ParsedDocx(
                title = "Document",
                elements = listOf(
                    DocxElement.Paragraph(
                        DocxParagraph("No document content could be extracted. The file may be in legacy binary format or corrupted.")
                    )
                )
            )
        }

        return parseDocumentXml(documentXml)
    }

    private fun parseDocumentXml(xml: String): ParsedDocx {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        val elements = mutableListOf<DocxElement>()
        var docTitle = "Document"

        var inParagraph = false
        var inTable = false
        var inRow = false
        var inCell = false
        var inHeading = false
        var headingLevel = 0
        var isBullet = false
        var isBold = false
        var isItalic = false

        val currentParagraphText = StringBuilder()
        val currentTableRow = mutableListOf<String>()
        val currentTableRows = mutableListOf<List<String>>()
        val currentCellText = StringBuilder()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (name) {
                        "tbl" -> {
                            inTable = true
                            currentTableRows.clear()
                        }
                        "tr" -> {
                            inRow = true
                            currentTableRow.clear()
                        }
                        "tc" -> {
                            inCell = true
                            currentCellText.clear()
                        }
                        "p" -> {
                            inParagraph = true
                            currentParagraphText.clear()
                            inHeading = false
                            headingLevel = 0
                            isBullet = false
                        }
                        "pStyle" -> {
                            val styleVal = parser.getAttributeValue(null, "val") ?: ""
                            if (styleVal.contains("Heading", ignoreCase = true) || styleVal.contains("Title", ignoreCase = true)) {
                                inHeading = true
                                headingLevel = styleVal.filter { it.isDigit() }.toIntOrNull() ?: 1
                            }
                        }
                        "numPr" -> {
                            isBullet = true
                        }
                        "b" -> {
                            val bVal = parser.getAttributeValue(null, "val")
                            isBold = (bVal == null || bVal == "1" || bVal == "true")
                        }
                        "i" -> {
                            val iVal = parser.getAttributeValue(null, "val")
                            isItalic = (iVal == null || iVal == "1" || iVal == "true")
                        }
                        "t" -> {
                            val text = parser.nextText()
                            if (inCell) {
                                currentCellText.append(text)
                            } else if (inParagraph) {
                                currentParagraphText.append(text)
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    when (name) {
                        "p" -> {
                            if (!inCell) {
                                val text = currentParagraphText.toString().trim()
                                if (text.isNotEmpty()) {
                                    if (elements.isEmpty() && inHeading) {
                                        docTitle = text
                                    }
                                    elements.add(
                                        DocxElement.Paragraph(
                                            DocxParagraph(
                                                text = text,
                                                isHeading = inHeading,
                                                headingLevel = headingLevel,
                                                isBullet = isBullet,
                                                isBold = isBold,
                                                isItalic = isItalic
                                            )
                                        )
                                    )
                                }
                            }
                            inParagraph = false
                            isBold = false
                            isItalic = false
                        }
                        "tc" -> {
                            currentTableRow.add(currentCellText.toString().trim())
                            inCell = false
                        }
                        "tr" -> {
                            if (currentTableRow.isNotEmpty()) {
                                currentTableRows.add(currentTableRow.toList())
                            }
                            inRow = false
                        }
                        "tbl" -> {
                            if (currentTableRows.isNotEmpty()) {
                                elements.add(DocxElement.Table(DocxTable(currentTableRows.toList())))
                            }
                            inTable = false
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        if (elements.isEmpty()) {
            elements.add(
                DocxElement.Paragraph(DocxParagraph("The document contains no displayable text or formatting."))
            )
        }

        return ParsedDocx(title = docTitle, elements = elements)
    }
}
