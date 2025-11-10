package com.espressodev.gptmap.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.espressodev.gptmap.core.room.model.ImageAnalysisEntity
import com.espressodev.gptmap.core.room.model.ImageAnalysisWithMessages
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageAnalysisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageAnalysis(imageAnalysis: ImageAnalysisEntity)

    @Transaction
    @Query("SELECT * FROM image_analysis ORDER BY date DESC")
    fun getImageAnalyses(): Flow<List<ImageAnalysisWithMessages>>

    @Transaction
    @Query("SELECT * FROM image_analysis WHERE imageId = :imageId LIMIT 1")
    suspend fun getImageAnalysis(imageId: String): ImageAnalysisWithMessages?

    @Transaction
    @Query("SELECT * FROM image_analysis WHERE imageId = :imageId")
    fun getImageAnalysisWithMessages(imageId: String): Flow<List<ImageAnalysisWithMessages>>

    @Query("DELETE FROM image_analysis WHERE imageId = :imageId")
    suspend fun deleteImageAnalysis(imageId: String)

    @Query("DELETE FROM image_analysis WHERE imageId IN (:imageIds)")
    suspend fun deleteImageAnalyses(imageIds: Set<String>)

    @Query("UPDATE image_analysis SET title = :text WHERE imageId = :imageId")
    suspend fun updateImageAnalysisTitle(imageId: String, text: String)

    @Query("SELECT imageType FROM image_analysis WHERE imageId = :imageId LIMIT 1")
    suspend fun getImageType(imageId: String): String?

    @Query("DELETE FROM image_analysis")
    suspend fun deleteAll()
}