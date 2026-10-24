package com.example.ui.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.data.model.FileType
import com.example.ui.manager.components.FileItemCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel,
    recentFiles: List<FileItem>,
    favoriteFiles: List<FileItem>,
    uiState: FileManagerUiState,
    onOpenFile: (FileItem) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    var itemToRename by remember { mutableStateOf<FileItem?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var itemForDetails by remember { mutableStateOf<FileItem?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    // System file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val item = viewModel.resolveUriToFileItem(uri)
            onOpenFile(item)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OmniView",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (uiState.currentTab) {
                                    ManagerTab.HOME -> "Universal File Viewer"
                                    ManagerTab.RECENT -> "Recent Files"
                                    ManagerTab.FAVORITES -> "Favorites"
                                    ManagerTab.SAMPLES -> "Sample Documents"
                                    ManagerTab.SETTINGS -> "Preferences"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Quick Theme Toggle Button
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Layout Mode Toggle Button
                    if (uiState.currentTab != ManagerTab.SETTINGS) {
                        IconButton(
                            onClick = { viewModel.toggleLayoutMode() },
                            modifier = Modifier.testTag("top_layout_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.layoutMode == ViewLayoutMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                                contentDescription = "Toggle Grid/List View"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                    NavigationBarItem(
                        selected = uiState.currentTab == ManagerTab.HOME,
                        onClick = { viewModel.setTab(ManagerTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_tab_home")
                    )
                    NavigationBarItem(
                        selected = uiState.currentTab == ManagerTab.RECENT,
                        onClick = { viewModel.setTab(ManagerTab.RECENT) },
                        icon = { Icon(Icons.Default.History, contentDescription = "Recent") },
                        label = { Text("Recent") },
                        modifier = Modifier.testTag("nav_tab_recent")
                    )
                    NavigationBarItem(
                        selected = uiState.currentTab == ManagerTab.FAVORITES,
                        onClick = { viewModel.setTab(ManagerTab.FAVORITES) },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        modifier = Modifier.testTag("nav_tab_favorites")
                    )
                    NavigationBarItem(
                        selected = uiState.currentTab == ManagerTab.SAMPLES,
                        onClick = { viewModel.setTab(ManagerTab.SAMPLES) },
                        icon = { Icon(Icons.Default.FolderOpen, contentDescription = "Samples") },
                        label = { Text("Samples") },
                        modifier = Modifier.testTag("nav_tab_samples")
                    )
                    NavigationBarItem(
                        selected = uiState.currentTab == ManagerTab.SETTINGS,
                        onClick = { viewModel.setTab(ManagerTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
    ) { paddingValues ->
        if (uiState.currentTab == ManagerTab.SETTINGS) {
            SettingsScreen(
                uiState = uiState,
                onSetTheme = { viewModel.setThemeMode(it) },
                onToggleLayout = { viewModel.toggleLayoutMode() },
                onClearHistory = { viewModel.clearRecentFiles() },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Header Hero Banner for HOME tab
                if (uiState.currentTab == ManagerTab.HOME && uiState.searchQuery.isBlank()) {
                    HomeHeroBanner(
                        recentCount = recentFiles.size,
                        favoriteCount = favoriteFiles.size,
                        onOpenPicker = { filePickerLauncher.launch(arrayOf("*/*")) }
                    )
                }

                // Search & Filter Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = {
                                    viewModel.setSearchQuery(it)
                                    if (it.isNotBlank()) {
                                        viewModel.addSearchQueryToHistory(it)
                                    }
                                },
                                placeholder = { Text("Search files by name, extension (.pdf, .xlsx)...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.clearSearchQuery() },
                                            modifier = Modifier.testTag("clear_search_field_btn")
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear search text")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.weight(1f).testTag("file_search_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Sort button
                            Box {
                                IconButton(
                                    onClick = { showSortMenu = true },
                                    modifier = Modifier.testTag("sort_menu_button")
                                ) {
                                    Icon(Icons.Default.Sort, contentDescription = "Sort files")
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Date (Newest first)") },
                                        onClick = { viewModel.setSortCriteria(SortCriteria.DATE_DESC); showSortMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Date (Oldest first)") },
                                        onClick = { viewModel.setSortCriteria(SortCriteria.DATE_ASC); showSortMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Name (A to Z)") },
                                        onClick = { viewModel.setSortCriteria(SortCriteria.NAME_ASC); showSortMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Name (Z to A)") },
                                        onClick = { viewModel.setSortCriteria(SortCriteria.NAME_DESC); showSortMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Size (Largest first)") },
                                        onClick = { viewModel.setSortCriteria(SortCriteria.SIZE_DESC); showSortMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("File Format") },
                                        onClick = { viewModel.setSortCriteria(SortCriteria.TYPE); showSortMenu = false }
                                    )
                                }
                            }
                        }

                        // Quick Search Suggestions / Category Filters Carousel
                        LazyRow(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(FileCategory.entries) { category ->
                                val isSelected = uiState.selectedCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setCategory(category) },
                                    label = { Text(category.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("filter_chip_${category.name}")
                                )
                            }
                        }

                        // Recent Searches Chips (when search query is blank)
                        if (uiState.searchQuery.isBlank() && uiState.recentSearches.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Searches:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(uiState.recentSearches) { searchItem ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.clickable { viewModel.setSearchQuery(searchItem) }
                                        ) {
                                            Text(
                                                text = searchItem,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                                TextButton(
                                    onClick = { viewModel.clearSearchHistory() },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Determine active file list based on current tab
                val sourceFiles = when (uiState.currentTab) {
                    ManagerTab.HOME -> recentFiles.ifEmpty { uiState.sampleFiles }
                    ManagerTab.RECENT -> recentFiles
                    ManagerTab.FAVORITES -> favoriteFiles
                    ManagerTab.SAMPLES -> uiState.sampleFiles
                    ManagerTab.SETTINGS -> emptyList()
                }

                val filteredFiles = remember(sourceFiles, uiState.selectedCategory, uiState.searchQuery, uiState.sortCriteria) {
                    val cleanQuery = uiState.searchQuery.trim().lowercase(Locale.getDefault())
                    sourceFiles
                        .filter { item ->
                            val matchCat = uiState.selectedCategory == FileCategory.ALL || item.fileType.category == uiState.selectedCategory
                            val matchSearch = if (cleanQuery.isBlank()) true else {
                                item.name.lowercase(Locale.getDefault()).contains(cleanQuery) ||
                                item.fileType.extension.lowercase(Locale.getDefault()).contains(cleanQuery) ||
                                item.fileType.displayName.lowercase(Locale.getDefault()).contains(cleanQuery) ||
                                item.fileType.category.displayName.lowercase(Locale.getDefault()).contains(cleanQuery)
                            }
                            matchCat && matchSearch
                        }
                        .sortedWith { a, b ->
                            when (uiState.sortCriteria) {
                                SortCriteria.DATE_DESC -> b.lastOpenedTimestamp.compareTo(a.lastOpenedTimestamp)
                                SortCriteria.DATE_ASC -> a.lastOpenedTimestamp.compareTo(b.lastOpenedTimestamp)
                                SortCriteria.NAME_ASC -> a.name.compareTo(b.name, ignoreCase = true)
                                SortCriteria.NAME_DESC -> b.name.compareTo(a.name, ignoreCase = true)
                                SortCriteria.SIZE_DESC -> b.sizeBytes.compareTo(a.sizeBytes)
                                SortCriteria.SIZE_ASC -> a.sizeBytes.compareTo(b.sizeBytes)
                                SortCriteria.TYPE -> a.fileType.name.compareTo(b.fileType.name)
                            }
                        }
                }

                // Active Search Match Counter Banner
                if (uiState.searchQuery.isNotBlank() && filteredFiles.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${filteredFiles.size} matching file${if (filteredFiles.size > 1) "s" else ""} found",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            TextButton(
                                onClick = { viewModel.clearSearchQuery() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Clear", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                if (filteredFiles.isEmpty()) {
                    // Empty State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (uiState.searchQuery.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No files match \"${uiState.searchQuery}\"",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Try searching by file extension (e.g. .pdf, .docx, .xlsx) or clearing your filters.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { viewModel.clearSearchQuery() },
                                    modifier = Modifier.testTag("empty_search_clear_btn")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reset Search")
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (uiState.currentTab == ManagerTab.FAVORITES) "No favorite files yet" else "No files in this view",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (uiState.currentTab == ManagerTab.FAVORITES) "Tap the star icon on any document to add it to favorites."
                                    else "Tap \"Open File\" to browse from storage, or switch to the Samples tab to test pre-loaded documents.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                                    modifier = Modifier.testTag("empty_state_open_btn")
                                ) {
                                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open File from Storage")
                                }
                            }
                        }
                    }
                } else {
                    // List or Grid presentation
                    if (uiState.layoutMode == ViewLayoutMode.GRID) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize().testTag("files_grid")
                        ) {
                            items(filteredFiles, key = { it.id }) { item ->
                                FileItemCard(
                                    item = item,
                                    isGrid = true,
                                    searchQuery = uiState.searchQuery,
                                    onOpen = { onOpenFile(item) },
                                    onToggleFavorite = { viewModel.toggleFavorite(item) },
                                    onDetails = { itemForDetails = item },
                                    onShare = { shareFileItem(context, item) },
                                    onRename = {
                                        itemToRename = item
                                        renameInput = item.name
                                    },
                                    onDelete = { viewModel.removeRecent(item.id) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize().testTag("files_list")
                        ) {
                            items(filteredFiles, key = { it.id }) { item ->
                                FileItemCard(
                                    item = item,
                                    isGrid = false,
                                    searchQuery = uiState.searchQuery,
                                    onOpen = { onOpenFile(item) },
                                    onToggleFavorite = { viewModel.toggleFavorite(item) },
                                    onDetails = { itemForDetails = item },
                                    onShare = { shareFileItem(context, item) },
                                    onRename = {
                                        itemToRename = item
                                        renameInput = item.name
                                    },
                                    onDelete = { viewModel.removeRecent(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    itemToRename?.let { targetItem ->
        AlertDialog(
            onDismissRequest = { itemToRename = null },
            title = { Text("Rename File Entry") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rename_input_field")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameFile(targetItem.id, renameInput.trim())
                        }
                        itemToRename = null
                    },
                    modifier = Modifier.testTag("rename_confirm_button")
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Details Dialog
    itemForDetails?.let { item ->
        val dateFormat = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { itemForDetails = null },
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
                    ManagerDetailRow("File Name", item.name)
                    ManagerDetailRow("Format", item.fileType.displayName)
                    ManagerDetailRow("Category", item.fileType.category.displayName)
                    ManagerDetailRow("Size", item.formattedSize)
                    ManagerDetailRow("Last Opened", dateFormat.format(Date(item.lastOpenedTimestamp)))
                    ManagerDetailRow("Location", if (item.isSample) "Built-in Sample Library" else "Device Storage")
                    ManagerDetailRow("URI", item.uriString)
                }
            },
            confirmButton = {
                Button(onClick = { itemForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun HomeHeroBanner(
    recentCount: Int,
    favoriteCount: Int,
    onOpenPicker: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("home_hero_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0284C7),
                            Color(0xFF1E3A8A)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "OmniView",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Universal file & document preview engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onOpenPicker,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("hero_open_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Open File",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBadge(label = "Recents", count = recentCount)
                        StatBadge(label = "Favorites", count = favoriteCount)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ManagerDetailRow(label: String, value: String) {
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

private fun shareFileItem(context: Context, item: FileItem) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = item.fileType.mimeTypes.firstOrNull() ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, item.uri)
            putExtra(Intent.EXTRA_SUBJECT, item.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share ${item.name}"))
    } catch (_: Exception) {
        Toast.makeText(context, "Unable to share file directly", Toast.LENGTH_SHORT).show()
    }
}
