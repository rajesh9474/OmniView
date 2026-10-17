package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.detector.FileTypeDetector
import com.example.core.parser.CsvParser
import com.example.core.parser.MarkdownBlock
import com.example.core.parser.MarkdownParser
import com.example.core.parser.RtfParser
import com.example.data.model.FileCategory
import com.example.data.model.FileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name OmniView`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OmniView", appName)
    }

    @Test
    fun `detect file types by extension`() {
        assertEquals(FileType.PDF, FileType.fromExtension("pdf"))
        assertEquals(FileType.DOCX, FileType.fromExtension("docx"))
        assertEquals(FileType.XLSX, FileType.fromExtension("xlsx"))
        assertEquals(FileType.PPTX, FileType.fromExtension("pptx"))
        assertEquals(FileType.MARKDOWN, FileType.fromExtension("md"))
        assertEquals(FileType.CSV, FileType.fromExtension("csv"))
        assertEquals(FileType.JSON, FileType.fromExtension("json"))
        assertEquals(FileType.XML, FileType.fromExtension("xml"))
        assertEquals(FileType.HTML, FileType.fromExtension("html"))
        assertEquals(FileType.RTF, FileType.fromExtension("rtf"))
        assertEquals(FileType.ODT, FileType.fromExtension("odt"))
        assertEquals(FileType.IMAGE, FileType.fromExtension("png"))
    }

    @Test
    fun `csv parser handles quotes and headers`() {
        val csvData = """
            Name,Role,Status
            "Smith, John",Engineer,Active
            "Doe, Jane",Architect,Active
        """.trimIndent()

        val parsed = CsvParser.parse(ByteArrayInputStream(csvData.toByteArray(Charsets.UTF_8)))
        assertEquals(3, parsed.headers.size)
        assertEquals("Name", parsed.headers[0])
        assertEquals(2, parsed.rows.size)
        assertEquals("Smith, John", parsed.rows[0][0])
    }

    @Test
    fun `markdown parser parses headers and code blocks`() {
        val md = """
            # OmniView
            ## Architecture
            ```kotlin
            val x = 10
            ```
            - Item 1
        """.trimIndent()

        val parsed = MarkdownParser.parse(md)
        assertTrue(parsed.blocks.any { it is MarkdownBlock.Header && it.text == "OmniView" })
        assertTrue(parsed.blocks.any { it is MarkdownBlock.CodeBlock && it.language == "kotlin" })
        assertTrue(parsed.blocks.any { it is MarkdownBlock.BulletItem && it.text == "Item 1" })
    }

    @Test
    fun `rtf parser decodes rtf tokens`() {
        val rtf = "{\\rtf1\\ansi Hello \\b World\\b0\\par Next line}"
        val result = RtfParser.parse(ByteArrayInputStream(rtf.toByteArray(Charsets.UTF_8)))
        assertTrue(result.contains("Hello"))
        assertTrue(result.contains("World"))
    }
}
