package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.manager.FileManagerScreen
import com.example.ui.manager.FileManagerViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewer.CommonViewerScaffold
import com.example.ui.viewer.ViewerUiState
import com.example.ui.viewer.ViewerViewModel

class MainActivity : ComponentActivity() {

    private val fileManagerViewModel: FileManagerViewModel by viewModels()
    private val viewerViewModel: ViewerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming intent (e.g. user clicked file in external file manager or shared it)
        handleIncomingIntent(intent)

        setContent {
            val managerUiState by fileManagerViewModel.uiState.collectAsState()
            val recentFiles by fileManagerViewModel.recentFiles.collectAsState()
            val favoriteFiles by fileManagerViewModel.favoriteFiles.collectAsState()
            val viewerUiState by viewerViewModel.uiState.collectAsState()

            val isDark = managerUiState.isDarkTheme ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (viewerUiState is ViewerUiState.Idle) {
                        FileManagerScreen(
                            viewModel = fileManagerViewModel,
                            recentFiles = recentFiles,
                            favoriteFiles = favoriteFiles,
                            uiState = managerUiState,
                            isDark = isDark,
                            onToggleTheme = {
                                fileManagerViewModel.toggleTheme(isDark)
                            },
                            onOpenFile = { fileItem ->
                                viewerViewModel.openFile(fileItem)
                            }
                        )
                    } else {
                        CommonViewerScaffold(
                            uiState = viewerUiState,
                            viewModel = viewerViewModel,
                            isDark = isDark,
                            onToggleTheme = {
                                fileManagerViewModel.toggleTheme(isDark)
                            },
                            onBack = {
                                viewerViewModel.closeViewer()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        val uri: Uri? = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
            }
            else -> null
        }

        if (uri != null) {
            val fileItem = fileManagerViewModel.resolveUriToFileItem(uri)
            viewerViewModel.openFile(fileItem)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val isCtrl = event.isCtrlPressed
            when {
                // Ctrl + F: Toggle Search
                isCtrl && event.keyCode == KeyEvent.KEYCODE_F -> {
                    val state = viewerViewModel.uiState.value
                    if (state is ViewerUiState.Ready) {
                        viewerViewModel.toggleSearch()
                        return true
                    }
                }
                // Ctrl + T: Toggle Dark/Light Theme
                isCtrl && event.keyCode == KeyEvent.KEYCODE_T -> {
                    val currentDark = fileManagerViewModel.uiState.value.isDarkTheme ?: false
                    fileManagerViewModel.toggleTheme(currentDark)
                    return true
                }
                // Ctrl + W: Close Viewer
                isCtrl && event.keyCode == KeyEvent.KEYCODE_W -> {
                    val state = viewerViewModel.uiState.value
                    if (state !is ViewerUiState.Idle) {
                        viewerViewModel.closeViewer()
                        return true
                    }
                }
                // Ctrl + Plus / Equals: Zoom In
                isCtrl && (event.keyCode == KeyEvent.KEYCODE_PLUS || event.keyCode == KeyEvent.KEYCODE_EQUALS) -> {
                    viewerViewModel.zoomIn()
                    return true
                }
                // Ctrl + Minus: Zoom Out
                isCtrl && event.keyCode == KeyEvent.KEYCODE_MINUS -> {
                    viewerViewModel.zoomOut()
                    return true
                }
                // F11: Fullscreen
                event.keyCode == KeyEvent.KEYCODE_F11 -> {
                    viewerViewModel.toggleFullscreen()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
