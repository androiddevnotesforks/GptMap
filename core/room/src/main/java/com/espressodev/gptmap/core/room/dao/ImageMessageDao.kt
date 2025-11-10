package com.espressodev.gptmap.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.espressodev.gptmap.core.room.model.ImageMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ImageMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ImageMessageEntity>)

    @Query("""
        UPDATE image_messages 
        SET response = :text, token = :token 
        WHERE id = :messageId
    """)
    suspend fun updateMessage(messageId: String, text: String, token: Int)

    @Query("SELECT * FROM image_messages WHERE imageAnalysisId = :imageAnalysisId ORDER BY date ASC")
    fun getMessages(imageAnalysisId: String): Flow<List<ImageMessageEntity>>

    @Query("DELETE FROM image_messages WHERE imageAnalysisId = :imageAnalysisId")
    suspend fun deleteMessagesForAnalysis(imageAnalysisId: String)
}