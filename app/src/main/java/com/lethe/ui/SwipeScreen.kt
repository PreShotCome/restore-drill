package com.lethe.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lethe.viewmodel.SwipeViewModel
import kotlinx.coroutines.launch

@Composable
fun SwipeScreen(viewModel: SwipeViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val trashLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onTrashConfirmed()
        }
    }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Lethe",
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val remaining = (state.photos.size - state.index).coerceAtLeast(0)
            Text(
                text = "$remaining left  ·  ${state.pendingTrash.size} to trash",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.loading -> CircularProgressIndicator()
                state.photos.isEmpty() -> Text(
                    "No photos found.",
                    color = MaterialTheme.colorScheme.onSurface,
                )
                state.finished -> FinishedView(
                    pending = state.pendingTrash.size,
                    onApply = {
                        viewModel.buildTrashRequest()?.let { pi ->
                            trashLauncher.launch(IntentSenderRequest.Builder(pi).build())
                        }
                    },
                )
                else -> SwipeStack(
                    viewModel = viewModel,
                    onAutoFlush = {
                        viewModel.buildTrashRequest()?.let { pi ->
                            trashLauncher.launch(IntentSenderRequest.Builder(pi).build())
                        }
                    },
                )
            }
        }

        if (!state.finished && !state.loading && state.photos.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                FilledIconButton(
                    onClick = { scope.launch { viewModel.discard() } },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete")
                }
                FilledIconButton(
                    onClick = { scope.launch { viewModel.keep() } },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = "Keep")
                }
            }
        }
    }
}

@Composable
private fun SwipeStack(
    viewModel: SwipeViewModel,
    onAutoFlush: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val current = state.current ?: return
    val next = state.next

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidthDp.dp.toPx() }
    val threshold = screenWidthPx * 0.25f

    val offsetX = remember(current.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f),
    ) {
        if (next != null) {
            PhotoCard(
                photo = next,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { scaleX = 0.95f; scaleY = 0.95f; alpha = 0.6f },
            )
        }

        PhotoCard(
            photo = current,
            swipeOffset = offsetX.value,
            swipeThreshold = threshold,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX.value
                    rotationZ = (offsetX.value / screenWidthPx) * 20f
                }
                .pointerInput(current.id) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                            }
                        },
                        onDragEnd = {
                            val v = offsetX.value
                            when {
                                v > threshold -> scope.launch {
                                    offsetX.animateTo(screenWidthPx * 1.5f, tween(220))
                                    viewModel.keep()
                                }
                                v < -threshold -> scope.launch {
                                    offsetX.animateTo(-screenWidthPx * 1.5f, tween(220))
                                    viewModel.discard()
                                    if (state.pendingTrash.size + 1 >= 60) onAutoFlush()
                                }
                                else -> scope.launch { offsetX.animateTo(0f, spring()) }
                            }
                        },
                    )
                },
        )
    }
}

@Composable
private fun FinishedView(pending: Int, onApply: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "All done.",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (pending > 0) {
            Text(
                "$pending photo${if (pending == 1) "" else "s"} ready to move to Trash.",
                color = MaterialTheme.colorScheme.onSurface,
            )
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                ),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  Move to Trash")
            }
        } else {
            Text("Nothing to trash.", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
