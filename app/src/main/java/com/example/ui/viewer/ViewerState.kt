package com.example.ui.viewer

import android.graphics.Bitmap
import com.example.core.parser.ParsedCsv
import com.example.core.parser.ParsedDocx
import com.example.core.parser.ParsedExcel
import com.example.core.parser.ParsedMarkdown
import com.example.core.parser.ParsedPresentation
import com.example.data.model.FileItem

sealed class ViewerContent {
    data class Pdf(
        val totalPages: Int,
        val currentPage: Int = 0,
        val zoomLevel: Float = 1.0f,
        val rotationDegrees: Int = 0,
        val currentBitmap: Bitmap? = null,
        val isLoadingPage: Boolean = false
    ) : ViewerContent()

    data class Word(
        val parsed: ParsedDocx,
        val zoomLevel: Float = 1.0f
    ) : ViewerContent()

    data class Excel(
        val parsed: ParsedExcel,
        val selectedSheetIndex: Int = 0,
        val filterQuery: String = ""
    ) : ViewerContent()

    data class Presentation(
        val parsed: ParsedPresentation,
        val currentSlideIndex: Int = 0,
        val isPresentationMode: Boolean = false
    ) : ViewerContent()

    data class Csv(
        val parsed: ParsedCsv,
        val filterQuery: String = ""
    ) : ViewerContent()

    data class Text(
        val rawText: String,
        val isWordWrap: Boolean = true,
        val fontSizeSp: Float = 14f
    ) : ViewerContent()

    data class Json(
        val formattedText: String,
        val rawText: String,
        val isValidJson: Boolean = true
    ) : ViewerContent()

    data class Xml(
        val formattedText: String,
        val rawText: String
    ) : ViewerContent()

    data class Html(
        val htmlContent: String,
        val isCodeView: Boolean = false
    ) : ViewerContent()

    data class Markdown(
        val parsed: ParsedMarkdown,
        val isRawView: Boolean = false
    ) : ViewerContent()

    data class RichText(
        val text: String
    ) : ViewerContent()

    data class Image(
        val uriString: String,
        val width: Int = 0,
        val height: Int = 0,
        val rotationDegrees: Int = 0,
        val zoomLevel: Float = 1.0f
    ) : ViewerContent()

    data class Unsupported(
        val message: String
    ) : ViewerContent()
}

sealed class ViewerUiState {
    object Idle : ViewerUiState()
    data class Loading(val fileName: String) : ViewerUiState()
    data class Error(val fileName: String, val message: String, val suggestion: String) : ViewerUiState()
    data class Ready(
        val fileItem: FileItem,
        val content: ViewerContent,
        val isSearchActive: Boolean = false,
        val searchQuery: String = "",
        val searchMatchCount: Int = 0,
        val currentMatchIndex: Int = 0,
        val isFullscreen: Boolean = false
    ) : ViewerUiState()
}
