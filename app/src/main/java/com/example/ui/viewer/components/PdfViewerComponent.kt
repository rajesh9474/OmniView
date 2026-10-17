package com.example.ui.viewer.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewer.ViewerContent

@Composable
fun PdfViewerComponent(
    content: ViewerContent.Pdf,
    onPageChange: (Int) -> Unit,
    onRotate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showThumbnails by remember { mutableStateOf(false) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var internalScale by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    internalScale = (internalScale * zoom).coerceIn(0.8f, 4f)
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                }
            }
    ) {
        // Document Page Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (content.isLoadingPage) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp).testTag("pdf_loading_indicator"),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (content.currentBitmap != null) {
                Card(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = internalScale * content.zoomLevel
                            scaleY = internalScale * content.zoomLevel
                            translationX = panOffsetX
                            translationY = panOffsetY
                            rotationZ = content.rotationDegrees.toFloat()
                        }
                        .testTag("pdf_page_card"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Image(
                        bitmap = content.currentBitmap.asImageBitmap(),
                        contentDescription = "PDF Page ${content.currentPage + 1}",
                        modifier = Modifier.fillMaxWidth(0.95f),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Text(
                    text = "No PDF preview available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Floating Page Navigation Pill at Bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .testTag("pdf_bottom_controls"),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (content.currentPage > 0) {
                            panOffsetX = 0f
                            panOffsetY = 0f
                            internalScale = 1f
                            onPageChange(content.currentPage - 1)
                        }
                    },
                    enabled = content.currentPage > 0,
                    modifier = Modifier.size(40.dp).testTag("pdf_prev_page_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Page"
                    )
                }

                Text(
                    text = "${content.currentPage + 1} / ${content.totalPages.coerceAtLeast(1)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp).testTag("pdf_page_indicator")
                )

                IconButton(
                    onClick = {
                        if (content.currentPage < content.totalPages - 1) {
                            panOffsetX = 0f
                            panOffsetY = 0f
                            internalScale = 1f
                            onPageChange(content.currentPage + 1)
                        }
                    },
                    enabled = content.currentPage < content.totalPages - 1,
                    modifier = Modifier.size(40.dp).testTag("pdf_next_page_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Page"
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onRotate,
                    modifier = Modifier.size(40.dp).testTag("pdf_rotate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = "Rotate PDF"
                    )
                }

                IconButton(
                    onClick = { showThumbnails = !showThumbnails },
                    modifier = Modifier.size(40.dp).testTag("pdf_thumbnails_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewCarousel,
                        contentDescription = "Toggle Page Selector",
                        tint = if (showThumbnails) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Bottom Page Jump Strip
        AnimatedVisibility(
            visible = showThumbnails,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Jump to Page",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        state = rememberLazyListState(initialFirstVisibleItemIndex = content.currentPage)
                    ) {
                        items(content.totalPages) { index ->
                            val isSelected = index == content.currentPage
                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 56.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        panOffsetX = 0f
                                        panOffsetY = 0f
                                        internalScale = 1f
                                        onPageChange(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
