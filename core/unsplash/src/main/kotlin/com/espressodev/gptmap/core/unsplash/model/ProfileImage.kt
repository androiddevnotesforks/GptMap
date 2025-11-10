package com.espressodev.gptmap.core.unsplash.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileImage(
    val large: String?,
    val medium: String?,
    val small: String?
)