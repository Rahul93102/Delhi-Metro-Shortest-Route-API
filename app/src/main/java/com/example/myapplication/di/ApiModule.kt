package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.api.MetroApiServer
import com.example.myapplication.data.api.SimpleMetroApiServer
import com.example.myapplication.data.repository.MetroRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    
    @Provides
    @Singleton
    fun provideMetroApiServer(metroRepository: MetroRepository): MetroApiServer {
        return MetroApiServer(metroRepository)
    }
    
    @Provides
    @Singleton
    fun provideSimpleMetroApiServer(metroRepository: MetroRepository): SimpleMetroApiServer {
        return SimpleMetroApiServer(metroRepository)
    }
} 