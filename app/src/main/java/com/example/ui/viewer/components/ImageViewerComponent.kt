package com.example.ui.viewer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.viewer.ViewerContent

@Composable
fun ImageViewerComponent(
    content: ViewerContent.Image,
    onRotate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var internalScale by remember { mutableFloatStateOf(1f) }
    var showInfo by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    internalScale = (internalScale * zoom).coerceIn(0.5f, 5.0f)
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                }
            }
    ) {
        // Render Image with gestures
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(content.uriString)
                .crossfade(true)
                .build(),
            contentDescription = "Image Preview",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = internalScale * content.zoomLevel
                    scaleY = internalScale * content.zoomLevel
                    translationX = panOffsetX
                    translationY = panOffsetY
                    rotationZ = content.rotationDegrees.toFloat()
                }
                .testTag("image_preview_canvas")
        )

        // Floating Bottom Controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .testTag("image_bottom_controls"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRotate,
                    modifier = Modifier.testTag("image_rotate_button")
                ) {
                    Icon(imageVector = Icons.Default.RotateRight, contentDescription = "Rotate Image")
                }

                IconButton(
                    onClick = {
                        panOffsetX = 0f
                        panOffsetY = 0f
                        internalScale = 1f
                    },
                    modifier = Modifier.testTag("image_fit_button")
                ) {
                    Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Fit to Screen")
                }

                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier.testTag("image_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Image Details",
                        tint = if (showInfo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Info Overlay Card
        if (showInfo) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .width(220.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Image Specifications",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Resolution: ${if (content.width > 0) "${content.width} × ${content.height} px" else "Auto"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Rotation: ${content.rotationDegrees}°",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Zoom: ${String.format("%.1fx", internalScale * content.zoomLevel)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
