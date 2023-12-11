package com.espressodev.gptmap.core.palm.module

import com.espressodev.gptmap.core.palm.PalmService
import com.espressodev.gptmap.core.palm.impl.PalmServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object PalmModule {

    @Provides
    @Singleton
    fun providePalmService(palmApi: PalmApi): PalmService = PalmServiceImpl(palmApi)

    @Provides
    @Singleton
    fun provideKtorClient() = HttpClient(Android) {
        install(Logging) {
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    @Provides
    @Singleton
    fun providePalmApi(
        client: HttpClient
    ): PalmApi = PalmApi(client)
}