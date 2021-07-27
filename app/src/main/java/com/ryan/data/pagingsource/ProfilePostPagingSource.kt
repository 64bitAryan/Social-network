package com.ryan.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.ryan.data.entities.Post
import com.ryan.data.entities.User
import kotlinx.coroutines.tasks.await

class ProfilePostPagingSource(
    private val db: FirebaseFirestore,
    private val uid: String
): PagingSource<QuerySnapshot, Post>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Post>): QuerySnapshot? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Post> {
        return try {
            val curPage = params.key ?: db.collection("posts").whereEqualTo("authorUid", uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val lastDocumentSnapshort = curPage.documents[curPage.size()-1]
            val nextPage = db.collection("posts").whereEqualTo("authorUid", uid)
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