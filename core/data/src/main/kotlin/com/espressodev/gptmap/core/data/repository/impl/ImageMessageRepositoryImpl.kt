package com.espressodev.gptmap.core.data.repository.impl

import android.content.Context
import com.espressodev.gptmap.core.data.repository.ImageMessageRepository
import com.espressodev.gptmap.core.data.util.runCatchingWithContext
import com.espressodev.gptmap.core.gemini.GeminiRepository
import com.espressodev.gptmap.core.model.Constants
import com.espressodev.gptmap.core.model.Exceptions
import com.espressodev.gptmap.core.model.ImageMessage
import com.espressodev.gptmap.core.model.di.Dispatcher
import com.espressodev.gptmap.core.model.di.GmDispatchers.IO
import com.espressodev.gptmap.core.model.ext.readBitmapFromExternalStorage
import com.espressodev.gptmap.core.room.domain.repository.ImageMessageRoomRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class ImageMessageRepositoryImpl @Inject constructor(
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    @param:ApplicationContext private val context: Context,
    private val geminiRepository: GeminiRepository,
    private val imageMessageRoomRepository: ImageMessageRoomRepository,
) : ImageMessageRepository {
    override suspend fun addImageMessage(imageId: String, text: String): Result<Unit> =
        runCatchingWithContext(ioDispatcher) {
            val imageMessage = ImageMessage(
                id = UUID.randomUUID().toString(),
                request = text,
                response = "",
                token = 0,
                date = LocalDateTime.now()
            )

            launch {
                imageMessageRoomRepository.addImageMessageToImageAnalysis(
                    imageAnalysisId = imageId,
                    message = imageMessage
                ).getOrThrow()
            }

            val bitmap =
                context.readBitmapFromExternalStorage(
                    directoryName = Constants.PHONE_IMAGE_DIR,
                    filename = imageId
                )
                    ?: throw Exceptions.FailedToReadBitmapFromExternalStorageException()

            val stringBuilder = StringBuilder()
            var totalToken = 0
            runCatching {
                geminiRepository.getImageDescription(bitmap = bitmap, text = text).getOrThrow()
                    .collect { (chunk, token) ->
                        stringBuilder.append(chunk)
                        totalToken = token
                    }
            }.onFailure {
                stringBuilder.append(it.message)
            }

            val fullResponseText = stringBuilder.toString().trim()
            imageMessageRoomRepository.updateImageMessageInImageAnalysis(
                imageAnalysisId = imageId,
                messageId = imageMessage.id,
                text = fullResponseText,
                token = totalToken
            ).getOrThrow()
        }
}
