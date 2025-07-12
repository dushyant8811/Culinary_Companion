package com.example.culinarycompanion.model

import com.google.firebase.firestore.PropertyName

data class PostComment(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",
    
    @get:PropertyName("authorId")
    @set:PropertyName("authorId")
    var authorId: String = "",

    @get:PropertyName("authorName")
    @set:PropertyName("authorName")
    var authorName: String = "Anonymous",

    @get:PropertyName("text")
    @set:PropertyName("text")
    var text: String = "",

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()
)