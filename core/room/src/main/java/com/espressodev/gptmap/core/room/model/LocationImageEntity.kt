package com.espressodev.gptmap.core.room.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.espressodev.gptmap.core.model.Content
import com.espressodev.gptmap.core.model.Coordinates
import com.espressodev.gptmap.core.model.Favourite
import com.espressodev.gptmap.core.model.unsplash.LocationImage
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@Entity(
    tableName = "location_images",
    foreignKeys = [
        ForeignKey(
            entity = FavouriteEntity::class,
            parentColumns = ["id"],
            childColumns = ["favouriteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["favouriteId"]), Index(value = ["analysisId"])]
)
data class LocationImageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val favouriteId: String,
    val analysisId: String = "",
    val imageUrl: String,
    val imageAuthor: String = ""
)

data class FavouriteWithImages(
    @Embedded val favourite: FavouriteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "favouriteId"
    )
    val locationImages: List<LocationImageEntity>
)

fun LocationImageEntity.toLocationImage() = LocationImage(
    id = id,
    analysisId = analysisId,
    imageUrl = imageUrl,
    imageAuthor = imageAuthor,
)

fun FavouriteWithImages.toFavourite() = Favourite(
    id = favourite.id,
    favouriteId = favourite.favouriteId,
    title = favourite.title,
    placeholderImageUrl = favourite.placeholderImageUrl,
    locationImages = locationImages.map { it.toLocationImage() },
    content = favourite.content?.toContent() ?: Content(),
    date = Instant.ofEpochMilli(favourite.date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
)

fun LocationImage.toLocationImageEntity(favouriteId: String) = LocationImageEntity(
    id = id,
    favouriteId = favouriteId,
    analysisId = analysisId,
    imageUrl = imageUrl,
    imageAuthor = imageAuthor
)

fun ContentEmbedded.toContent() = Content(
    coordinates = Coordinates(latitude, longitude),
    city = city,
    district = district,
    country = country,
    poeticDescription = poeticDescription,
    normalDescription = normalDescription
)
