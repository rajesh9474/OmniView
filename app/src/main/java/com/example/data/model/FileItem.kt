package com.example.data.model

import android.net.Uri

data class FileItem(
    val id: Long = 0,
    val name: String,
    val uriString: String,
    val fileType: FileType,
    val sizeBytes: Long = 0,
    val lastOpenedTimestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isSample: Boolean = false,
    val sampleAssetKey: String? = null
) {
    val formattedSize: String
        get() = when {
            sizeBytes <= 0 -> "Unknown size"
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> String.format("%.1f KB", sizeBytes / 1024.0)
            else -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
        }

    val uri: Uri
        get() = Uri.parse(uriString)
}
