package com.example.ui.viewer

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.parser.CsvParser
import com.example.core.parser.DocxParser
import com.example.core.parser.MarkdownParser
import com.example.core.parser.OdfParser
import com.example.core.parser.PptxParser
import com.example.core.parser.RtfParser
import com.example.core.parser.XlsxParser
import com.example.core.pdf.PdfEngine
import com.example.data.model.FileItem
import com.example.data.model.FileType
import com.example.data.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class ViewerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository(application)
    private var pdfEngine: PdfEngine? = null

    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Idle)
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    fun openFile(fileItem: FileItem) {
        viewModelScope.launch {
            _uiState.value = ViewerUiState.Loading(fileItem.name)

            // Record in recent files database
            try {
                repository.recordFileOpen(fileItem)
            } catch (_: Exception) {}

            try {
                val context = getApplication<Application>()
                val content = loadContentForFile(context, fileItem)
                _uiState.value = ViewerUiState.Ready(
                    fileItem = fileItem,
                    content = content
                )
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Unknown error"
                val suggestion = when {
                    errorMsg.contains("password", ignoreCase = true) ->
                        "This document is encrypted with a password. Please decrypt the file before viewing."
                    errorMsg.contains("permission", ignoreCase = true) ->
                        "Permission denied. OmniView cannot read this file without read access."
                    errorMsg.contains("not found", ignoreCase = true) || errorMsg.contains("no such file", ignoreCase = true) ->
                        "The file could not be located. It may have been moved, deleted, or unmounted."
                    errorMsg.contains("OutOfMemory", ignoreCase = true) ->
                        "File is too large to render in system memory. Try viewing a smaller document."
                    else ->
                        "Unable to open this file. The file may be corrupted, password protected, or unsupported."
                }
                _uiState.value = ViewerUiState.Error(
                    fileName = fileItem.name,
                    message = errorMsg,
                    suggestion = suggestion
                )
            }
        }
    }

    private suspend fun loadContentForFile(context: Context, fileItem: FileItem): ViewerContent =
        withContext(Dispatchers.IO) {
            val uri = fileItem.uri

            when (fileItem.fileType) {
                FileType.PDF -> {
                    val engine = PdfEngine(context)
                    pdfEngine = engine
                    val result = engine.open(uri)
                    if (result.isFailure) {
                        throw result.exceptionOrNull() ?: Exception("Failed to open PDF")
                    }
                    val totalPages = result.getOrNull() ?: 0
                    val firstPageBitmap = if (totalPages > 0) engine.renderPage(0) else null
                    ViewerContent.Pdf(
                        totalPages = totalPages,
                        currentPage = 0,
                        currentBitmap = firstPageBitmap
                    )
                }

                FileType.DOCX -> {
                    openStream(context, uri).use { stream ->
                        val parsed = DocxParser.parse(stream)
                        ViewerContent.Word(parsed)
                    }
                }

                FileType.DOC -> {
                    // Legacy binary doc stream extraction
                    openStream(context, uri).use { stream ->
                        val text = stream.bufferedReader(Charsets.ISO_8859_1).readText()
                            .filter { it in ' '..'~' || it == '\n' || it == '\r' || it == '\t' }
                        ViewerContent.RichText(text.ifBlank { "Legacy Microsoft Word document content could not be displayed." })
                    }
                }

                FileType.XLSX -> {
                    openStream(context, uri).use { stream ->
                        val parsed = XlsxParser.parse(stream)
                        ViewerContent.Excel(parsed)
                    }
                }

                FileType.XLS -> {
                    openStream(context, uri).use { stream ->
                        val text = stream.bufferedReader(Charsets.ISO_8859_1).readText()
                            .filter { it in ' '..'~' || it == '\n' || it == '\r' || it == '\t' }
                        ViewerContent.RichText(text.ifBlank { "Legacy Microsoft Excel spreadsheet content could not be displayed." })
                    }
                }

                FileType.PPTX -> {
                    openStream(context, uri).use { stream ->
                        val parsed = PptxParser.parse(stream)
                        ViewerContent.Presentation(parsed)
                    }
                }

                FileType.PPT -> {
                    openStream(context, uri).use { stream ->
                        val text = stream.bufferedReader(Charsets.ISO_8859_1).readText()
                            .filter { it in ' '..'~' || it == '\n' || it == '\r' || it == '\t' }
                        ViewerContent.RichText(text.ifBlank { "Legacy PowerPoint presentation content could not be displayed." })
                    }
                }

                FileType.CSV -> {
                    openStream(context, uri).use { stream ->
                        val parsed = CsvParser.parse(stream)
                        ViewerContent.Csv(parsed)
                    }
                }

                FileType.MARKDOWN -> {
                    openStream(context, uri).use { stream ->
                        val text = stream.bufferedReader(Charsets.UTF_8).readText()
                        val parsed = MarkdownParser.parse(text)
                        ViewerContent.Markdown(parsed)
                    }
                }

                FileType.JSON -> {
                    openStream(context, uri).use { stream ->
                        val raw = stream.bufferedReader(Charsets.UTF_8).readText()
                        val formatted = try {
                            val trimmed = raw.trim()
                            if (trimmed.startsWith("{")) {
                                JSONObject(trimmed).toString(2)
                            } else if (trimmed.startsWith("[")) {
                                JSONArray(trimmed).toString(2)
                            } else {
                                raw
                            }
                        } catch (_: Exception) {
                            raw
                        }
                        ViewerContent.Json(formattedText = formatted, rawText = raw)
                    }
                }

                FileType.XML -> {
                    openStream(context, uri).use { stream ->
                        val raw = stream.bufferedReader(Charsets.UTF_8).readText()
                        val formatted = try {
                            val transformer = TransformerFactory.newInstance().newTransformer().apply {
                                setOutputProperty(OutputKeys.INDENT, "yes")
                                setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
                            }
                            val writer = StringWriter()
                            transformer.transform(StreamSource(StringReader(raw)), StreamResult(writer))
                            writer.toString()
                        } catch (_: Exception) {
                            raw
                        }
                        ViewerContent.Xml(formattedText = formatted, rawText = raw)
                    }
                }

                FileType.HTML -> {
                    openStream(context, uri).use { stream ->
                        val text = stream.bufferedReader(Charsets.UTF_8).readText()
                        ViewerContent.Html(htmlContent = text)
                    }
                }

                FileType.RTF -> {
                    openStream(context, uri).use { stream ->
                        val text = RtfParser.parse(stream)
                        ViewerContent.RichText(text)
                    }
                }

                FileType.ODT -> {
                    openStream(context, uri).use { stream ->
                        val parsed = OdfParser.parseOdt(stream)
                        ViewerContent.Word(parsed)
                    }
                }

                FileType.ODS -> {
                    openStream(context, uri).use { stream ->
                        val parsed = OdfParser.parseOds(stream)
                        ViewerContent.Excel(parsed)
                    }
                }

                FileType.ODP -> {
                    openStream(context, uri).use { stream ->
                        val parsed = OdfParser.parseOdp(stream)
                        ViewerContent.Presentation(parsed)
                    }
                }

                FileType.IMAGE -> {
                    var width = 0
                    var height = 0
                    try {
                        openStream(context, uri).use { s ->
                            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeStream(s, null, options)
                            width = options.outWidth
                            height = options.outHeight
                        }
                    } catch (_: Exception) {}

                    ViewerContent.Image(
                        uriString = uri.toString(),
                        width = width,
                        height = height
                    )
                }

                FileType.TXT -> {
                    openStream(context, uri).use { stream ->
                        val text = stream.bufferedReader(Charsets.UTF_8).readText()
                        ViewerContent.Text(rawText = text)
                    }
                }

                FileType.UNKNOWN -> {
                    openStream(context, uri).use { stream ->
                        val text = stream.bufferedReader(Charsets.UTF_8).readText()
                        ViewerContent.Text(rawText = text)
                    }
                }
            }
        }

    private fun openStream(context: Context, uri: Uri): InputStream {
        return context.contentResolver.openInputStream(uri)
            ?: throw Exception("Could not open input stream for $uri")
    }

    // --- PDF Operations ---

    fun goToPdfPage(pageIndex: Int) {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        val pdf = state.content as? ViewerContent.Pdf ?: return
        if (pageIndex !in 0 until pdf.totalPages || pageIndex == pdf.currentPage) return

        viewModelScope.launch {
            _uiState.value = state.copy(content = pdf.copy(isLoadingPage = true))
            val bitmap = pdfEngine?.renderPage(pageIndex, 1.5f * pdf.zoomLevel)
            _uiState.value = state.copy(
                content = pdf.copy(
                    currentPage = pageIndex,
                    currentBitmap = bitmap,
                    isLoadingPage = false
                )
            )
        }
    }

    fun rotatePdf() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        val pdf = state.content as? ViewerContent.Pdf ?: return
        val newRotation = (pdf.rotationDegrees + 90) % 360
        _uiState.value = state.copy(content = pdf.copy(rotationDegrees = newRotation))
    }

    // --- Presentation Operations ---

    fun goToSlide(slideIndex: Int) {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        val ppt = state.content as? ViewerContent.Presentation ?: return
        if (slideIndex in 0 until ppt.parsed.slides.size) {
            _uiState.value = state.copy(content = ppt.copy(currentSlideIndex = slideIndex))
        }
    }

    fun togglePresentationMode() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        val ppt = state.content as? ViewerContent.Presentation ?: return
        _uiState.value = state.copy(content = ppt.copy(isPresentationMode = !ppt.isPresentationMode))
    }

    // --- Excel Operations ---

    fun selectExcelSheet(sheetIndex: Int) {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        val excel = state.content as? ViewerContent.Excel ?: return
        if (sheetIndex in 0 until excel.parsed.sheets.size) {
            _uiState.value = state.copy(content = excel.copy(selectedSheetIndex = sheetIndex))
        }
    }

    // --- Zoom Controls ---

    fun zoomIn() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        when (val c = state.content) {
            is ViewerContent.Pdf -> {
                val newZoom = (c.zoomLevel + 0.25f).coerceAtMost(3.0f)
                viewModelScope.launch {
                    val bitmap = pdfEngine?.renderPage(c.currentPage, 1.5f * newZoom)
                    _uiState.value = state.copy(content = c.copy(zoomLevel = newZoom, currentBitmap = bitmap))
                }
            }
            is ViewerContent.Word -> {
                _uiState.value = state.copy(content = c.copy(zoomLevel = (c.zoomLevel + 0.25f).coerceAtMost(2.5f)))
            }
            is ViewerContent.Text -> {
                _uiState.value = state.copy(content = c.copy(fontSizeSp = (c.fontSizeSp + 2f).coerceAtMost(28f)))
            }
            is ViewerContent.Image -> {
                _uiState.value = state.copy(content = c.copy(zoomLevel = (c.zoomLevel + 0.25f).coerceAtMost(4.0f)))
            }
            else -> {}
        }
    }

    fun zoomOut() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        when (val c = state.content) {
            is ViewerContent.Pdf -> {
                val newZoom = (c.zoomLevel - 0.25f).coerceAtLeast(0.5f)
                viewModelScope.launch {
                    val bitmap = pdfEngine?.renderPage(c.currentPage, 1.5f * newZoom)
                    _uiState.value = state.copy(content = c.copy(zoomLevel = newZoom, currentBitmap = bitmap))
                }
            }
            is ViewerContent.Word -> {
                _uiState.value = state.copy(content = c.copy(zoomLevel = (c.zoomLevel - 0.25f).coerceAtLeast(0.75f)))
            }
            is ViewerContent.Text -> {
                _uiState.value = state.copy(content = c.copy(fontSizeSp = (c.fontSizeSp - 2f).coerceAtLeast(10f)))
            }
            is ViewerContent.Image -> {
                _uiState.value = state.copy(content = c.copy(zoomLevel = (c.zoomLevel - 0.25f).coerceAtLeast(0.5f)))
            }
            else -> {}
        }
    }

    fun resetZoom() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        when (val c = state.content) {
            is ViewerContent.Pdf -> {
                viewModelScope.launch {
                    val bitmap = pdfEngine?.renderPage(c.currentPage, 1.5f)
                    _uiState.value = state.copy(content = c.copy(zoomLevel = 1.0f, currentBitmap = bitmap))
                }
            }
            is ViewerContent.Word -> {
                _uiState.value = state.copy(content = c.copy(zoomLevel = 1.0f))
            }
            is ViewerContent.Text -> {
                _uiState.value = state.copy(content = c.copy(fontSizeSp = 14f))
            }
            is ViewerContent.Image -> {
                _uiState.value = state.copy(content = c.copy(zoomLevel = 1.0f, rotationDegrees = 0))
            }
            else -> {}
        }
    }

    fun rotateImage() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        val img = state.content as? ViewerContent.Image ?: return
        val newRot = (img.rotationDegrees + 90) % 360
        _uiState.value = state.copy(content = img.copy(rotationDegrees = newRot))
    }

    // --- Search inside Document ---

    fun toggleSearch() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        _uiState.value = state.copy(
            isSearchActive = !state.isSearchActive,
            searchQuery = if (state.isSearchActive) "" else state.searchQuery,
            searchMatchCount = if (state.isSearchActive) 0 else state.searchMatchCount,
            currentMatchIndex = 0
        )
    }

    fun updateSearchQuery(query: String) {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        if (query.isBlank()) {
            _uiState.value = state.copy(searchQuery = query, searchMatchCount = 0, currentMatchIndex = 0)
            return
        }

        val textToSearch = when (val c = state.content) {
            is ViewerContent.Word -> c.parsed.elements.joinToString(" ") {
                when (it) {
                    is com.example.core.parser.DocxElement.Paragraph -> it.p.text
                    is com.example.core.parser.DocxElement.Table -> it.table.rows.flatten().joinToString(" ")
                }
            }
            is ViewerContent.Presentation -> c.parsed.slides.joinToString(" ") { it.title + " " + it.bulletPoints.joinToString(" ") }
            is ViewerContent.Excel -> c.parsed.sheets.joinToString(" ") { s -> s.rows.joinToString(" ") { r -> r.cells.joinToString(" ") { it.value } } }
            is ViewerContent.Csv -> c.parsed.headers.joinToString(" ") + " " + c.parsed.rows.joinToString(" ") { it.joinToString(" ") }
            is ViewerContent.Text -> c.rawText
            is ViewerContent.Json -> c.formattedText
            is ViewerContent.Xml -> c.formattedText
            is ViewerContent.Html -> c.htmlContent
            is ViewerContent.Markdown -> c.parsed.rawText
            is ViewerContent.RichText -> c.text
            else -> ""
        }

        val count = Regex(Regex.escape(query), RegexOption.IGNORE_CASE).findAll(textToSearch).count()
        _uiState.value = state.copy(
            searchQuery = query,
            searchMatchCount = count,
            currentMatchIndex = if (count > 0) 1 else 0
        )
    }

    fun nextMatch() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        if (state.searchMatchCount > 1) {
            val next = if (state.currentMatchIndex >= state.searchMatchCount) 1 else state.currentMatchIndex + 1
            _uiState.value = state.copy(currentMatchIndex = next)
        }
    }

    fun prevMatch() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        if (state.searchMatchCount > 1) {
            val prev = if (state.currentMatchIndex <= 1) state.searchMatchCount else state.currentMatchIndex - 1
            _uiState.value = state.copy(currentMatchIndex = prev)
        }
    }

    fun toggleFullscreen() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        _uiState.value = state.copy(isFullscreen = !state.isFullscreen)
    }

    fun toggleHtmlMode() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        val html = state.content as? ViewerContent.Html ?: return
        _uiState.value = state.copy(content = html.copy(isCodeView = !html.isCodeView))
    }

    fun toggleMarkdownMode() {
        val state = _uiState.value as? ViewerUiState.Ready ?: return
        val md = state.content as? ViewerContent.Markdown ?: return
        _uiState.value = state.copy(content = md.copy(isRawView = !md.isRawView))
    }

    fun closeViewer() {
        pdfEngine?.close()
        pdfEngine = null
        _uiState.value = ViewerUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        pdfEngine?.close()
        pdfEngine = null
    }
}
