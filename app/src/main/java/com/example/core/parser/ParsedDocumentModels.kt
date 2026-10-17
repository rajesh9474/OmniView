package com.example.core.parser

// --- Word Document Models ---
data class DocxParagraph(
    val text: String,
    val isHeading: Boolean = false,
    val headingLevel: Int = 0,
    val isBullet: Boolean = false,
    val isBold: Boolean = false,
    val isItalic: Boolean = false
)

data class DocxTable(
    val rows: List<List<String>>
)

sealed class DocxElement {
    data class Paragraph(val p: DocxParagraph) : DocxElement()
    data class Table(val table: DocxTable) : DocxElement()
}

data class ParsedDocx(
    val title: String,
    val elements: List<DocxElement>
)

// --- Excel Spreadsheet Models ---
data class ExcelCell(
    val colIndex: Int,
    val value: String,
    val isHeader: Boolean = false
)

data class ExcelRow(
    val rowIndex: Int,
    val cells: List<ExcelCell>
)

data class ExcelSheet(
    val name: String,
    val maxCols: Int,
    val rows: List<ExcelRow>
)

data class ParsedExcel(
    val sheets: List<ExcelSheet>
)

// --- PowerPoint Presentation Models ---
data class PresentationSlide(
    val slideNumber: Int,
    val title: String,
    val bulletPoints: List<String>,
    val additionalTexts: List<String>
)

data class ParsedPresentation(
    val title: String,
    val slides: List<PresentationSlide>
)

// --- CSV Models ---
data class ParsedCsv(
    val headers: List<String>,
    val rows: List<List<String>>
)

// --- Markdown Models ---
sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class BlockQuote(val text: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
}

data class ParsedMarkdown(
    val rawText: String,
    val blocks: List<MarkdownBlock>
)

// --- Image Info Model ---
data class ImageDetails(
    val width: Int,
    val height: Int,
    val mimeType: String,
    val colorSpace: String,
    val orientationDegrees: Int = 0
)
