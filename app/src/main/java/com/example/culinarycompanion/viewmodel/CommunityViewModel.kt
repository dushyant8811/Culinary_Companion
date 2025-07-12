package com.example.culinarycompanion.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.culinarycompanion.model.CommunityPost
import com.example.culinarycompanion.repository.CommunityRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.culinarycompanion.model.PostComment

class CommunityViewModel(private val repository: CommunityRepository) : ViewModel() {

    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds = _likedPostIds.asStateFlow()

    private val _comments = MutableStateFlow<List<PostComment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _commentsAreLoading = MutableStateFlow(false)
    val commentsAreLoading = _commentsAreLoading.asStateFlow()

    // This will hold our active listener subscription
    private var postsListener: ListenerRegistration? = null

    // This is called by the UI when the screen becomes visible
    fun startListeningForPosts() {
        if (postsListener != null) return // Already listening

        _isLoading.value = true
        postsListener = repository.listenForCommunityPosts { updatedPosts ->
            // This lambda is called by the repository every time Firestore sends new data
            _posts.value = updatedPosts
            if (updatedPosts.isNotEmpty()) {
                checkLikedStatus(updatedPosts.map { it.id })
            }
            _isLoading.value = false
        }
    }

    // This is called by the UI when the screen is no longer visible
    fun stopListeningForPosts() {
        postsListener?.remove()
        postsListener = null
        Log.d("CommunityViewModel", "Stopped listening for post updates.")
    }

    fun createPost(imageUri: Uri, caption: String) {
        viewModelScope.launch {
            val user = Firebase.auth.currentUser ?: return@launch
            _isLoading.value = true
            try {
                // We no longer need to manually update the list, the listener will do it for us.
                repository.createPost(
                    authorId = user.uid,
                    authorName = user.displayName ?: "Anonymous",
                    caption = caption,
                    imageUri = imageUri
                )
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Failed to create post", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val user = Firebase.auth.currentUser ?: return@launch

            val isCurrentlyLiked = _likedPostIds.value.contains(postId)
            // Optimistic UI update
            if (isCurrentlyLiked) {
                _likedPostIds.update { it - postId }
                _posts.update { posts -> posts.map { if (it.id == postId) it.copy(likeCount = it.likeCount - 1) else it } }
            } else {
                _likedPostIds.update { it + postId }
                _posts.update { posts -> posts.map { if (it.id == postId) it.copy(likeCount = it.likeCount + 1) else it } }
            }

            try {
                repository.toggleLike(postId, user.uid)
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Failed to toggle like", e)
                // Revert on failure
                if (isCurrentlyLiked) _likedPostIds.update { it + postId } else _likedPostIds.update { it - postId }
            }
        }
    }

    private fun checkLikedStatus(postIds: List<String>) {
        viewModelScope.launch {
            val user = Firebase.auth.currentUser ?: return@launch
            if (postIds.isNotEmpty()) {
                val newLikedIds = repository.getLikedPosts(user.uid, postIds)
                _likedPostIds.update { it + newLikedIds }
            }
        }
    }

    fun loadCommentsFor(postId: String) {
        viewModelScope.launch {
            _commentsAreLoading.value = true
            _comments.value = emptyList() // Clear old comments
            try {
                _comments.value = repository.getCommentsForPost(postId)
            } finally {
                _commentsAreLoading.value = false
            }
        }
    }

    fun postComment(postId: String, text: String) {
        viewModelScope.launch {
            val user = Firebase.auth.currentUser ?: return@launch
            try {
                val newComment = PostComment(
                    authorId = user.uid,
                    authorName = user.displayName ?: "Anonymous",
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                repository.postCommentOnPost(postId, newComment)

                // Optimistic UI update
                _comments.update { it + newComment }
                // Also update the comment count on the post in our main list
                _posts.update { posts ->
                    posts.map {
                        if (it.id == postId) it.copy(commentCount = it.commentCount + 1) else it
                    }
                }
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Failed to post comment", e)
            }
        }
    }

    // This is crucial to prevent memory leaks when the user navigates away permanently
    override fun onCleared() {
        super.onCleared()
        stopListeningForPosts()
    }
}