package com.espressodev.gptmap.core.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.espressodev.gptmap.core.model.ImageMessage
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@Entity(
    tableName = "image_messages",
    foreignKeys = [
        ForeignKey(
            entity = ImageAnalysisEntity::class,
            parentColumns = ["id"],
            childColumns = ["imageAnalysisId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["imageAnalysisId"])]
)
data class ImageMessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val imageAnalysisId: String, // Foreign key
    val request: String,
    val response: String,
    val token: Int,
    val date: Long = System.currentTimeMillis()
)

fun ImageMessageEntity.toImageMessage() = ImageMessage(
    id = id,
    request = request,
    response = response,
    token = token,
    date = Instant.ofEpochMilli(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
)
