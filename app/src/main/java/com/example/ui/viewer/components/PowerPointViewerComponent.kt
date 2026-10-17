package com.example.ui.viewer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewer.ViewerContent

@Composable
fun PowerPointViewerComponent(
    content: ViewerContent.Presentation,
    onSlideChange: (Int) -> Unit,
    onTogglePresentationMode: () -> Unit,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    val slides = content.parsed.slides
    val currentSlide = slides.getOrNull(content.currentSlideIndex) ?: slides.firstOrNull()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (content.isPresentationMode) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(if (content.isPresentationMode) 0.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Presentation Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (currentSlide != null) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 840.dp)
                        .fillMaxWidth(if (content.isPresentationMode) 0.98f else 0.95f)
                        .aspectRatio(16f / 9f)
                        .testTag("powerpoint_slide_card"),
                    shape = RoundedCornerShape(if (content.isPresentationMode) 8.dp else 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .padding(24.dp)
                            .verticalScroll(scrollState)
                    ) {
                        // Slide Header Banner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SLIDE ${currentSlide.slideNumber} OF ${slides.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Icon(
                                imageVector = Icons.Default.Slideshow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Slide Title
                        Text(
                            text = currentSlide.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.primary,
                            thickness = 2.dp,
                            modifier = Modifier.width(64.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        // Slide Bullet Items
                        currentSlide.bulletPoints.forEach { point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp, end = 12.dp)
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Additional texts
                        currentSlide.additionalTexts.forEach { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Slide Controls Bar
        Surface(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .testTag("powerpoint_navigation_bar"),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (content.currentSlideIndex > 0) {
                            onSlideChange(content.currentSlideIndex - 1)
                        }
                    },
                    enabled = content.currentSlideIndex > 0,
                    modifier = Modifier.testTag("ppt_prev_button")
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Slide")
                }

                Text(
                    text = "${content.currentSlideIndex + 1} / ${slides.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp).testTag("ppt_slide_counter")
                )

                IconButton(
                    onClick = {
                        if (content.currentSlideIndex < slides.size - 1) {
                            onSlideChange(content.currentSlideIndex + 1)
                        }
                    },
                    enabled = content.currentSlideIndex < slides.size - 1,
                    modifier = Modifier.testTag("ppt_next_button")
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Slide")
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onTogglePresentationMode,
                    modifier = Modifier.testTag("ppt_presentation_mode_toggle")
                ) {
                    Icon(
                        imageVector = if (content.isPresentationMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Toggle Presentation Mode",
                        tint = if (content.isPresentationMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Slide Thumbnails Strip
        AnimatedVisibility(visible = !content.isPresentationMode) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                state = rememberLazyListState(initialFirstVisibleItemIndex = content.currentSlideIndex)
            ) {
                itemsIndexed(slides) { index, slide ->
                    val isSelected = index == content.currentSlideIndex
                    Card(
                        modifier = Modifier
                            .width(110.dp)
                            .height(68.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSlideChange(index) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "Slide ${slide.slideNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = slide.title,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
