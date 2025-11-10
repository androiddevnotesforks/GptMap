package com.espressodev.gptmap.core.room.di

import com.espressodev.gptmap.core.room.data.repository.FavouriteRoomRepositoryImpl
import com.espressodev.gptmap.core.room.data.repository.ImageAnalysisRoomRepositoryImpl
import com.espressodev.gptmap.core.room.data.repository.ImageMessageRoomRepositoryImpl
import com.espressodev.gptmap.core.room.domain.repository.FavouriteRoomRepository
import com.espressodev.gptmap.core.room.domain.repository.ImageAnalysisRoomRepository
import com.espressodev.gptmap.core.room.domain.repository.ImageMessageRoomRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindImageMessageRepository(
        impl: ImageMessageRoomRepositoryImpl
    ): ImageMessageRoomRepository

    @Binds
    @Singleton
    abstract fun bindImageAnalysisRepository(
        impl: ImageAnalysisRoomRepositoryImpl
    ): ImageAnalysisRoomRepository

    @Binds
    @Singleton
    abstract fun bindFavouriteRepository(
        impl: FavouriteRoomRepositoryImpl
    ): FavouriteRoomRepository
}
