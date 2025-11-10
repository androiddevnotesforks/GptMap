package com.espressodev.gptmap.core.room.domain.repository

import com.espressodev.gptmap.core.model.ImageMessage
import kotlinx.coroutines.flow.Flow

interface ImageMessageRoomRepository {
    fun getImageAnalysisMessages(imageAnalysisId: String): Flow<List<ImageMessage>>
    
    suspend fun addImageMessageToImageAnalysis(
        imageAnalysisId: String,
        message: ImageMessage
    ): Result<Unit>

    suspend fun updateImageMessageInImageAnalysis(
        imageAnalysisId: String,
        messageId: String,
        text: String,
        token: Int
    ): Result<Unit>

    fun getImageType(imageAnalysisId: String): String
}
