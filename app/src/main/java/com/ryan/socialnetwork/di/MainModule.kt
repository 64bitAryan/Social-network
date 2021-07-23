package com.ryan.socialnetwork.di

import com.ryan.repositories.AuthRepository
import com.ryan.repositories.DefaultAuthRepository
import com.ryan.repositories.DefaultMainRepository
import com.ryan.repositories.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Singleton
    @Provides
    fun provideMainRepository() = DefaultMainRepository() as MainRepository
}