package com.ryan.socialnetwork.di

import com.ryan.repositories.AuthRepository
import com.ryan.repositories.DefaultAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Singleton
    @Provides
    fun provideAuthRepository(): AuthRepository {
        return DefaultAuthRepository()
    }
}