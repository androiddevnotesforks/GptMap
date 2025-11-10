package com.espressodev.gptmap.core.data.repository.impl

import android.content.Context
import android.graphics.Bitmap
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.espressodev.gptmap.core.data.repository.ImageAnalysisRepository
import com.espressodev.gptmap.core.data.util.runCatchingWithContext
import com.espressodev.gptmap.core.data.worker.DeleteImagesFromStorageAndPhoneWorker
import com.espressodev.gptmap.core.datastore.DataStoreService
import com.espressodev.gptmap.core.firebase.StorageRepository
import com.espressodev.gptmap.core.model.Constants
import com.espressodev.gptmap.core.model.ImageAnalysis
import com.espressodev.gptmap.core.model.ImageType
import com.espressodev.gptmap.core.model.di.Dispatcher
import com.espressodev.gptmap.core.model.di.GmDispatchers.IO
import com.espressodev.gptmap.core.model.ext.compressImage
import com.espressodev.gptmap.core.model.ext.downloadResizeAndCompress
import com.espressodev.gptmap.core.model.ext.resizeImage
import com.espressodev.gptmap.core.model.ext.saveToInternalStorageIfNotExist
import com.espressodev.gptmap.core.model.ext.toBitmap
import com.espressodev.gptmap.core.room.domain.repository.FavouriteRoomRepository
import com.espressodev.gptmap.core.room.domain.repository.ImageAnalysisRoomRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class ImageAnalysisRepositoryImpl @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context,
    private val imageAnalysisRoomRepository: ImageAnalysisRoomRepository,
    private val dataStoreService: DataStoreService,
    private val favouriteRoomRepository: FavouriteRoomRepository,
    private val storageRepository: StorageRepository
) : ImageAnalysisRepository {
    override suspend fun deleteImageAnalyses(imageIds: Set<String>): Result<Unit> =
        runCatchingWithContext(ioDispatcher) {
            updateDataStoreIfNecessary(imageIds)

            val deleteFromRealmJob = launch {
                imageAnalysisRoomRepository.deleteImageAnalyses(imageIds).getOrThrow()
            }
            deleteFromRealmJob.join()

            val inputData =
                Data.Builder().putStringArray("imageIds", imageIds.toTypedArray()).build()
            val workRequest = OneTimeWorkRequestBuilder<DeleteImagesFromStorageAndPhoneWorker>()
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }

    private suspend fun updateDataStoreIfNecessary(imageIds: Set<String>) =
        runCatchingWithContext(ioDispatcher) {
            val latestImageIdForChat = dataStoreService.getLatestImageIdForChat().first()
            if (latestImageIdForChat in imageIds) {
                launch {
                    dataStoreService.setLatestImageIdForChat("")
                }
                launch {
                    dataStoreService.setLatestImageUrl("")
                }
                launch {
                    favouriteRoomRepository.resetImageAnalysisId(latestImageIdForChat)
                }
            }
        }

    override suspend fun turnImageToImageAnalysis(imageUrl: String): Result<String> =
        runCatchingWithContext(ioDispatcher) {
            val bitmap =
                imageUrl.downloadResizeAndCompress(
                    width = Constants.DOWNLOAD_IMAGE_WIDTH_FOR_ANALYSIS,
                    height = Constants.DOWNLOAD_IMAGE_HEIGHT_FOR_ANALYSIS
                ).toBitmap()

            val imageAnalysisId = saveImageAnalysisToStorage(
                bitmap = bitmap,
                title = "",
                imageWidth = Constants.DOWNLOAD_IMAGE_WIDTH_FOR_ANALYSIS,
                imageHeight = Constants.DOWNLOAD_IMAGE_HEIGHT_FOR_ANALYSIS,
                imageType = ImageType.Favourite
            ).getOrThrow()

            launch {
                dataStoreService.setLatestImageIdForChat(imageAnalysisId)
            }
            launch {
                dataStoreService.setLatestImageUrl(imageUrl)
            }
            launch {
                bitmap.saveToInternalStorageIfNotExist(context, imageAnalysisId)
            }
            imageAnalysisId
        }

    override suspend fun saveImageAnalysisToStorage(
        bitmap: Bitmap,
        title: String,
        imageWidth: Int,
        imageHeight: Int,
        imageType: ImageType
    ): Result<String> = runCatchingWithContext(ioDispatcher) {
        val byteArray =
            bitmap.resizeImage(imageWidth, imageHeight).compressImage()
        val imageId = UUID.randomUUID().toString()
        val imageUrl = storageRepository.uploadImage(
            byteArray,
            imageId,
            StorageRepository.ANALYSIS_IMAGE_REFERENCE
        ).getOrThrow()
        saveImageAnalysisToRoom(imageId, imageUrl, title, imageType)
        imageId
    }

    private suspend fun saveImageAnalysisToRoom(
        imageId: String,
        imageUrl: String,
        title: String,
        imageType: ImageType
    ) {
        val imageAnalysis = ImageAnalysis(
            id = UUID.randomUUID().toString(),
            imageId = imageId,
            imageUrl = imageUrl,
            title = title,
            imageType = imageType.name,
            messages = emptyList(),
            date = LocalDateTime.now()
        )
        imageAnalysisRoomRepository.saveImageAnalysis(imageAnalysis).getOrThrow()
    }
}
