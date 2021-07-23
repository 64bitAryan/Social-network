package com.ryan.data.entities

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.ryan.socialnetwork.other.Constants.DEFAULT_PROFILE_PICTURE_URL

@IgnoreExtraProperties
data class User (
    val uid: String = "",
    val userName: String = "",
    val profilePictureUrl: String = DEFAULT_PROFILE_PICTURE_URL,
    val description: String = "",
    var follows: List<String> = listOf(),
    @get:Exclude
    var isFollowing: Boolean = false
)
