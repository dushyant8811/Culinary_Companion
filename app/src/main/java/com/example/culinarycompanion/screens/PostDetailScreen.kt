package com.example.culinarycompanion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culinarycompanion.components.CommunityPostCard
import com.example.culinarycompanion.model.PostComment
import com.example.culinarycompanion.viewmodel.CommunityViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    viewModel: CommunityViewModel,
    postId: String
) {
    // Find the post from our feed list
    val post by remember {
        derivedStateOf { viewModel.posts.value.find { it.id == postId } }
    }

    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.commentsAreLoading.collectAsState()
    val likedPostIds by viewModel.likedPostIds.collectAsState()

    // Load comments when the screen appears
    LaunchedEffect(postId) {
        viewModel.loadCommentsFor(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post by ${post?.authorName ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Show the original post at the top
            item {
                post?.let {
                    CommunityPostCard(
                        post = it,
                        isLiked = likedPostIds.contains(it.id),
                        onLikeClick = { viewModel.toggleLike(it.id) },
                        onCommentClick = { /* Clicks do nothing here */ }
                    )
                }
            }

            // Show the comment submission form
            item {
                var commentText by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Add a comment...") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.postComment(postId, commentText)
                                commentText = ""
                            },
                            enabled = commentText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, "Post Comment")
                        }
                    }
                )
            }

            // Show the list of comments
            if (isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            } else if (comments.isEmpty()) {
                item { Text("No comments yet.", modifier = Modifier.padding(16.dp)) }
            } else {
                items(comments, key = { it.id }) { comment ->
                    CommentItem(comment) // We can reuse the one from RecipeDetailScreen
                }
            }
        }
    }
}

// You can move this to a shared components file if you want
@Composable
fun CommentItem(comment: PostComment) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(comment.authorName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}