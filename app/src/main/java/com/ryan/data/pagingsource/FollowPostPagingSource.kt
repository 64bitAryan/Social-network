package com.ryan.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.ryan.data.entities.Post
import com.ryan.data.entities.User
import kotlinx.coroutines.tasks.await

class FollowPostPagingSource(
    private val db: FirebaseFirestore
): PagingSource<QuerySnapshot, Post>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Post>): QuerySnapshot? {
        TODO("Not yet implemented")
    }

    var firstLoad = true
    lateinit var follows: List<String>

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Post> {
        return try {
            val uid = FirebaseAuth.getInstance().uid!!
            if(firstLoad) {
                follows = db.collection("users")
                    .document(uid)
                    .get()
                    .await()
                    .toObject(User::class.java)
                    ?.follows ?: listOf()
                firstLoad = false
            }
            val curPage = params.key ?: db.collection("posts")
                .whereIn("authorUid", follows)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val lastDocumentSnapshort = curPage.documents[curPage.size()-1]
            val nextPage = db.collection("posts")
                .whereIn("authorUid", follows)
                .orderBy("date", Query.Direction.DESCENDING)
                .startAfter(lastDocumentSnapshort)
                .get()
                .await()
            LoadResult.Page (
                curPage.toObjects(Post::class.java).onEach { post ->
                    val user = db.collection("users").document(uid).get().await().toObject(User::class.java)!!
                    post.authorProfilePicture = user.profilePictureUrl
                    post.authorUsername = user.userName
                    post.isLiked = uid in post.likedBy
                },
                null,
                nextPage
            )

        } catch(e: Exception) {
            LoadResult.Error(e)
        }
    }
}