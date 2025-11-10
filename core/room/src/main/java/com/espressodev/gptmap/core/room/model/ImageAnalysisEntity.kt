package com.espressodev.gptmap.core.room.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.espressodev.gptmap.core.model.ImageAnalysis
import com.espressodev.gptmap.core.model.ImageMessage
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@Entity(tableName = "image_analysis")
data class ImageAnalysisEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val imageId: String,
    val imageUrl: String,
    val title: String,
    val imageType: String,
    val date: Long = System.currentTimeMillis()
)

data class ImageAnalysisWithMessages(
    @Embedded val imageAnalysis: ImageAnalysisEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "imageAnalysisId"
    )
    val messages: List<ImageMessageEntity>
)

fun ImageAnalysisEntity.toImageAnalysis(messages: List<ImageMessage>) = ImageAnalysis(
    id = id,
    imageId = imageId,
    imageUrl = imageUrl,
    title = title,
    imageType = imageType,
    messages = messages,
    date = Instant.ofEpochMilli(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
)

fun ImageAnalysisWithMessages.toImageAnalysis() = ImageAnalysis(
    id = imageAnalysis.id,
    imageId = imageAnalysis.imageId,
    imageUrl = imageAnalysis.imageUrl,
    title = imageAnalysis.title,
    imageType = imageAnalysis.imageType,
    messages = messages.map { it.toImageMessage() },
    date = Instant.ofEpochMilli(imageAnalysis.date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
)

fun ImageAnalysis.sortByDate(): ImageAnalysis {
    return this.copy(messages = messages.sortedBy { it.date })
}