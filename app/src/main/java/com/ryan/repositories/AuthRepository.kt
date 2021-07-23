package com.ryan.repositories

import com.google.firebase.auth.AuthResult
import com.ryan.socialnetwork.other.Resource
import dagger.Provides

interface AuthRepository {

    suspend fun register(email: String, username: String, password: String): Resource<AuthResult>

    suspend fun login(email: String, password: String): Resource<AuthResult>

}