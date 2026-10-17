package com.example.ui.viewer.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.viewer.ViewerContent

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlViewerComponent(
    content: ViewerContent.Html,
    onToggleCodeView: () -> Unit,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Mode switch toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (content.isCodeView) "HTML Source Code" else "Rendered HTML (Safe Sandbox)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                FilterChip(
                    selected = content.isCodeView,
                    onClick = onToggleCodeView,
                    label = { Text(if (content.isCodeView) "View Rendered" else "View Source") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (content.isCodeView) Icons.Default.Visibility else Icons.Default.Code,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.testTag("html_mode_toggle")
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (content.isCodeView) {
            CodeDataViewerComponent(
                codeText = content.htmlContent,
                language = "HTML",
                searchQuery = searchQuery,
                modifier = Modifier.weight(1f)
            )
        } else {
            Box(modifier = Modifier.weight(1f).testTag("html_webview_container")) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            // Strict security sandbox: disable scripts and local file access
                            settings.javaScriptEnabled = false
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false
                            settings.domStorageEnabled = false
                            loadDataWithBaseURL(null, content.htmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, content.htmlContent, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
