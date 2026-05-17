package es.joshluq.authkit.showcase

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.SessionState
import es.joshluq.authkit.session.model.Token
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.authkit.showcase.ui.theme.ShowcaseTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as ShowcaseApp
        
        setContent {
            ShowcaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (app.isInitialized()) {
                        SessionScreen(
                            authKit = app.authKit,
                            preset = app.configManager.getActivePreset()!!,
                            onReset = {
                                app.configManager.clearConfig()
                                restartApp()
                            }
                        )
                    } else {
                        PresetSelectionScreen { preset ->
                            app.configManager.savePreset(preset)
                            restartApp()
                        }
                    }
                }
            }
        }
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetSelectionScreen(onPresetSelected: (SessionPreset) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AuthKit Showcase") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Select a Session Configuration:",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(SessionPreset.entries) { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPresetSelected(preset) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    authKit: AuthKit,
    preset: SessionPreset,
    onReset: () -> Unit
) {
    val state by authKit.session.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // Timer local para la demo visual
    var secondsRemaining by remember { mutableIntStateOf(0) }
    val isTimed = preset.expiration is ExpirationPolicy.Timed
    val initialDuration = if (preset.expiration is ExpirationPolicy.Timed) {
        (preset.expiration.durationMillis / 1000).toInt()
    } else 0

    LaunchedEffect(state) {
        if (!isTimed) {
            secondsRemaining = 0
            return@LaunchedEffect
        }

        when (state) {
            is SessionState.Active -> {
                secondsRemaining = initialDuration
                while (secondsRemaining > 0) {
                    delay(1000)
                    secondsRemaining--
                }
            }
            is SessionState.Idle -> {
                secondsRemaining = 0
            }
            SessionState.ExpiringSoon -> {
                secondsRemaining--
                while (secondsRemaining > 0) {
                    delay(1000)
                    secondsRemaining--
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Session Manager") },
                actions = {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Settings, contentDescription = "Change Config")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card de Configuración Activa
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Active Configuration:", fontWeight = FontWeight.Bold)
                    Text(text = preset.title)
                    Text(
                        text = "Storage: ${preset.persistence.javaClass.simpleName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card de Estado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (state) {
                        is SessionState.Active -> Color(0xFFE8F5E9)
                        is SessionState.ExpiringSoon -> Color(0xFFFFF3E0)
                        else -> Color(0xFFF5F5F5)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Current State:", fontWeight = FontWeight.Medium)
                    Text(
                        text = state.javaClass.simpleName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (state) {
                            is SessionState.Active -> Color(0xFF2E7D32)
                            is SessionState.ExpiringSoon -> Color(0xFFEF6C00)
                            else -> Color.Gray
                        }
                    )
                    
                    if (isTimed && state !is SessionState.Idle) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Approx. time remaining:")
                        Text(
                            text = "${secondsRemaining}s",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else if (!isTimed && state is SessionState.Active) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "✓ Persistent Session", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Acciones
            Button(
                onClick = {
                    scope.launch {
                        val tokens = TokenHolder().apply {
                            addToken(Token.Access("mock_access_token"))
                            addToken(Token.Refresh("mock_refresh_token"))
                        }
                        authKit.session.startSession(tokens)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state is SessionState.Idle
            ) {
                Text("Start Session")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isTimed) {
                Button(
                    onClick = {
                        scope.launch {
                            val tokens = TokenHolder().apply {
                                addToken(Token.Access("new_access_token_${System.currentTimeMillis()}"))
                            }
                            authKit.session.extendSession(tokens)
                            secondsRemaining = initialDuration
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is SessionState.Idle,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
                ) {
                    Text("Extend Session")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = {
                    scope.launch { authKit.session.endSession() }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is SessionState.Idle
            ) {
                Text("End Session")
            }

            Spacer(modifier = Modifier.height(24.dp))

            val keepAlive = remember { authKit.session.keepAlive() }
            
            if (isTimed) {
                Button(
                    onClick = {
                        keepAlive.notifyActivity()
                        secondsRemaining = initialDuration
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is SessionState.Idle,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Simulate Activity (Reset Timer)")
                }
            }
        }
    }
}
