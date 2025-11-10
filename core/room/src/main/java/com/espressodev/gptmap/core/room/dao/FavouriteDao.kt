package com.espressodev.gptmap.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.espressodev.gptmap.core.room.model.FavouriteEntity
import com.espressodev.gptmap.core.room.model.FavouriteWithImages
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(favourite: FavouriteEntity)

    @Transaction
    @Query("SELECT * FROM favourites ORDER BY date DESC")
    fun getFavourites(): Flow<List<FavouriteWithImages>>

    @Transaction
    @Query("SELECT * FROM favourites WHERE favouriteId = :favouriteId LIMIT 1")
    suspend fun getFavourite(favouriteId: String): FavouriteWithImages?

    @Query("DELETE FROM favourites WHERE favouriteId = :favouriteId")
    suspend fun deleteFavourite(favouriteId: String)

    @Query("UPDATE favourites SET title = :text WHERE favouriteId = :favouriteId")
    suspend fun updateFavouriteTitle(favouriteId: String, text: String)

    @Query("DELETE FROM favourites")
    suspend fun deleteAll()
}