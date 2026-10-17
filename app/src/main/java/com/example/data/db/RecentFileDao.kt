package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFileDao {

    @Query("SELECT * FROM recent_files ORDER BY lastOpenedTimestamp DESC")
    fun getAllRecentFiles(): Flow<List<RecentFileEntity>>

    @Query("SELECT * FROM recent_files WHERE isFavorite = 1 ORDER BY lastOpenedTimestamp DESC")
    fun getFavoriteFiles(): Flow<List<RecentFileEntity>>

    @Query("SELECT * FROM recent_files WHERE uriString = :uriString LIMIT 1")
    suspend fun findByUri(uriString: String): RecentFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: RecentFileEntity): Long

    @Update
    suspend fun update(entity: RecentFileEntity)

    @Query("UPDATE recent_files SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE recent_files SET name = :newName WHERE id = :id")
    suspend fun updateName(id: Long, newName: String)

    @Query("DELETE FROM recent_files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recent_files WHERE isSample = 0")
    suspend fun clearUserRecentFiles()

    @Query("DELETE FROM recent_files")
    suspend fun clearAll()
}
