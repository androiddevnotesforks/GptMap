package com.espressodev.gptmap.core.unsplash.impl

import com.espressodev.gptmap.core.model.unsplash.LocationImage
import com.espressodev.gptmap.core.unsplash.UnsplashApi
import com.espressodev.gptmap.core.unsplash.UnsplashRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Even the data class fields are not null response can make your data class fields null
 * how is this allowed?
 */

class UnsplashRepositoryImpl(private val unsplashApi: UnsplashApi) : UnsplashRepository {
    override suspend fun getTwoPhotos(query: String): Result<List<LocationImage>> =
        withContext(Dispatchers.IO) {
            try {
                val response = unsplashApi.getTwoPhotos(query)
                val images = response.toLocationImageList()

                if (images.isEmpty()) {
                    Result.failure(Throwable(UnsplashApiException()))
                } else {
                    val notNullImages = images.map {
                        it.copy(
                            id = UUID.randomUUID().toString(),
                            analysisId = ""
                        )
                    }
                    Result.success(notNullImages)
                }
            } catch (_: Exception) {
                Result.failure(Throwable(UnsplashApiException()))
            }
        }

    companion object {
        class UnsplashApiException : Exception()
    }
}
