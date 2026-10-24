package com.example.ui.viewer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FileType
import com.example.ui.viewer.components.CodeDataViewerComponent
import com.example.ui.viewer.components.CsvViewerComponent
import com.example.ui.viewer.components.ExcelViewerComponent
import com.example.ui.viewer.components.HtmlViewerComponent
import com.example.ui.viewer.components.ImageViewerComponent
import com.example.ui.viewer.components.MarkdownViewerComponent
import com.example.ui.viewer.components.PdfViewerComponent
import com.example.ui.viewer.components.PowerPointViewerComponent
import com.example.ui.viewer.components.TextViewerComponent
import com.example.ui.viewer.components.WordViewerComponent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonViewerScaffold(
    uiState: ViewerUiState,
    viewModel: ViewerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDetailsDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (uiState is ViewerUiState.Ready && uiState.isFullscreen) {
            viewModel.toggleFullscreen()
        } else if (uiState is ViewerUiState.Ready && uiState.isSearchActive) {
            viewModel.toggleSearch()
        } else {
            viewModel.closeViewer()
            onBack()
        }
    }

    when (uiState) {
        is ViewerUiState.Idle -> {
            Box(modifier = modifier.fillMaxSize())
        }

        is ViewerUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(52.dp).testTag("viewer_global_loading"),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Opening ${uiState.fileName}...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Preparing high-fidelity local rendering",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is ViewerUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("viewer_error_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Unable to open ${uiState.fileName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = uiState.suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.closeViewer()
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("viewer_error_back_button")
                        ) {
                            Text("Back to File Manager")
                        }
                    }
                }
            }
        }

        is ViewerUiState.Ready -> {
            val fileItem = uiState.fileItem
            val content = uiState.content

            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    if (!uiState.isFullscreen) {
                        Column {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(fileItem.fileType.badgeColor).copy(alpha = 0.2f),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(
                                                text = fileItem.fileType.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(fileItem.fileType.badgeColor),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = fileItem.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            viewModel.closeViewer()
                                            onBack()
                                        },
                                        modifier = Modifier.testTag("viewer_back_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back to file list"
                                        )
                                    }
                                },
                                actions = {
                                    // Search toggle
                                    IconButton(
                                        onClick = { viewModel.toggleSearch() },
                                        modifier = Modifier.testTag("viewer_search_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search text",
                                            tint = if (uiState.isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Theme Toggle
                                    IconButton(
                                        onClick = onToggleTheme,
                                        modifier = Modifier.testTag("viewer_theme_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Zoom Out
                                    IconButton(
                                        onClick = { viewModel.zoomOut() },
                                        modifier = Modifier.testTag("viewer_zoom_out")
                                    ) {
                                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                                    }

                                    // Zoom In
                                    IconButton(
                                        onClick = { viewModel.zoomIn() },
                                        modifier = Modifier.testTag("viewer_zoom_in")
                                    ) {
                                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In")
                                    }

                                    // Fullscreen
                                    IconButton(
                                        onClick = { viewModel.toggleFullscreen() },
                                        modifier = Modifier.testTag("viewer_fullscreen_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                            contentDescription = "Toggle Fullscreen"
                                        )
                                    }

                                    // Share
                                    IconButton(
                                        onClick = { shareFile(context, fileItem.uri, fileItem.name, fileItem.fileType) },
                                        modifier = Modifier.testTag("viewer_share_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share File")
                                    }

                                    // Details Info
                                    IconButton(
                                        onClick = { showDetailsDialog = true },
                                        modifier = Modifier.testTag("viewer_info_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = "File Details")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            // Expandable Search Bar
                            AnimatedVisibility(visible = uiState.isSearchActive) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = uiState.searchQuery,
                                            onValueChange = { viewModel.updateSearchQuery(it) },
                                            placeholder = { Text("Search in document...") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f).testTag("viewer_search_input"),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                            keyboardActions = KeyboardActions(onSearch = { viewModel.nextMatch() })
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        if (uiState.searchMatchCount > 0) {
                                            Text(
                                                text = "${uiState.currentMatchIndex} of ${uiState.searchMatchCount}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )

                                            IconButton(onClick = { viewModel.prevMatch() }) {
                                                Icon(Icons.Default.ChevronLeft, "Previous match")
                                            }

                                            IconButton(onClick = { viewModel.nextMatch() }) {
                                                Icon(Icons.Default.ChevronRight, "Next match")
                                            }
                                        } else if (uiState.searchQuery.isNotBlank()) {
                                            Text(
                                                text = "No matches",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }

                                        IconButton(onClick = { viewModel.toggleSearch() }) {
                                            Icon(Icons.Default.Close, "Close search")
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (content) {
                        is ViewerContent.Pdf -> {
                            PdfViewerComponent(
                                content = content,
                                onPageChange = { viewModel.goToPdfPage(it) },
                                onRotate = { viewModel.rotatePdf() }
                            )
                        }

                        is ViewerContent.Word -> {
                            WordViewerComponent(
                                content = content,
                                searchQuery = uiState.searchQuery
                            )
                        }

                        is ViewerContent.Excel -> {
                            ExcelViewerComponent(
                                content = content,
                                onSelectSheet = { viewModel.selectExcelSheet(it) },
                                searchQuery = uiState.searchQuery
                            )
                        }

                        is ViewerContent.Presentation -> {
                            PowerPointViewerComponent(
                                content = content,
                                onSlideChange = { viewModel.goToSlide(it) },
                                onTogglePresentationMode = { viewModel.togglePresentationMode() },
                                searchQuery = uiState.searchQuery
                            )
                        }

                        is ViewerContent.Image -> {
                            ImageViewerComponent(
                                content = content,
                                onRotate = { viewModel.rotateImage() }
                            )
                        }

                        is ViewerContent.Text -> {
                            TextViewerComponent(
                                content = content,
                                searchQuery = uiState.searchQuery
                            )
                        }

                        is ViewerContent.Csv -> {
                            CsvViewerComponent(
                                content = content,
                                searchQuery = uiState.searchQuery
                            )
                        }

                        is ViewerContent.Json -> {
                            CodeDataViewerComponent(
                                codeText = content.formattedText,
                                language = "JSON",
                                searchQuery = uiState.searchQuery
                            )
                        }

                        is ViewerContent.Xml -> {
                            CodeDataViewerComponent(
                                codeText = content.formattedText,
                                language = "XML",
                                searchQuery = uiState.searchQuery
                            )
                        }

                        is ViewerContent.Html -> {
                            HtmlViewerComponent(
                                content = content,
                                onToggleCodeView = { viewModel.toggleHtmlMode() },
                                searchQuery = uiState.searchQuery
                            )
                        }

                        is ViewerContent.Markdown -> {
                            MarkdownViewerComponent(
                                content = content,
                                onToggleRawView = { viewModel.toggleMarkdownMode() },
                                searchQuery = uiState.searchQuery
                            )
                        }

                        is ViewerContent.RichText -> {
                            TextViewerComponent(
                                content = ViewerContent.Text(rawText = content.text),
                                searchQuery = uiState.searchQuery
                            )
                        }

                        is ViewerContent.Unsupported -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(content.message)
                            }
                        }
                    }

                    // Floating Exit Fullscreen Button when in fullscreen mode
                    if (uiState.isFullscreen) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            tonalElevation = 6.dp
                        ) {
                            IconButton(onClick = { viewModel.toggleFullscreen() }) {
                                Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen")
                            }
                        }
                    }
                }
            }

            // File Details Dialog
            if (showDetailsDialog) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
                AlertDialog(
                    onDismissRequest = { showDetailsDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("File Details")
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow("File Name", fileItem.name)
                            DetailRow("Format", fileItem.fileType.displayName)
                            DetailRow("Category", fileItem.fileType.category.displayName)
                            DetailRow("Size", fileItem.formattedSize)
                            DetailRow("Last Opened", dateFormat.format(Date(fileItem.lastOpenedTimestamp)))
                            DetailRow("Storage Location", if (fileItem.isSample) "Built-in Sample" else "Device Storage")
                            DetailRow("URI Scheme", fileItem.uri.scheme ?: "unknown")
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showDetailsDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun shareFile(context: Context, uri: Uri, fileName: String, fileType: FileType) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = fileType.mimeTypes.firstOrNull() ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share $fileName"))
    } catch (_: Exception) {
        Toast.makeText(context, "Unable to share file directly", Toast.LENGTH_SHORT).show()
    }
}
