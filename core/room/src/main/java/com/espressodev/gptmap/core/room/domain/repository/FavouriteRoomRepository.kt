package com.espressodev.gptmap.core.room.domain.repository

import com.espressodev.gptmap.core.model.Favourite
import kotlinx.coroutines.flow.Flow

interface FavouriteRoomRepository {
    suspend fun saveFavourite(favourite: Favourite): Result<Unit>
    fun getFavourites(): Flow<List<Favourite>>
    suspend fun getFavourite(id: String): Favourite?
    suspend fun deleteFavourite(favouriteId: String): Result<Unit>
    suspend fun updateFavouriteText(favouriteId: String, text: String): Result<Unit>
    suspend fun updateImageAnalysisId(
        favouriteId: String, 
        messageId: String, 
        imageAnalysisId: String
    ): Result<Unit>
    suspend fun resetImageAnalysisId(imageAnalysisId: String): Result<Unit>
}