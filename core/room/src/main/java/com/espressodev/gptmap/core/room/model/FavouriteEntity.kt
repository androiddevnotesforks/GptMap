package com.espressodev.gptmap.core.room.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val favouriteId: String,
    val title: String,
    val placeholderImageUrl: String = "",
    @Embedded val content: ContentEmbedded? = null,
    val date: Long = System.currentTimeMillis()
)