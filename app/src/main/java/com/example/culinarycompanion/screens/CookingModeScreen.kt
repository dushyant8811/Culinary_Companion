package com.example.culinarycompanion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culinarycompanion.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    viewModel: AppViewModel,
    onStop: () -> Unit
) {
    val recipe by viewModel.currentRecipeForReading.collectAsState()
    val currentStepIndex by viewModel.currentInstructionIndex.collectAsState()

    val currentRecipe = recipe ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentRecipe.title) },
                actions = {
                    Text(
                        text = "Step ${currentStepIndex + 1}/${currentRecipe.instructions.size}",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Instruction Text
            Text(
                text = currentRecipe.instructions.getOrElse(currentStepIndex) { "Finished!" },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(32.dp)
                    .wrapContentHeight()
            )

            // Control Buttons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousStep() }, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.SkipPrevious, "Previous Step", modifier = Modifier.fillMaxSize())
                    }
                    IconButton(onClick = { viewModel.repeatStep() }, modifier = Modifier.size(80.dp)) {
                        Icon(Icons.Default.Replay, "Repeat Step", modifier = Modifier.fillMaxSize())
                    }
                    IconButton(onClick = { viewModel.nextStep() }, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.SkipNext, "Next Step", modifier = Modifier.fillMaxSize())
                    }
                }
                Button(
                    onClick = onStop,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text("Stop Cooking Mode")
                }
            }
        }
    }
}