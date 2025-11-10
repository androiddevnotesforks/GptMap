package com.espressodev.gptmap.core.room.data.repository

import com.espressodev.gptmap.core.model.ImageAnalysis
import com.espressodev.gptmap.core.model.di.Dispatcher
import com.espressodev.gptmap.core.model.di.GmDispatchers
import com.espressodev.gptmap.core.room.dao.ImageAnalysisDao
import com.espressodev.gptmap.core.room.dao.ImageMessageDao
import com.espressodev.gptmap.core.room.domain.repository.ImageAnalysisRoomRepository
import com.espressodev.gptmap.core.room.model.ImageAnalysisEntity
import com.espressodev.gptmap.core.room.model.ImageMessageEntity
import com.espressodev.gptmap.core.room.model.sortByDate
import com.espressodev.gptmap.core.room.model.toImageAnalysis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.ZoneId
import javax.inject.Inject


class ImageAnalysisRoomRepositoryImpl @Inject constructor(
    private val imageAnalysisDao: ImageAnalysisDao,
    private val imageMessageDao: ImageMessageDao,
    @param:Dispatcher(GmDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ImageAnalysisRoomRepository {

    override suspend fun saveImageAnalysis(imageAnalysis: ImageAnalysis): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val imageAnalysisEntity = ImageAnalysisEntity(
                    id = imageAnalysis.id,
                    imageId = imageAnalysis.imageId,
                    imageUrl = imageAnalysis.imageUrl,
                    title = imageAnalysis.title,
                    imageType = imageAnalysis.imageType,
                    date = imageAnalysis.date.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )

                imageAnalysisDao.insertImageAnalysis(imageAnalysisEntity)

                if (imageAnalysis.messages.isNotEmpty()) {
                    val messageEntities = imageAnalysis.messages.map { message ->
                        ImageMessageEntity(
                            id = message.id,
                            imageAnalysisId = imageAnalysisEntity.id,
                            request = message.request,
                            response = message.response,
                            token = message.token,
                            date = message.date.atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                        )
                    }
                    imageMessageDao.insertMessages(messageEntities)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun getImageAnalyses(): Flow<List<ImageAnalysis>> =
        imageAnalysisDao.getImageAnalyses()
            .map { analysisList ->
                analysisList.map { it.toImageAnalysis().sortByDate() }
            }
            .flowOn(ioDispatcher)

    override suspend fun getImageAnalysis(id: String): Result<ImageAnalysis> = runCatching {
        withContext(ioDispatcher) {
            val analysisWithMessages = imageAnalysisDao.getImageAnalysis(id)
            if (analysisWithMessages != null) {
                analysisWithMessages.toImageAnalysis().sortByDate()
            } else {
                throw NoSuchElementException("Image analysis not found for id: $id")
            }
        }
    }

    override suspend fun deleteImageAnalysis(imageAnalysisId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                imageAnalysisDao.deleteImageAnalysis(imageAnalysisId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteImageAnalyses(imageAnalysesIds: Set<String>): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                imageAnalysisDao.deleteImageAnalyses(imageAnalysesIds)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateImageAnalysisText(
        imageAnalysisId: String,
        text: String
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            imageAnalysisDao.updateImageAnalysisTitle(imageAnalysisId, text)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}