package com.espressodev.gptmap.core.model

import com.espressodev.gptmap.core.model.unsplash.LocationImage
import java.time.LocalDateTime

data class Favourite(
    val id: String = "",
    val userId: String = "",
    val favouriteId: String = "",
    val title: String = "",
    val placeholderImageUrl: String = "",
    val locationImages: List<LocationImage> = listOf(),
    val content: Content = Content(),
    val date: LocalDateTime = LocalDateTime.MIN
) {
    val placeholderTitle = "${content.city}, ${content.country}"
    val placeholderCoordinates = "${"%.4f".format(content.coordinates.latitude)}°, ${"%.4f".format(content.coordinates.longitude)}°"

    fun toLocation(): Location = Location(
        id = id,
        content = content,
        locationImages = locationImages,
        isAddedToFavourite = false,
        favouriteId = favouriteId
    )
}
