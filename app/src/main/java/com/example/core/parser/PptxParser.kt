package com.example.core.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipInputStream

object PptxParser {

    fun parse(inputStream: InputStream): ParsedPresentation {
        val bytes = inputStream.readBytes()
        val zip = ZipInputStream(ByteArrayInputStream(bytes))
        var entry = zip.nextEntry

        val slideEntries = mutableMapOf<Int, ByteArray>()
        while (entry != null) {
            val name = entry.name
            if (name.startsWith("ppt/slides/slide") && name.endsWith(".xml")) {
                val slideNum = name.substringAfter("slide").substringBefore(".xml").toIntOrNull() ?: 999
                slideEntries[slideNum] = zip.readBytes()
            }
            entry = zip.nextEntry
        }

        val sortedNums = slideEntries.keys.sorted()
        val slides = mutableListOf<PresentationSlide>()

        for (num in sortedNums) {
            val data = slideEntries[num] ?: continue
            val slide = parseSlideXml(num, ByteArrayInputStream(data))
            slides.add(slide)
        }

        if (slides.isEmpty()) {
            slides.add(
                PresentationSlide(
                    slideNumber = 1,
                    title = "Presentation",
                    bulletPoints = listOf("Slide preview could not be loaded or presentation contains no slides."),
                    additionalTexts = emptyList()
                )
            )
        }

        val deckTitle = slides.firstOrNull()?.title?.takeIf { it.isNotBlank() } ?: "Presentation"
        return ParsedPresentation(title = deckTitle, slides = slides)
    }

    private fun parseSlideXml(slideNum: Int, stream: InputStream): PresentationSlide {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(stream.reader(Charsets.UTF_8))

        var slideTitle = ""
        val bulletPoints = mutableListOf<String>()
        val additionalTexts = mutableListOf<String>()

        var inParagraph = false
        val currentParaText = StringBuilder()
        var isTitleShape = false

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name ?: ""

            when (event) {
                XmlPullParser.START_TAG -> {
                    when (tag) {
                        "sp" -> {
                            isTitleShape = false
                        }
                        "ph" -> {
                            val type = parser.getAttributeValue(null, "type")
                            if (type == "title" || type == "ctrTitle") {
                                isTitleShape = true
                            }
                        }
                        "p" -> {
                            inParagraph = true
                            currentParaText.clear()
                        }
                        "t" -> {
                            val text = parser.nextText()
                            if (inParagraph) {
                                currentParaText.append(text)
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tag == "p") {
                        val text = currentParaText.toString().trim()
                        if (text.isNotEmpty()) {
                            if (isTitleShape && slideTitle.isEmpty()) {
                                slideTitle = text
                            } else if (slideTitle.isEmpty() && bulletPoints.isEmpty()) {
                                slideTitle = text
                            } else if (text.length > 3) {
                                bulletPoints.add(text)
                            } else {
                                additionalTexts.add(text)
                            }
                        }
                        inParagraph = false
                    }
                }
            }
            event = parser.next()
        }

        if (slideTitle.isBlank()) {
            slideTitle = "Slide $slideNum"
        }

        return PresentationSlide(
            slideNumber = slideNum,
            title = slideTitle,
            bulletPoints = bulletPoints,
            additionalTexts = additionalTexts
        )
    }
}
