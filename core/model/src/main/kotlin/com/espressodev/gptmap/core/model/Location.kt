package com.espressodev.gptmap.core.model

import androidx.compose.runtime.Stable
import com.espressodev.gptmap.core.model.unsplash.LocationImage
import java.time.LocalDateTime
import java.util.UUID

@Stable
data class Location(
    val id: String,
    val content: Content,
    val locationImages: List<LocationImage>,
    val isAddedToFavourite: Boolean,
    val favouriteId: String
)

val locationDefault: Location =
    Location(
        id = "", content = Content(),
        locationImages = List(2) { LocationImage(imageUrl = "", imageAuthor = "") },
        isAddedToFavourite = true,
        favouriteId = ""
    )

fun Location.toFavourite(placeholderImageUrl: String = ""): Favourite = Favourite(
    id = id.ifEmpty { UUID.randomUUID().toString() },
    favouriteId = favouriteId.ifEmpty { UUID.randomUUID().toString() },
    title = content.city.ifEmpty { content.country },
    placeholderImageUrl = placeholderImageUrl,
    locationImages = locationImages,
    content = content,
    date = LocalDateTime.now()
)
