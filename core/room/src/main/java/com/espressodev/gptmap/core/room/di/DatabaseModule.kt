package com.espressodev.gptmap.core.room.di

import android.content.Context
import androidx.room.Room
import com.espressodev.gptmap.core.room.AppDatabase
import com.espressodev.gptmap.core.room.dao.FavouriteDao
import com.espressodev.gptmap.core.room.dao.ImageAnalysisDao
import com.espressodev.gptmap.core.room.dao.ImageMessageDao
import com.espressodev.gptmap.core.room.dao.LocationImageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideImageAnalysisDao(database: AppDatabase): ImageAnalysisDao {
        return database.imageAnalysisDao()
    }

    @Provides
    fun provideImageMessageDao(database: AppDatabase): ImageMessageDao {
        return database.imageMessageDao()
    }

    @Provides
    fun provideFavouriteDao(database: AppDatabase): FavouriteDao {
        return database.favouriteDao()
    }

    @Provides
    fun provideLocationImageDao(database: AppDatabase): LocationImageDao {
        return database.locationImageDao()
    }
}
