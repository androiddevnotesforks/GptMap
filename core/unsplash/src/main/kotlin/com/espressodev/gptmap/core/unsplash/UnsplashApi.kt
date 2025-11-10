package com.espressodev.gptmap.core.unsplash

import com.espressodev.gptmap.core.unsplash.model.UnsplashResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApi {
    @GET("search/photos")
    suspend fun getTwoPhotos(
        @Query("query") query: String,
    ): UnsplashResponse
}
