package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.core.detector.FileTypeDetector
import com.example.core.sample.SampleFilesGenerator
import com.example.data.db.AppDatabase
import com.example.data.db.RecentFileEntity
import com.example.data.model.FileItem
import com.example.data.model.FileType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class FileRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.recentFileDao()

    val recentFiles: Flow<List<FileItem>> = dao.getAllRecentFiles().map { list ->
        list.map { it.toFileItem() }
    }

    val favoriteFiles: Flow<List<FileItem>> = dao.getFavoriteFiles().map { list ->
        list.map { it.toFileItem() }
    }

    suspend fun getSampleFiles(): List<FileItem> {
        return SampleFilesGenerator.generateAllSamples(context)
    }

    suspend fun recordFileOpen(fileItem: FileItem): Long {
        val existing = dao.findByUri(fileItem.uriString)
        val toSave = if (existing != null) {
            existing.copy(
                lastOpenedTimestamp = System.currentTimeMillis(),
                sizeBytes = if (fileItem.sizeBytes > 0) fileItem.sizeBytes else existing.sizeBytes
            )
        } else {
            RecentFileEntity.fromFileItem(
                fileItem.copy(lastOpenedTimestamp = System.currentTimeMillis())
            )
        }
        return dao.insertOrUpdate(toSave)
    }

    suspend fun toggleFavorite(fileItem: FileItem) {
        val newFav = !fileItem.isFavorite
        val existing = dao.findByUri(fileItem.uriString)
        if (existing != null) {
            dao.updateFavorite(existing.id, newFav)
        } else {
            val entity = RecentFileEntity.fromFileItem(
                fileItem.copy(isFavorite = newFav, lastOpenedTimestamp = System.currentTimeMillis())
            )
            dao.insertOrUpdate(entity)
        }
    }

    suspend fun renameFile(id: Long, newName: String) {
        dao.updateName(id, newName)
    }

    suspend fun removeRecentFile(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearUserRecentFiles() {
        dao.clearUserRecentFiles()
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    fun resolveFileItem(uri: Uri): FileItem {
        var name = uri.lastPathSegment ?: "Document"
        var size: Long = 0

        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) {
                            name = cursor.getString(nameIndex) ?: name
                        }
                        if (sizeIndex != -1) {
                            size = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (_: Exception) {}
        } else if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (file.exists()) {
                name = file.name
                size = file.length()
            }
        }

        val type = FileTypeDetector.detect(context, uri, name)

        return FileItem(
            name = name,
            uriString = uri.toString(),
            fileType = type,
            sizeBytes = size,
            lastOpenedTimestamp = System.currentTimeMillis()
        )
    }
}
