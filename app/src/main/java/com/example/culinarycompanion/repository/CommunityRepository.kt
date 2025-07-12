package com.example.culinarycompanion.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.culinarycompanion.BuildConfig
import com.example.culinarycompanion.model.CommunityPost
import com.example.culinarycompanion.network.ImgbbApiService
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.culinarycompanion.model.PostComment


class CommunityRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val postsCollection = db.collection("posts")

    private val imgbbApiService: ImgbbApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImgbbApiService::class.java)
    }

    // --- This function now sets up a LIVE listener for real-time updates ---
    fun listenForCommunityPosts(onUpdate: (List<CommunityPost>) -> Unit): ListenerRegistration {
        val query = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20) // Listen to the 20 most recent posts

        return query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("CommunityRepo", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val posts = snapshot.toObjects(CommunityPost::class.java)
                Log.d("CommunityRepo", "Real-time update: Received ${posts.size} posts.")
                // Call the provided callback with the fresh list of posts
                onUpdate(posts)
            }
        }
    }

    // This function is for manual pagination if you add a "Load More" button later
    suspend fun getMorePosts(lastVisiblePostTimestamp: Long): List<CommunityPost> = withContext(Dispatchers.IO) {
        try {
            val result = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePostTimestamp)
                .limit(10)
                .get()
                .await()
            return@withContext result.toObjects(CommunityPost::class.java)
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error fetching more posts", e)
            return@withContext emptyList()
        }
    }

    suspend fun createPost(authorId: String, authorName: String, caption: String, imageUri: Uri): CommunityPost = withContext(Dispatchers.IO) {
        val postRef = postsCollection.document()
        val postId = postRef.id
        val imageUrl = uploadPostImage(imageUri)
        val newPost = CommunityPost(
            id = postId,
            authorId = authorId,
            authorName = authorName,
            caption = caption,
            postImageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        postRef.set(newPost).await()
        return@withContext newPost
    }

    private suspend fun uploadPostImage(imageUri: Uri): String {
        val apiKey = BuildConfig.IMG_BB_API_KEY // Remember to paste your key
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes() ?: throw Exception("Cannot read image file.")
        inputStream.close()
        val requestBody = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", "community_post.jpg", requestBody)
        val response = imgbbApiService.uploadImage(apiKey, part)
        if (response.isSuccessful && response.body()?.success == true) {
            response.body()?.data?.url?.let { return it }
        }
        throw Exception("ImgBB upload failed: ${response.errorBody()?.string()}")
    }

    suspend fun toggleLike(postId: String, userId: String) {
        val postRef = postsCollection.document(postId)
        val likeRef = postRef.collection("likes").document(userId)
        db.runTransaction { transaction ->
            val likeSnapshot = transaction.get(likeRef)
            if (likeSnapshot.exists()) {
                transaction.delete(likeRef)
                transaction.update(postRef, "likeCount", FieldValue.increment(-1))
            } else {
                transaction.set(likeRef, mapOf("timestamp" to System.currentTimeMillis()))
                transaction.update(postRef, "likeCount", FieldValue.increment(1))
            }
            null
        }.await()
    }

    suspend fun getLikedPosts(userId: String, postIds: List<String>): Set<String> = withContext(Dispatchers.IO) {
        val likedPostIds = mutableSetOf<String>()
        postIds.forEach { postId ->
            val likeSnapshot = postsCollection.document(postId).collection("likes").document(userId).get().await()
            if (likeSnapshot.exists()) {
                likedPostIds.add(postId)
            }
        }
        return@withContext likedPostIds
    }

    suspend fun getCommentsForPost(postId: String): List<PostComment> = withContext(Dispatchers.IO) {
        try {
            postsCollection.document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get().await().toObjects(PostComment::class.java)
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting comments for post $postId", e)
            emptyList()
        }
    }

    suspend fun postCommentOnPost(postId: String, comment: PostComment) {
        val commentRef = postsCollection.document(postId).collection("comments").document()
        comment.id = commentRef.id
        commentRef.set(comment).await()
        // Also increment the commentCount on the main post document
        postsCollection.document(postId).update("commentCount", FieldValue.increment(1)).await()
    }
}