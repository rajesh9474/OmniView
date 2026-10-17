package com.example.ui.viewer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.parser.DocxElement
import com.example.core.parser.DocxParagraph
import com.example.core.parser.DocxTable
import com.example.ui.viewer.ViewerContent

@Composable
fun WordViewerComponent(
    content: ViewerContent.Word,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .verticalScroll(scrollState)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Document Page Canvas
        Card(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .testTag("word_document_canvas"),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = (24 * content.zoomLevel).dp,
                    vertical = (32 * content.zoomLevel).dp
                )
            ) {
                // Header accent line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                for (element in content.parsed.elements) {
                    when (element) {
                        is DocxElement.Paragraph -> {
                            RenderDocxParagraph(
                                paragraph = element.p,
                                zoomLevel = content.zoomLevel,
                                searchQuery = searchQuery
                            )
                        }
                        is DocxElement.Table -> {
                            RenderDocxTable(
                                table = element.table,
                                zoomLevel = content.zoomLevel,
                                searchQuery = searchQuery
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height((10 * content.zoomLevel).dp))
                }
            }
        }
    }
}

@Composable
private fun RenderDocxParagraph(
    paragraph: DocxParagraph,
    zoomLevel: Float,
    searchQuery: String
) {
    val annotatedText = buildAnnotatedString {
        val text = paragraph.text
        if (searchQuery.isNotBlank() && text.contains(searchQuery, ignoreCase = true)) {
            var startIndex = 0
            while (startIndex < text.length) {
                val matchIndex = text.indexOf(searchQuery, startIndex, ignoreCase = true)
                if (matchIndex == -1) {
                    append(text.substring(startIndex))
                    break
                }
                append(text.substring(startIndex, matchIndex))
                withStyle(SpanStyle(background = Color(0xFFFDE047), color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)) {
                    append(text.substring(matchIndex, matchIndex + searchQuery.length))
                }
                startIndex = matchIndex + searchQuery.length
            }
        } else {
            append(text)
        }
    }

    if (paragraph.isBullet) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (12 * zoomLevel).dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = (8 * zoomLevel).dp, end = (8 * zoomLevel).dp)
                    .size((6 * zoomLevel).dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (15 * zoomLevel).sp,
                    lineHeight = (22 * zoomLevel).sp,
                    fontWeight = if (paragraph.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (paragraph.isItalic) FontStyle.Italic else FontStyle.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else if (paragraph.isHeading) {
        val (size, weight, color) = when (paragraph.headingLevel) {
            1 -> Triple((22 * zoomLevel).sp, FontWeight.Bold, MaterialTheme.colorScheme.primary)
            2 -> Triple((18 * zoomLevel).sp, FontWeight.SemiBold, MaterialTheme.colorScheme.secondary)
            else -> Triple((16 * zoomLevel).sp, FontWeight.Medium, MaterialTheme.colorScheme.onSurface)
        }
        Column(modifier = Modifier.padding(top = (8 * zoomLevel).dp, bottom = (4 * zoomLevel).dp)) {
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = size, fontWeight = weight),
                color = color
            )
            if (paragraph.headingLevel == 1) {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    } else {
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = (15 * zoomLevel).sp,
                lineHeight = (23 * zoomLevel).sp,
                fontWeight = if (paragraph.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (paragraph.isItalic) FontStyle.Italic else FontStyle.Normal
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RenderDocxTable(
    table: DocxTable,
    zoomLevel: Float,
    searchQuery: String
) {
    val horizontalScroll = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = (8 * zoomLevel).dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Box(modifier = Modifier.horizontalScroll(horizontalScroll)) {
            Column {
                table.rows.forEachIndexed { rowIndex, row ->
                    val isHeader = rowIndex == 0
                    Row(
                        modifier = Modifier
                            .background(
                                if (isHeader) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                else if (rowIndex % 2 == 1) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                            .padding(vertical = (8 * zoomLevel).dp, horizontal = (12 * zoomLevel).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEachIndexed { colIndex, cellText ->
                            Text(
                                text = cellText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = (13 * zoomLevel).sp,
                                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .widthIn(min = (120 * zoomLevel).dp)
                                    .padding(horizontal = (6 * zoomLevel).dp)
                            )
                        }
                    }
                    if (rowIndex < table.rows.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}
