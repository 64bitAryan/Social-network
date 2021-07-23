package com.ryan.repositories

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ryan.data.entities.Comment
import com.ryan.data.entities.Post
import com.ryan.data.entities.ProfileUpdate
import com.ryan.data.entities.User
import com.ryan.socialnetwork.other.Constants.DEFAULT_PROFILE_PICTURE_URL
import com.ryan.socialnetwork.other.Resource
import com.ryan.socialnetwork.other.safeCall
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.util.*

@ActivityScoped
class DefaultMainRepository: MainRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage
    private val users = firestore.collection("users")
    private val posts = firestore.collection("posts")
    private val comments = firestore.collection("comments")

    override suspend fun createPost(imageUri: Uri, text: String) = withContext(Dispatchers.IO) {
            safeCall {
                val uid = auth.uid!!
                val postId = UUID.randomUUID().toString()
                val imageUploadResult = storage.getReference(postId).putFile(imageUri).await()
                val imageUrl =
                    imageUploadResult?.metadata?.reference?.downloadUrl?.await().toString()
                val post = Post(
                    id = postId,
                    authorUid = uid,
                    text = text,
                    imageUrl = imageUrl,
                    date = System.currentTimeMillis()
                )
                posts.document(postId).set(post).await()
                Resource.Success(Any())
            }
        }

    override suspend fun getUsers(uids: List<String>) = withContext(Dispatchers.IO) {
        safeCall {
            // Query the user from Firebase using uid, order by username and convert that as our User class
            val usersList = users.whereIn("uid",uids).orderBy("username").get().await()
                .toObjects(User::class.java)
            Resource.Success(usersList)
        }
    }

    override suspend fun getUser(uid: String) = withContext(Dispatchers.IO) {
        safeCall{
            val user = users.document(uid).get().await().toObject(User::class.java)
                ?: throw IllegalStateException()
            val currentUid = FirebaseAuth.getInstance().uid!!
            val currentUser = users.document(currentUid).get().await().toObject(User::class.java)
                ?: throw IllegalStateException()
            user.isFollowing = uid in currentUser.follows
            Resource.Success(user)
        }
    }

    override suspend fun getPostForFollows() = withContext(Dispatchers.IO) {
        safeCall {
            val uid = FirebaseAuth.getInstance().uid!!
            val follows = getUser(uid).data!!.follows
            val allPosts = posts.whereIn("authorUid", follows)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Post::class.java)
                //Manually assigning the authorProfilePicture, authorUserName and isLiked on Each post
                .onEach { post ->
                    val user = getUser(post.authorUid).data!!
                    post.authorProfilePicture = user.profilePictureUrl
                    post.authorUsername = user.userName
                    post.isLiked = uid in post.likedBy
                }
            Resource.Success(allPosts)
        }
    }

    override suspend fun toggleLikeForPosts(post: Post) = withContext(Dispatchers.IO) {
        safeCall{
            var isLiked = false
            firestore.runTransaction { transaction ->
                val uid = FirebaseAuth.getInstance().uid!!
                val postResult = transaction.get(posts.document(post.id))
                val currentLikes = postResult.toObject(Post::class.java)?.likedBy ?: listOf()
                transaction.update(posts.document(post.id), "likedBy",
                    if(uid in currentLikes)
                        currentLikes - uid
                    else {
                        currentLikes + uid
                        isLiked = true
                    }
                )
            }.await()
            Resource.Success(isLiked)
        }
    }

    override suspend fun getPostForProfile(uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            val profilePosts = posts.whereEqualTo("authorUid", uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Post::class.java)
                .onEach { post ->
                    val user = getUser(post.authorUid).data!!
                    post.authorProfilePicture = user.profilePictureUrl
                    post.authorUsername = user.userName
                    post.isLiked = uid in post.likedBy
                }
            Resource.Success(profilePosts)
        }
    }

    override suspend fun deletePost(post: Post) = withContext(Dispatchers.IO) {
        safeCall {
            posts.document(post.id).delete().await()
            storage.getReferenceFromUrl(post.imageUrl).delete().await()
            Resource.Success(post)
        }
    }

    override suspend fun toggleFollowForUser(uid: String) = withContext(Dispatchers.IO) {
        safeCall{
            var isFollowing = false
            firestore.runTransaction { transaction->
                val currentUid = auth.uid!!
                val currentUser = transaction.get(users.document(currentUid)).toObject(User::class.java)!!
                isFollowing = uid in currentUser.follows
                val newFollows = if(isFollowing) currentUser.follows - uid else currentUser.follows + uid
                transaction.update(users.document(currentUid), "follows", newFollows)
            }.await()
            Resource.Success(!isFollowing)
        }
    }

    override suspend fun searchUser(query: String) = withContext(Dispatchers.IO) {
        safeCall {
            val userResult = users.whereGreaterThanOrEqualTo("userName", query.uppercase(Locale.ROOT))
                .get().await().toObjects(User::class.java)
            Resource.Success(userResult)
        }
    }

    override suspend fun createComment(commentText: String, postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val uid = auth.uid!!
            val commentId = UUID.randomUUID().toString()
            val user = getUser(uid).data!!
            val comment = Comment(
                commentId,
                postId,
                uid,
                user.userName,
                user.profilePictureUrl,
                commentText
            )
            comments.document(commentId).set(comment).await()
            Resource.Success(comment)
        }
    }

    override suspend fun deleteComment(comment: Comment) = withContext(Dispatchers.IO) {
        safeCall {
            comments.document(comment.commentId).delete().await()
            Resource.Success(comment)
        }
    }

    override suspend fun getCommentFromPost(postId: String) = withContext(Dispatchers.IO) {
        safeCall{
            val commentForPost = comments
                .whereEqualTo("postId", postId)
                .orderBy("data", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(
                    Comment::class.java
                ).onEach { comment ->
                    val user = getUser(auth.uid!!).data!!
                    comment.uesrname = user.userName
                    comment.profilePicture = user.profilePictureUrl
                }
            Resource.Success(commentForPost)
        }
    }

    override suspend fun updateProfilePicture(uid: String, imageUri: Uri) = withContext(Dispatchers.IO) {
            val storageRef = storage.getReference(uid)
            val user = getUser(uid).data!!
            if(user.profilePictureUrl != DEFAULT_PROFILE_PICTURE_URL) {
                storage.getReferenceFromUrl(user.profilePictureUrl).delete().await()
            }
            storageRef.putFile(imageUri).await().metadata?.reference?.downloadUrl?.await()
    }

    override suspend fun updateProfile(profileUpdate: ProfileUpdate) = withContext(Dispatchers.IO) {
        safeCall {
            val imageUrl = profileUpdate.profilePictureUri?.let { uri ->
                updateProfilePicture(profileUpdate.uidToUpdate, uri).toString()
            }
            val map = mutableMapOf(
                "userName" to profileUpdate.username,
                "description" to profileUpdate.description
            )
            imageUrl?.let{ url ->
                map["profilePictureUrl"] = url
            }
            users.document(profileUpdate.uidToUpdate).update(map.toMap()).await()
            Resource.Success(Any())
        }
    }
}