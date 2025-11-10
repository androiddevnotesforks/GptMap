package com.espressodev.gptmap.core.unsplash.model

import android.annotation.SuppressLint
import com.espressodev.gptmap.core.model.unsplash.LocationImage
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class UnsplashResponse(
    val results: List<Result>?,
) {
    fun toLocationImageList(): List<LocationImage> =
        results?.map {
            LocationImage(imageUrl = it.urls?.regular.orEmpty(), imageAuthor = it.user?.name.orEmpty())
        }.orEmpty()
}
