package com.example.ui.viewer.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewer.ViewerContent

@Composable
fun TextViewerComponent(
    content: ViewerContent.Text,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isWordWrap by remember { mutableStateOf(content.isWordWrap) }
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val lines = remember(content.rawText) { content.rawText.lines() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar info header
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
                    text = "${lines.size} lines  •  ${content.rawText.length} characters",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { isWordWrap = !isWordWrap },
                    modifier = Modifier.size(36.dp).testTag("text_wrap_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.WrapText,
                        contentDescription = "Toggle Word Wrap",
                        tint = if (isWordWrap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Document Text", content.rawText))
                        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp).testTag("text_copy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Text",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Text Content with Line Numbers
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            // Line numbers gutter
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 12.dp, horizontal = 10.dp)
            ) {
                lines.forEachIndexed { index, _ ->
                    Text(
                        text = "${index + 1}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = content.fontSizeSp.sp,
                        lineHeight = (content.fontSizeSp * 1.5f).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            // Text canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(if (!isWordWrap) Modifier.horizontalScroll(horizontalScroll) else Modifier)
                    .padding(12.dp)
            ) {
                Column {
                    lines.forEach { line ->
                        val annotated = buildAnnotatedString {
                            if (searchQuery.isNotBlank() && line.contains(searchQuery, ignoreCase = true)) {
                                var startIndex = 0
                                while (startIndex < line.length) {
                                    val matchIndex = line.indexOf(searchQuery, startIndex, ignoreCase = true)
                                    if (matchIndex == -1) {
                                        append(line.substring(startIndex))
                                        break
                                    }
                                    append(line.substring(startIndex, matchIndex))
                                    withStyle(SpanStyle(background = Color(0xFFFEF08A), color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)) {
                                        append(line.substring(matchIndex, matchIndex + searchQuery.length))
                                    }
                                    startIndex = matchIndex + searchQuery.length
                                }
                            } else {
                                append(line)
                            }
                        }

                        val textToDisplay = if (annotated.isEmpty()) androidx.compose.ui.text.AnnotatedString(" ") else annotated
                        Text(
                            text = textToDisplay,
                            fontFamily = FontFamily.Monospace,
                            fontSize = content.fontSizeSp.sp,
                            lineHeight = (content.fontSizeSp * 1.5f).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            softWrap = isWordWrap
                        )
                    }
                }
            }
        }
    }
}
