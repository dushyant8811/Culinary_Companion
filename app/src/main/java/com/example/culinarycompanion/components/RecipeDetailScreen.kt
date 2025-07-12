package com.example.culinarycompanion.components

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.Review
import com.example.culinarycompanion.screens.CookingModeScreen
import com.example.culinarycompanion.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    viewModel: AppViewModel,
    navController: NavController
) {
    val collections by viewModel.collections.collectAsState()
    val downloadedIds by viewModel.downloadedRecipeIds.collectAsState()
    val isReadingAloud by viewModel.isReadingAloud.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val reviewsAreLoading by viewModel.reviewsAreLoading.collectAsState()
    val isDownloaded = downloadedIds.contains(recipe.id)
    var showCollectionsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(recipe.id) {
        viewModel.loadReviewsFor(recipe.id)
    }

    if (isReadingAloud) {
        CookingModeScreen(
            viewModel = viewModel,
            onStop = { viewModel.stopReading() }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(recipe.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val appLink = "https://play.google.com/store/apps/details?id=${context.packageName}"
                            val shareText = """
                                Check out this recipe: *${recipe.title}*

                                *Description:*
                                ${recipe.description}

                                See the full recipe in the Culinary Companion app!
                                Download here: $appLink
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Recipe")
                            context.startActivity(shareIntent)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Recipe")
                        }
                        IconButton(onClick = { viewModel.toggleDownload(recipe) }) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.FileDownload,
                                contentDescription = "Download Recipe",
                                tint = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { viewModel.toggleFavorite(recipe, !recipe.isFavorite) }) {
                            Icon(
                                imageVector = if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                                contentDescription = "Favorite",
                                tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { showCollectionsDialog = true },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Add to Collection") },
                        text = { Text("Add to Collection") }
                    )
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.startReading(recipe) },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Start Cooking") },
                        text = { Text("Start Cooking") }
                    )
                }
            }
        ) { innerPadding ->

            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(bottom = 120.dp)
            ) {
                item {
                    Column {
                        recipe.imageUrl?.let { url ->
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = "Recipe Image",
                                modifier = Modifier.fillMaxWidth().height(250.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "By ${recipe.author}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                        if (recipe.description.isNotBlank()) {
                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            InfoChip(label = "Prep", value = "${recipe.prepTime} min")
                            InfoChip(label = "Cook", value = "${recipe.cookTime} min")
                            InfoChip(label = "Serves", value = recipe.servings.toString())
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionTitle("Ingredients")
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            recipe.ingredients.forEach { ingredient ->
                                Text("• $ingredient", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionTitle("Instructions")
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            recipe.instructions.forEachIndexed { index, step ->
                                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text("${index + 1}. ", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text(step, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    ReviewSubmissionSection(
                        onSubmit = { rating, text ->
                            viewModel.submitReview(recipe.id, rating, text)
                        }
                    )
                    SectionTitle("Reviews (${recipe.reviewCount})")
                }

                if (reviewsAreLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (reviews.isEmpty()) {
                    item {
                        Text(
                            "No reviews yet. Be the first!",
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(reviews, key = { it.id }) { review ->
                        ReviewItem(review)
                    }
                }
            }

            if (showCollectionsDialog) {
                AlertDialog(
                    onDismissRequest = { showCollectionsDialog = false },
                    title = { Text("Add to Collection") },
                    text = {
                        LazyColumn {
                            if (collections.isEmpty()) {
                                item { Text("No collections yet. Create one first.") }
                            } else {
                                items(collections) { collection ->
                                    val containsRecipe = recipe.id in collection.recipeIds
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (!containsRecipe) {
                                                    viewModel.addToCollection(recipe, collection.id)
                                                }
                                                showCollectionsDialog = false
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (containsRecipe) Icons.Default.Check else Icons.Default.Folder,
                                            contentDescription = "Collection Icon",
                                            tint = if (containsRecipe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(collection.name, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            navController.navigate("collections")
                            showCollectionsDialog = false
                        }) {
                            Text("Manage Collections")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCollectionsDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

// --- ALL HELPER COMPOSABLES ---

@Composable
fun ReviewSubmissionSection(onSubmit: (Float, String) -> Unit) {
    var rating by remember { mutableStateOf(0f) }
    var reviewText by remember { mutableStateOf("") }
    val isSubmittable = rating > 0 && reviewText.isNotBlank()

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Leave a Review", style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Your Rating: ", style = MaterialTheme.typography.bodyLarge)
            (1..5).forEach { starIndex ->
                IconButton(onClick = { rating = starIndex.toFloat() }) {
                    Icon(
                        imageVector = if (starIndex <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Star $starIndex",
                        tint = if (starIndex <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("Write your review...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            singleLine = false
        )
        Button(
            onClick = {
                onSubmit(rating, reviewText)
                rating = 0f
                reviewText = ""
            },
            enabled = isSubmittable,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(review.authorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    (1..5).forEach {
                        Icon(
                            imageVector = if (it <= review.rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Text(
                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(review.timestamp),
                style = MaterialTheme.typography.labelSmall
            )
            Text(review.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}