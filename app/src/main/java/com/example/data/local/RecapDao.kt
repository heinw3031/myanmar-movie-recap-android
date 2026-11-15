package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecapDao {

    @Query("SELECT * FROM recaps ORDER BY createdAt DESC")
    fun observeAllRecaps(): Flow<List<RecapEntity>>

    @Query("SELECT * FROM recaps WHERE jobId = :jobId LIMIT 1")
    suspend fun getRecapById(jobId: String): RecapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecap(recap: RecapEntity)

    @Query("UPDATE recaps SET isPublished = 1, isDraft = 0 WHERE jobId = :jobId")
    suspend fun markPublished(jobId: String)

    @Query("UPDATE recaps SET isDraft = 1, isPublished = 0 WHERE jobId = :jobId")
    suspend fun markDraft(jobId: String)

    @Query("UPDATE recaps SET isSaved = :saved WHERE jobId = :jobId")
    suspend fun setSaved(jobId: String, saved: Boolean)

    @Query("UPDATE recaps SET isLiked = :liked WHERE jobId = :jobId")
    suspend fun setLiked(jobId: String, liked: Boolean)

    @Query("UPDATE recaps SET lastWatchedAt = :timestamp, viewCount = viewCount + 1 WHERE jobId = :jobId")
    suspend fun recordWatch(jobId: String, timestamp: Long)

    @Query("DELETE FROM recaps WHERE jobId = :jobId")
    suspend fun deleteRecap(jobId: String)
}
