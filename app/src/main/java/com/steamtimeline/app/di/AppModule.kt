package com.steamtimeline.app.di

import android.content.Context
import androidx.room.Room
import com.steamtimeline.app.data.local.SteamDatabase
import com.steamtimeline.app.data.local.dao.GameSessionDao
import com.steamtimeline.app.data.remote.SteamApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    fun provideSteamApiService(okHttpClient: OkHttpClient): SteamApiService =
        Retrofit.Builder()
            .baseUrl("https://api.steampowered.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SteamApiService::class.java)

    @Provides
    @Singleton
    fun provideSteamDatabase(@ApplicationContext context: Context): SteamDatabase =
        Room.databaseBuilder(context, SteamDatabase::class.java, "steam_timeline.db").build()

    @Provides
    @Singleton
    fun provideGameSessionDao(db: SteamDatabase): GameSessionDao = db.gameSessionDao()
}
