package com.example.culinarycompanion.model

import com.google.firebase.firestore.PropertyName

data class CommunityPost(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("authorId")
    @set:PropertyName("authorId")
    var authorId: String = "",

    @get:PropertyName("authorName")
    @set:PropertyName("authorName")
    var authorName: String = "",

    @get:PropertyName("authorProfileImageUrl")
    @set:PropertyName("authorProfileImageUrl")
    var authorProfileImageUrl: String? = null,

    @get:PropertyName("postImageUrl")
    @set:PropertyName("postImageUrl")
    var postImageUrl: String = "",

    @get:PropertyName("caption")
    @set:PropertyName("caption")
    var caption: String = "",

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis(),

    @get:PropertyName("likeCount")
    @set:PropertyName("likeCount")
    var likeCount: Long = 0,

    @get:PropertyName("commentCount")
    @set:PropertyName("commentCount")
    var commentCount: Long = 0
)