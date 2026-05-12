package es.joshluq.authkit.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
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
import es.joshluq.authkit.session.model.SessionState
import es.joshluq.authkit.session.model.Token
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.authkit.showcase.ui.theme.ShowcaseTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authKit = (application as ShowcaseApp).authKit
        
        enableEdgeToEdge()
        setContent {
            ShowcaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SessionScreen(authKit)
                }
            }
        }
    }
}

@Composable
fun SessionScreen(authKit: AuthKit) {
    val state by authKit.session.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // Timer local para la demo
    var secondsRemaining by remember { mutableIntStateOf(0) }
    
    // Efecto para manejar el contador visual
    LaunchedEffect(state) {
        when (state) {
            is SessionState.Active -> {
                secondsRemaining = 30
                while (secondsRemaining > 0) {
                    delay(1000)
                    secondsRemaining--
                }
            }

            is SessionState.Idle -> {
                secondsRemaining = 0
            }

            SessionState.ExpiringSoon -> {
                while (secondsRemaining > 0) {
                    delay(1000)
                    secondsRemaining--
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AuthKit Showcase",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

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
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Estado de la Sesión:", fontWeight = FontWeight.Medium)
                Text(
                    text = state.javaClass.simpleName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (state) {
                        is SessionState.Active -> Color(0xFF2E7D32)
                        is SessionState.ExpiringSoon -> Color(0xFFEF6C00)
                        else -> Color.Gray
                    }
                )
                
                if (state !is SessionState.Idle) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Tiempo restante aproximado:")
                    Text(
                        text = "${secondsRemaining}s",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
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
                        addToken(Token.Custom("Custom","mock_refresh_token"))
                    }
                    authKit.session.startSession(tokens)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state is SessionState.Idle
        ) {
            Text("Iniciar Sesión (30s)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    val tokens = TokenHolder().apply {
                        addToken(Token.Access("new_access_token_${System.currentTimeMillis()}"))
                    }
                    authKit.session.extendSession(tokens)
                    secondsRemaining = 30 // Reiniciar timer visual
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is SessionState.Idle,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
        ) {
            Text("Extender Sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                scope.launch { authKit.session.endSession() }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is SessionState.Idle
        ) {
            Text("Cerrar Sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactor de Actividad
        val interactionInteractor = remember { authKit.session.interactionInteractor() }
        
        Button(
            onClick = {
                interactionInteractor.notifyActivity()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is SessionState.Idle,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Simular Actividad (Reset Timer)")
        }
    }
}
