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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

@Composable
fun CodeDataViewerComponent(
    codeText: String,
    language: String,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val lines = remember(codeText) { codeText.lines() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Toolbar header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1E293B),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )

                Text(
                    text = "  •  ${lines.size} lines",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText(language, codeText))
                        Toast.makeText(context, "$language copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp).testTag("code_copy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF334155))

        // Code Area
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            // Line numbers
            Column(
                modifier = Modifier
                    .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                    .padding(vertical = 12.dp, horizontal = 10.dp)
            ) {
                lines.forEachIndexed { index, _ ->
                    Text(
                        text = "${index + 1}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            // Code Text Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScroll)
                    .padding(12.dp)
            ) {
                Column {
                    lines.forEach { line ->
                        val annotated = highlightCodeLine(line, language, searchQuery)
                        val textToDisplay = if (annotated.isEmpty()) androidx.compose.ui.text.AnnotatedString(" ") else annotated
                        Text(
                            text = textToDisplay,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

private fun highlightCodeLine(line: String, language: String, searchQuery: String) = buildAnnotatedString {
    if (searchQuery.isNotBlank() && line.contains(searchQuery, ignoreCase = true)) {
        var startIndex = 0
        while (startIndex < line.length) {
            val matchIndex = line.indexOf(searchQuery, startIndex, ignoreCase = true)
            if (matchIndex == -1) {
                appendCodeSpan(line.substring(startIndex), language)
                break
            }
            appendCodeSpan(line.substring(startIndex, matchIndex), language)
            withStyle(SpanStyle(background = Color(0xFFFEF08A), color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)) {
                append(line.substring(matchIndex, matchIndex + searchQuery.length))
            }
            startIndex = matchIndex + searchQuery.length
        }
    } else {
        appendCodeSpan(line, language)
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendCodeSpan(text: String, language: String) {
    if (language.equals("json", ignoreCase = true)) {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c == '"' -> {
                    // String or key
                    val endQuote = text.indexOf('"', i + 1)
                    if (endQuote != -1) {
                        val token = text.substring(i, endQuote + 1)
                        val nextNonWhitespace = text.substring(endQuote + 1).trimStart().firstOrNull()
                        val color = if (nextNonWhitespace == ':') Color(0xFF38BDF8) // Key
                        else Color(0xFF4ADE80) // String value
                        withStyle(SpanStyle(color = color)) { append(token) }
                        i = endQuote + 1
                    } else {
                        withStyle(SpanStyle(color = Color(0xFF4ADE80))) { append(text.substring(i)) }
                        break
                    }
                }
                c in "0123456789" -> {
                    val numEnd = (i until text.length).firstOrNull { !text[it].isDigit() && text[it] != '.' } ?: text.length
                    withStyle(SpanStyle(color = Color(0xFFFB923C))) { append(text.substring(i, numEnd)) }
                    i = numEnd
                }
                c == '{' || c == '}' || c == '[' || c == ']' -> {
                    withStyle(SpanStyle(color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold)) { append(c) }
                    i++
                }
                else -> {
                    val remaining = text.substring(i)
                    if (remaining.startsWith("true") || remaining.startsWith("false")) {
                        val boolText = if (remaining.startsWith("true")) "true" else "false"
                        withStyle(SpanStyle(color = Color(0xFFF87171), fontWeight = FontWeight.Bold)) { append(boolText) }
                        i += boolText.length
                    } else if (remaining.startsWith("null")) {
                        withStyle(SpanStyle(color = Color(0xFF94A3B8), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) { append("null") }
                        i += 4
                    } else {
                        withStyle(SpanStyle(color = Color(0xFFE2E8F0))) { append(c) }
                        i++
                    }
                }
            }
        }
    } else if (language.equals("xml", ignoreCase = true) || language.equals("html", ignoreCase = true)) {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c == '<' -> {
                    val tagEnd = text.indexOf('>', i)
                    if (tagEnd != -1) {
                        val tagFull = text.substring(i, tagEnd + 1)
                        withStyle(SpanStyle(color = Color(0xFF2DD4BF))) { append(tagFull) }
                        i = tagEnd + 1
                    } else {
                        withStyle(SpanStyle(color = Color(0xFF2DD4BF))) { append(text.substring(i)) }
                        break
                    }
                }
                c == '"' -> {
                    val quoteEnd = text.indexOf('"', i + 1)
                    if (quoteEnd != -1) {
                        withStyle(SpanStyle(color = Color(0xFF4ADE80))) { append(text.substring(i, quoteEnd + 1)) }
                        i = quoteEnd + 1
                    } else {
                        withStyle(SpanStyle(color = Color(0xFF4ADE80))) { append(text.substring(i)) }
                        break
                    }
                }
                else -> {
                    withStyle(SpanStyle(color = Color(0xFFE2E8F0))) { append(c) }
                    i++
                }
            }
        }
    } else {
        withStyle(SpanStyle(color = Color(0xFFE2E8F0))) { append(text) }
    }
}
