package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.FileItem
import com.example.data.model.FileType

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val uriString: String,
    val fileType: String,
    val sizeBytes: Long,
    val lastOpenedTimestamp: Long,
    val isFavorite: Boolean = false,
    val isSample: Boolean = false,
    val sampleAssetKey: String? = null
) {
    fun toFileItem(): FileItem {
        val type = try {
            FileType.valueOf(fileType)
        } catch (_: Exception) {
            FileType.UNKNOWN
        }
        return FileItem(
            id = id,
            name = name,
            uriString = uriString,
            fileType = type,
            sizeBytes = sizeBytes,
            lastOpenedTimestamp = lastOpenedTimestamp,
            isFavorite = isFavorite,
            isSample = isSample,
            sampleAssetKey = sampleAssetKey
        )
    }

    companion object {
        fun fromFileItem(item: FileItem): RecentFileEntity {
            return RecentFileEntity(
                id = item.id,
                name = item.name,
                uriString = item.uriString,
                fileType = item.fileType.name,
                sizeBytes = item.sizeBytes,
                lastOpenedTimestamp = item.lastOpenedTimestamp,
                isFavorite = item.isFavorite,
                isSample = item.isSample,
                sampleAssetKey = item.sampleAssetKey
            )
        }
    }
}
