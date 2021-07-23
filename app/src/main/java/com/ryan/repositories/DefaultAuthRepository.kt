package com.ryan.repositories

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ryan.data.entities.User
import com.ryan.socialnetwork.other.Resource
import com.ryan.socialnetwork.other.safeCall
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class DefaultAuthRepository: AuthRepository {

    val auth = FirebaseAuth.getInstance()
    val users = FirebaseFirestore.getInstance().collection("users")

    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): Resource<AuthResult> {
        return withContext(Dispatchers.IO) {
            safeCall {
                //Creating user with email and password
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                //Saving uid of the user
                val uid = result.user?.uid!!
                //Saving user as User data class
                val user = User(uid, username)
                //Adding user document to firestore
                users.document(uid).set(user).await()
                //Returning Resource Success class of type result
                Resource.Success(result)
            }
        }
    }

    override suspend fun login(email: String, password: String): Resource<AuthResult> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                Resource.Success(result)
            }
        }
    }
}