package com.example.ui.manager

import android.app.Application
import android.net.Uri
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortCriteria {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC,
    TYPE
}

enum class ViewLayoutMode {
    LIST,
    GRID
}

enum class ManagerTab {
    HOME,
    RECENT,
    FAVORITES,
    SAMPLES,
    SETTINGS
}

data class FileManagerUiState(
    val currentTab: ManagerTab = ManagerTab.HOME,
    val selectedCategory: FileCategory = FileCategory.ALL,
    val searchQuery: String = "",
    val sortCriteria: SortCriteria = SortCriteria.DATE_DESC,
    val layoutMode: ViewLayoutMode = ViewLayoutMode.LIST,
    val sampleFiles: List<FileItem> = emptyList(),
    val isDarkTheme: Boolean? = null, // null means system default
    val recentSearches: List<String> = emptyList()
)

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository(application)
    private val prefs = application.getSharedPreferences("omniview_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        FileManagerUiState(
            isDarkTheme = when (prefs.getString("theme_mode", "SYSTEM")) {
                "DARK" -> true
                "LIGHT" -> false
                else -> null
            },
            recentSearches = prefs.getStringSet("recent_searches", emptySet())?.toList() ?: emptyList()
        )
    )
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    val recentFiles = repository.recentFiles.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val favoriteFiles = repository.favoriteFiles.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        loadSamples()
    }

    private fun loadSamples() {
        viewModelScope.launch {
            val samples = repository.getSampleFiles()
            _uiState.value = _uiState.value.copy(sampleFiles = samples)
        }
    }

    fun setTab(tab: ManagerTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setCategory(category: FileCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun clearSearchQuery() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
    }

    fun addSearchQueryToHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        val current = _uiState.value.recentSearches.toMutableList()
        current.remove(trimmed)
        current.add(0, trimmed)
        val updated = current.take(6)
        _uiState.value = _uiState.value.copy(recentSearches = updated)
        prefs.edit().putStringSet("recent_searches", updated.toSet()).apply()
    }

    fun clearSearchHistory() {
        _uiState.value = _uiState.value.copy(recentSearches = emptyList())
        prefs.edit().remove("recent_searches").apply()
    }

    fun setSortCriteria(sort: SortCriteria) {
        _uiState.value = _uiState.value.copy(sortCriteria = sort)
    }

    fun toggleLayoutMode() {
        val next = if (_uiState.value.layoutMode == ViewLayoutMode.LIST) ViewLayoutMode.GRID else ViewLayoutMode.LIST
        _uiState.value = _uiState.value.copy(layoutMode = next)
    }

    fun setThemeMode(dark: Boolean?) {
        _uiState.value = _uiState.value.copy(isDarkTheme = dark)
        prefs.edit().putString(
            "theme_mode",
            when (dark) {
                true -> "DARK"
                false -> "LIGHT"
                null -> "SYSTEM"
            }
        ).apply()
    }

    fun toggleTheme(isSystemCurrentlyDark: Boolean = false) {
        val current = _uiState.value.isDarkTheme
        val next = when (current) {
            null -> !isSystemCurrentlyDark
            true -> false
            false -> true
        }
        setThemeMode(next)
    }

    fun toggleFavorite(fileItem: FileItem) {
        viewModelScope.launch {
            repository.toggleFavorite(fileItem)
        }
    }

    fun renameFile(id: Long, newName: String) {
        viewModelScope.launch {
            repository.renameFile(id, newName)
        }
    }

    fun removeRecent(id: Long) {
        viewModelScope.launch {
            repository.removeRecentFile(id)
        }
    }

    fun clearRecentFiles() {
        viewModelScope.launch {
            repository.clearUserRecentFiles()
        }
    }

    fun resolveUriToFileItem(uri: Uri): FileItem {
        return repository.resolveFileItem(uri)
    }
}
