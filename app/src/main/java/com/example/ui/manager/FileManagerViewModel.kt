package com.example.ui.manager

import android.app.Application
import android.net.Uri
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
    val isDarkTheme: Boolean? = null // null means system default
)

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository(application)

    private val _uiState = MutableStateFlow(FileManagerUiState())
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

    fun setSortCriteria(sort: SortCriteria) {
        _uiState.value = _uiState.value.copy(sortCriteria = sort)
    }

    fun toggleLayoutMode() {
        val next = if (_uiState.value.layoutMode == ViewLayoutMode.LIST) ViewLayoutMode.GRID else ViewLayoutMode.LIST
        _uiState.value = _uiState.value.copy(layoutMode = next)
    }

    fun setThemeMode(dark: Boolean?) {
        _uiState.value = _uiState.value.copy(isDarkTheme = dark)
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
