package com.espressodev.gptmap.core.room.data.repository

import com.espressodev.gptmap.core.model.Favourite
import com.espressodev.gptmap.core.model.di.Dispatcher
import com.espressodev.gptmap.core.model.di.GmDispatchers
import com.espressodev.gptmap.core.room.dao.FavouriteDao
import com.espressodev.gptmap.core.room.dao.LocationImageDao
import com.espressodev.gptmap.core.room.domain.repository.FavouriteRoomRepository
import com.espressodev.gptmap.core.room.model.FavouriteEntity
import com.espressodev.gptmap.core.room.model.LocationImageEntity
import com.espressodev.gptmap.core.room.model.toContentEmbedded
import com.espressodev.gptmap.core.room.model.toFavourite
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.ZoneId
import javax.inject.Inject

class FavouriteRoomRepositoryImpl @Inject constructor(
    private val favouriteDao: FavouriteDao,
    private val locationImageDao: LocationImageDao,
    @param:Dispatcher(GmDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : FavouriteRoomRepository {

    override suspend fun saveFavourite(favourite: Favourite): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val favouriteEntity = FavouriteEntity(
                    id = favourite.id,
                    favouriteId = favourite.favouriteId,
                    title = favourite.title,
                    placeholderImageUrl = favourite.placeholderImageUrl,
                    content = favourite.content.toContentEmbedded(),
                    date = favourite.date.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )

                favouriteDao.insertFavourite(favouriteEntity)

                if (favourite.locationImages.isNotEmpty()) {
                    val locationImages = favourite.locationImages.map { image ->
                        LocationImageEntity(
                            id = image.id,
                            favouriteId = favouriteEntity.id,
                            analysisId = image.analysisId,
                            imageUrl = image.imageUrl,
                            imageAuthor = image.imageAuthor
                        )
                    }
                    locationImageDao.insertLocationImages(locationImages)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun getFavourites(): Flow<List<Favourite>> =
        favouriteDao.getFavourites()
            .map { favouritesList ->
                favouritesList.map { it.toFavourite() }
            }
            .flowOn(ioDispatcher)

    override suspend fun getFavourite(id: String): Favourite? =
        withContext(ioDispatcher) {
            favouriteDao.getFavourite(id)?.toFavourite()
        }

    override suspend fun deleteFavourite(favouriteId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                favouriteDao.deleteFavourite(favouriteId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateFavouriteText(
        favouriteId: String,
        text: String
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            favouriteDao.updateFavouriteTitle(favouriteId, text)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateImageAnalysisId(
        favouriteId: String,
        messageId: String,
        imageAnalysisId: String
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            locationImageDao.updateImageAnalysisId(messageId, imageAnalysisId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetImageAnalysisId(imageAnalysisId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                locationImageDao.resetImageAnalysisId(imageAnalysisId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}