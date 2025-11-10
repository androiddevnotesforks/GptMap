package com.espressodev.gptmap.core.unsplash.module

import android.util.Log
import com.espressodev.gptmap.core.unsplash.BuildConfig
import com.espressodev.gptmap.core.unsplash.BuildConfig.UNSPLASH_BASE_URL
import com.espressodev.gptmap.core.unsplash.UnsplashApi
import com.espressodev.gptmap.core.unsplash.UnsplashRepository
import com.espressodev.gptmap.core.unsplash.impl.UnsplashRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(ViewModelComponent::class)
object UnsplashModule {
    @Provides
    @ViewModelScoped
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(UnsplashAuthInterceptor())
            .build()
    }

    @ViewModelScoped
    @Provides
    fun provideUnsplashApi(okHttpClient: OkHttpClient): UnsplashApi {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(UNSPLASH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnsplashApi::class.java)
    }

    @Provides
    @ViewModelScoped
    fun provideUnsplashDataSource(unsplashApi: UnsplashApi): UnsplashRepository =
        UnsplashRepositoryImpl(unsplashApi)
}


class UnsplashAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithAuth = originalRequest.newBuilder()
            .addHeader("Authorization", "Client-ID ${BuildConfig.ACCESS_KEY}")
            .build()
        return chain.proceed(requestWithAuth)
    }
}
