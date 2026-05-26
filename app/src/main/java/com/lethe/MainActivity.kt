package com.lethe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lethe.ui.AlbumPreviewScreen
import com.lethe.ui.HomeScreen
import com.lethe.ui.SwipeScreen
import com.lethe.ui.theme.LetheTheme

private sealed class Route {
    data object Home : Route()
    data class Preview(val bucketId: String, val name: String) : Route()
    data class Swipe(
        val bucketId: String,
        val name: String,
        val startAtId: Long?,
        val skipProcessed: Boolean,
    ) : Route()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            LetheTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    PermissionGate {
                        AppNav()
                    }
                }
            }
        }
    }
}

@Composable
private fun AppNav() {
    var route by remember { mutableStateOf<Route>(Route.Home) }
    when (val r = route) {
        Route.Home -> HomeScreen(
            onAlbumPicked = { id, name -> route = Route.Preview(id, name) },
        )
        is Route.Preview -> AlbumPreviewScreen(
            bucketId = r.bucketId,
            albumName = r.name,
            onBack = { route = Route.Home },
            onStartSwipe = { startAtId, skipProcessed ->
                route = Route.Swipe(r.bucketId, r.name, startAtId, skipProcessed)
            },
        )
        is Route.Swipe -> SwipeScreen(
            bucketId = r.bucketId,
            startAtId = r.startAtId,
            skipProcessed = r.skipProcessed,
            onBack = { route = Route.Preview(r.bucketId, r.name) },
        )
    }
}

private val photoPermission: String
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

@Composable
private fun PermissionGate(content: @Composable () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, photoPermission)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result -> granted = result }

    if (granted) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Lethe needs access to your photos.",
                color = MaterialTheme.colorScheme.onSurface,
            )
            Button(onClick = { launcher.launch(photoPermission) }) {
                Text("Grant access")
            }
        }
    }
}
