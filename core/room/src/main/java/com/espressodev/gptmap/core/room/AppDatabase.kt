package com.espressodev.gptmap.core.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.espressodev.gptmap.core.room.dao.FavouriteDao
import com.espressodev.gptmap.core.room.dao.ImageAnalysisDao
import com.espressodev.gptmap.core.room.dao.ImageMessageDao
import com.espressodev.gptmap.core.room.dao.LocationImageDao
import com.espressodev.gptmap.core.room.model.FavouriteEntity
import com.espressodev.gptmap.core.room.model.ImageAnalysisEntity
import com.espressodev.gptmap.core.room.model.ImageMessageEntity
import com.espressodev.gptmap.core.room.model.LocationImageEntity


@Database(
    entities = [
        ImageAnalysisEntity::class,
        ImageMessageEntity::class,
        FavouriteEntity::class,
        LocationImageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageAnalysisDao(): ImageAnalysisDao
    abstract fun imageMessageDao(): ImageMessageDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun locationImageDao(): LocationImageDao
}
