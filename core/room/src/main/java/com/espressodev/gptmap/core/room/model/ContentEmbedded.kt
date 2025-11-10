package com.espressodev.gptmap.core.room.model

import com.espressodev.gptmap.core.model.Content

data class ContentEmbedded(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: String = "",
    val district: String = "",
    val country: String = "",
    val poeticDescription: String = "",
    val normalDescription: String = ""
)

fun Content.toContentEmbedded() = ContentEmbedded(
    latitude = coordinates.latitude,
    longitude = coordinates.longitude,
    city = city,
    district = district ?: city,
    country = country,
    poeticDescription = poeticDescription,
    normalDescription = normalDescription
)