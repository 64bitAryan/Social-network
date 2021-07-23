package com.ryan.data.entities

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val uid: String = "",
    @get:Exclude
    var uesrname: String = "",
    @get:Exclude
    var profilePicture: String = "",
    val comment: String = "",
    val data: Long = System.currentTimeMillis()
) {
}