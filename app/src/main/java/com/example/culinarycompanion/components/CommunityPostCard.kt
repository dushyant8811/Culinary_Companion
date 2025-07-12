package com.example.culinarycompanion.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.culinarycompanion.model.CommunityPost

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // --- Author Info ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // You can add a user's profile picture here later
                // Icon(Icons.Default.Person, contentDescription = "Author")
                // Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- Post Image ---
            Image(
                painter = rememberAsyncImagePainter(post.postImageUrl),
                contentDescription = "Post image by ${post.authorName}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f), // Makes the image a square
                contentScale = ContentScale.Crop
            )

            // --- Caption and Actions ---
            Column(modifier = Modifier.padding(12.dp)) {
                if (post.caption.isNotBlank()) {
                    Text(
                        text = post.caption,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like Button
                    IconToggleButton(
                        checked = isLiked,
                        onCheckedChange = { onLikeClick() }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("${post.likeCount}", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.width(16.dp))

                    // Comment Button
                    IconButton(onClick = onCommentClick) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("${post.commentCount}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}