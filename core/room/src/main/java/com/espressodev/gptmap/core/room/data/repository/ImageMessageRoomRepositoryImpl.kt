package com.espressodev.gptmap.core.room.data.repository

import com.espressodev.gptmap.core.model.ImageMessage
import com.espressodev.gptmap.core.model.di.Dispatcher
import com.espressodev.gptmap.core.model.di.GmDispatchers
import com.espressodev.gptmap.core.room.dao.ImageAnalysisDao
import com.espressodev.gptmap.core.room.dao.ImageMessageDao
import com.espressodev.gptmap.core.room.domain.repository.ImageMessageRoomRepository
import com.espressodev.gptmap.core.room.model.ImageMessageEntity
import com.espressodev.gptmap.core.room.model.toImageMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.ZoneId
import javax.inject.Inject

class ImageMessageRoomRepositoryImpl @Inject constructor(
    private val imageAnalysisDao: ImageAnalysisDao,
    private val imageMessageDao: ImageMessageDao,
    @param:Dispatcher(GmDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ImageMessageRoomRepository {

    override fun getImageAnalysisMessages(imageAnalysisId: String): Flow<List<ImageMessage>> =
        imageAnalysisDao.getImageAnalysisWithMessages(imageAnalysisId)
            .map { analysisList ->
                analysisList.flatMap { analysisWithMessages ->
                    analysisWithMessages.messages
                        .sortedByDescending { it.date }
                        .map { it.toImageMessage() }
                }
            }
            .flowOn(ioDispatcher)

    override suspend fun addImageMessageToImageAnalysis(
        imageAnalysisId: String,
        message: ImageMessage
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val analysis = imageAnalysisDao.getImageAnalysis(imageAnalysisId)

            if (analysis != null) {
                val messageEntity = ImageMessageEntity(
                    id = message.id,
                    imageAnalysisId = analysis.imageAnalysis.id,
                    request = message.request,
                    response = message.response,
                    token = message.token,
                    date = message.date.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )
                imageMessageDao.insertMessage(messageEntity)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Image analysis not found for id: $imageAnalysisId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateImageMessageInImageAnalysis(
        imageAnalysisId: String,
        messageId: String,
        text: String,
        token: Int
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            imageMessageDao.updateMessage(messageId, text, token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getImageType(imageAnalysisId: String): String = runBlocking {
        imageAnalysisDao.getImageType(imageAnalysisId).orEmpty()
    }
}
