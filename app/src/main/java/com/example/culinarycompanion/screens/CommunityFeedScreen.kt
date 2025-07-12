package com.example.culinarycompanion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culinarycompanion.components.CommunityPostCard
import com.example.culinarycompanion.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen(
    navController: NavController,
    viewModel: CommunityViewModel
) {
    val posts by viewModel.posts.collectAsState()
    val likedPostIds by viewModel.likedPostIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // --- Use DisposableEffect to manage the listener's lifecycle ---
    // This ensures the listener is active only when the screen is on screen.
    DisposableEffect(Unit) {
        // When the screen is first composed (appears), start listening for posts.
        viewModel.startListeningForPosts()

        onDispose {
            // When the screen is removed from composition (user navigates away), stop listening.
            // This is crucial to prevent memory leaks and unnecessary reads.
            viewModel.stopListeningForPosts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Feed") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("createPost") }) {
                Icon(Icons.Default.Add, "Create Post")
            }
        }
    ) { padding ->
        // Show a single loading indicator when the feed is first loading
        if (isLoading && posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No community posts yet. Be the first!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
            ) {
                items(posts, key = { it.id }) { post ->
                    CommunityPostCard(
                        post = post,
                        isLiked = likedPostIds.contains(post.id),
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onCommentClick = {
                            navController.navigate("postDetail/${post.id}")
                        }
                    )
                }
            }
        }
    }
}