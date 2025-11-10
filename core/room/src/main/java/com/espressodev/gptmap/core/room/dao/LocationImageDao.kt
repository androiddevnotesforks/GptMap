package com.espressodev.gptmap.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.espressodev.gptmap.core.room.model.LocationImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationImageDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationImage(image: LocationImageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationImages(images: List<LocationImageEntity>)
    
    @Query("""
        UPDATE location_images 
        SET analysisId = :imageAnalysisId 
        WHERE id = :messageId
    """)
    suspend fun updateImageAnalysisId(messageId: String, imageAnalysisId: String)
    
    @Query("""
        UPDATE location_images 
        SET analysisId = '' 
        WHERE analysisId = :imageAnalysisId
    """)
    suspend fun resetImageAnalysisId(imageAnalysisId: String)
    
    @Query("SELECT * FROM location_images WHERE favouriteId = :favouriteId")
    fun getLocationImages(favouriteId: String): Flow<List<LocationImageEntity>>
    
    @Query("DELETE FROM location_images WHERE favouriteId = :favouriteId")
    suspend fun deleteLocationImages(favouriteId: String)
}
