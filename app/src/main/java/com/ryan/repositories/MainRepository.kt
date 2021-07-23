package com.ryan.repositories

import android.net.Uri
import com.ryan.data.entities.Comment
import com.ryan.data.entities.Post
import com.ryan.data.entities.ProfileUpdate
import com.ryan.data.entities.User
import com.ryan.socialnetwork.other.Resource

interface MainRepository {
    suspend fun createPost(imageUri: Uri, text: String): Resource<Any>

    suspend fun getUsers(uids: List<String>): Resource<List<User>>

    suspend fun getUser(uid: String): Resource<User>

    suspend fun getPostForFollows(): Resource<List<Post>>

    suspend fun toggleLikeForPosts(post: Post): Resource<Boolean>

    suspend fun deletePost(post: Post): Resource<Post>

    suspend fun getPostForProfile(uid: String): Resource<List<Post>>
    
    suspend fun toggleFollowForUser(uid: String): Resource<Boolean>

    suspend fun searchUser(query: String): Resource<List<User>>

    suspend fun createComment(commentText: String, postId: String): Resource<Comment>

    suspend fun deleteComment(comment: Comment): Resource<Comment>

    suspend fun getCommentFromPost(postId: String): Resource<List<Comment>>

    suspend fun  updateProfile(profileUpdate: ProfileUpdate): Resource<Any>

    suspend fun updateProfilePicture(uid: String, imageUri: Uri): Uri?
}