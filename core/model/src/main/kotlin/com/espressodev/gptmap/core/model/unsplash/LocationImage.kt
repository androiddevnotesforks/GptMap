package com.espressodev.gptmap.core.model.unsplash


data class LocationImage(
    val id: String = "",
    val analysisId: String = "",
    val imageUrl: String,
    val imageAuthor: String,
)