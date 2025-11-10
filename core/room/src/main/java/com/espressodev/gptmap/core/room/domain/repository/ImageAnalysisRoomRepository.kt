package com.espressodev.gptmap.core.room.domain.repository

import com.espressodev.gptmap.core.model.ImageAnalysis
import kotlinx.coroutines.flow.Flow

interface ImageAnalysisRoomRepository {
    suspend fun saveImageAnalysis(imageAnalysis: ImageAnalysis): Result<Unit>
    fun getImageAnalyses(): Flow<List<ImageAnalysis>>
    suspend fun getImageAnalysis(id: String): Result<ImageAnalysis>
    suspend fun deleteImageAnalysis(imageAnalysisId: String): Result<Unit>
    suspend fun deleteImageAnalyses(imageAnalysesIds: Set<String>): Result<Unit>
    suspend fun updateImageAnalysisText(imageAnalysisId: String, text: String): Result<Unit>
}