package com.example.culinarycompanion.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.culinarycompanion.viewmodel.AppViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    viewModel: AppViewModel,
    onStop: () -> Unit
) {
    val recipe by viewModel.currentRecipeForReading.collectAsState()
    val currentStepIndex by viewModel.currentInstructionIndex.collectAsState()
    val context = LocalContext.current

    val currentRecipe = recipe ?: return

    // This launcher starts the system's voice recognition activity and waits for a result.
    // Its definition doesn't change.
    val voiceCommandLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.processVoiceCommandResult(result.data)
        }
    }

    // The intent we want to launch also remains the same.
    val voiceIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening for a command...")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    }

     LaunchedEffect(Unit) {
        viewModel.listenNowEvent.collectLatest { it: Unit ->

            try {
                voiceCommandLauncher.launch(voiceIntent)
            } catch (e: Exception) {

                Toast.makeText(context, "Voice recognition not available.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

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
        },
        floatingActionButton = {
            // The FAB now acts as a manual backup. If the user misses the automatic
            // prompt or it fails, they can tap this to try again.
            FloatingActionButton(
                onClick = {
                    try {
                        voiceCommandLauncher.launch(voiceIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Voice recognition not available.", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Listen Manually")
            }
        }
    ) { padding ->
        // The rest of the UI below does not need any changes.
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

            // On-screen Control Buttons
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