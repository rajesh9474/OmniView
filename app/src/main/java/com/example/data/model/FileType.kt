package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class FileCategory(val displayName: String) {
    ALL("All Files"),
    DOCUMENT("Documents"),
    PDF("PDF"),
    PRESENTATION("Presentations"),
    SPREADSHEET("Spreadsheets"),
    IMAGE("Images"),
    CODE_DATA("Code & Data"),
    OTHER("Other")
}

enum class FileType(
    val extension: String,
    val displayName: String,
    val category: FileCategory,
    val mimeTypes: List<String>,
    val badgeColor: Long
) {
    PDF("pdf", "PDF Document", FileCategory.PDF, listOf("application/pdf"), 0xFFE53935),
    DOC("doc", "Word 97-2003", FileCategory.DOCUMENT, listOf("application/msword"), 0xFF1E88E5),
    DOCX("docx", "Word Document", FileCategory.DOCUMENT, listOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), 0xFF1976D2),
    PPT("ppt", "PowerPoint 97-2003", FileCategory.PRESENTATION, listOf("application/vnd.ms-powerpoint"), 0xFFE64A19),
    PPTX("pptx", "PowerPoint Deck", FileCategory.PRESENTATION, listOf("application/vnd.openxmlformats-officedocument.presentationml.presentation"), 0xFFD84315),
    XLS("xls", "Excel 97-2003", FileCategory.SPREADSHEET, listOf("application/vnd.ms-excel"), 0xFF2E7D32),
    XLSX("xlsx", "Excel Spreadsheet", FileCategory.SPREADSHEET, listOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), 0xFF1B5E20),
    TXT("txt", "Plain Text", FileCategory.CODE_DATA, listOf("text/plain"), 0xFF546E7A),
    CSV("csv", "CSV Spreadsheet", FileCategory.SPREADSHEET, listOf("text/csv", "text/comma-separated-values", "application/csv"), 0xFF00897B),
    RTF("rtf", "Rich Text Format", FileCategory.DOCUMENT, listOf("application/rtf", "text/rtf"), 0xFF8E24AA),
    ODT("odt", "OpenDocument Text", FileCategory.DOCUMENT, listOf("application/vnd.oasis.opendocument.text"), 0xFF0288D1),
    ODS("ods", "OpenDocument Sheet", FileCategory.SPREADSHEET, listOf("application/vnd.oasis.opendocument.spreadsheet"), 0xFF388E3C),
    ODP("odp", "OpenDocument Presentation", FileCategory.PRESENTATION, listOf("application/vnd.oasis.opendocument.presentation"), 0xFFF57C00),
    MARKDOWN("md", "Markdown", FileCategory.CODE_DATA, listOf("text/markdown", "text/x-markdown"), 0xFF3949AB),
    JSON("json", "JSON Data", FileCategory.CODE_DATA, listOf("application/json"), 0xFFF9A825),
    XML("xml", "XML Document", FileCategory.CODE_DATA, listOf("application/xml", "text/xml"), 0xFF0097A7),
    HTML("html", "HTML Web Page", FileCategory.CODE_DATA, listOf("text/html"), 0xFFE65100),
    IMAGE("image", "Image", FileCategory.IMAGE, listOf("image/jpeg", "image/png", "image/webp", "image/gif", "image/bmp", "image/svg+xml"), 0xFF7B1FA2),
    UNKNOWN("bin", "Binary File", FileCategory.OTHER, emptyList(), 0xFF78909C);

    companion object {
        fun fromExtension(ext: String): FileType {
            val normalized = ext.lowercase().trimStart('.')
            return when (normalized) {
                "pdf" -> PDF
                "doc" -> DOC
                "docx" -> DOCX
                "ppt" -> PPT
                "pptx" -> PPTX
                "xls" -> XLS
                "xlsx" -> XLSX
                "txt", "log", "conf", "ini" -> TXT
                "csv", "tsv" -> CSV
                "rtf" -> RTF
                "odt" -> ODT
                "ods" -> ODS
                "odp" -> ODP
                "md", "markdown" -> MARKDOWN
                "json" -> JSON
                "xml" -> XML
                "html", "htm" -> HTML
                "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg" -> IMAGE
                else -> UNKNOWN
            }
        }

        fun fromMimeType(mime: String?): FileType {
            if (mime.isNullOrBlank()) return UNKNOWN
            val lower = mime.lowercase()
            return entries.firstOrNull { type ->
                type.mimeTypes.any { lower.startsWith(it) || lower == it }
            } ?: if (lower.startsWith("image/")) IMAGE
            else if (lower.startsWith("text/")) TXT
            else UNKNOWN
        }
    }
}
