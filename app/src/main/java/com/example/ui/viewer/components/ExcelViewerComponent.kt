package com.example.ui.viewer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewer.ViewerContent

@Composable
fun ExcelViewerComponent(
    content: ViewerContent.Excel,
    onSelectSheet: (Int) -> Unit,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    val currentSheet = content.parsed.sheets.getOrNull(content.selectedSheetIndex)
        ?: content.parsed.sheets.firstOrNull()

    var selectedCellRef by remember { mutableStateOf<String?>(null) }
    var selectedCellValue by remember { mutableStateOf<String?>(null) }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sheet Tabs Header
        if (content.parsed.sheets.size > 1) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(content.parsed.sheets) { index, sheet ->
                        val isSelected = index == content.selectedSheetIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectSheet(index) },
                            label = { Text(sheet.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(end = 8.dp).testTag("excel_sheet_tab_$index")
                        )
                    }
                }
            }
        }

        // Formula / Cell Inspector Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCellRef ?: "Cell",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(60.dp)
                )

                Text(
                    text = selectedCellValue ?: "Select a cell to view formula/value",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedCellValue != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (currentSheet == null || currentSheet.rows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No worksheet data found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Interactive Spreadsheet Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
                    .horizontalScroll(horizontalScroll)
            ) {
                Column {
                    // Column Headers (A, B, C...)
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        // Empty Corner cell
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 32.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Column Letter headers
                        for (c in 0 until currentSheet.maxCols) {
                            val colLetter = getColumnLetter(c)
                            Box(
                                modifier = Modifier
                                    .size(width = 130.dp, height = 32.dp)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = colLetter,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Data Rows
                    currentSheet.rows.forEach { row ->
                        Row(
                            modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            // Row Number Header on Left
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${row.rowIndex}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Row Cells
                            val cellMap = row.cells.associateBy { it.colIndex }
                            for (c in 0 until currentSheet.maxCols) {
                                val cell = cellMap[c]
                                val cellText = cell?.value ?: ""
                                val isHeaderRow = row.rowIndex == 1
                                val isMatch = searchQuery.isNotBlank() && cellText.contains(searchQuery, ignoreCase = true)
                                val colLetter = getColumnLetter(c)
                                val cellRef = "$colLetter${row.rowIndex}"
                                val isSelected = selectedCellRef == cellRef

                                Box(
                                    modifier = Modifier
                                        .size(width = 130.dp, height = 36.dp)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                                isMatch -> Color(0xFFFEF08A)
                                                isHeaderRow -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                row.rowIndex % 2 == 0 -> MaterialTheme.colorScheme.surface
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 0.5.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                        .clickable {
                                            selectedCellRef = cellRef
                                            selectedCellValue = cellText
                                        }
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = cellText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = if (isHeaderRow) FontWeight.SemiBold else FontWeight.Normal
                                        ),
                                        color = if (isMatch) Color(0xFF0F172A)
                                        else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getColumnLetter(colIndex: Int): String {
    var num = colIndex
    var result = ""
    while (num >= 0) {
        result = ('A'.code + (num % 26)).toChar() + result
        num = (num / 26) - 1
    }
    return result
}
