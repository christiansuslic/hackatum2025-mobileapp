package com.example.insanecrossmobilepingpongapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.insanecrossmobilepingpongapp.model.PlayerRole
import com.example.insanecrossmobilepingpongapp.network.ConnectionState
import org.jetbrains.compose.resources.painterResource
import insanecrossmobilepingpongapp.composeapp.generated.resources.Res
import insanecrossmobilepingpongapp.composeapp.generated.resources.racket_black
import insanecrossmobilepingpongapp.composeapp.generated.resources.racket_red

/**
 * Game screen showing the racket view and connection status.
 */
@Composable
fun GameScreen(
    playerRole: PlayerRole,
    connectionState: ConnectionState,
    isDebugVisible: Boolean,
    onToggleDebug: () -> Unit,
    onDisconnect: () -> Unit,
    debugContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        // Main Content - Racket Display
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Connection Status Indicator
            ConnectionStatusBadge(
                connectionState = connectionState,
                playerRole = playerRole
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Racket Image
            RacketDisplay(
                playerRole = playerRole,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            )
        }

        // Top-Right Controls
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Debug Toggle Button
            FloatingActionButton(
                onClick = onToggleDebug,
                containerColor = if (isDebugVisible) 
                    MaterialTheme.colorScheme.secondary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "Toggle Debug",
                    tint = if (isDebugVisible) Color.White else Color.Gray
                )
            }
        }

        // Bottom Disconnect Button
        Button(
            onClick = onDisconnect,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .widthIn(min = 200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE63946),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Trennen")
        }

        // Debug Overlay
        if (isDebugVisible) {
            debugContent()
        }
    }
}

/**
 * Displays the racket image based on player role.
 */
@Composable
private fun RacketDisplay(
    playerRole: PlayerRole,
    modifier: Modifier = Modifier
) {
    val racketResource = when (playerRole) {
        PlayerRole.PLAYER1 -> Res.drawable.racket_red
        PlayerRole.PLAYER2 -> Res.drawable.racket_black
    }

    Card(
        modifier = modifier,
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Image(
            painter = painterResource(racketResource),
            contentDescription = "${playerRole.displayName} Racket",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Connection status badge showing player and connection state.
 */
@Composable
private fun ConnectionStatusBadge(
    connectionState: ConnectionState,
    playerRole: PlayerRole,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (connectionState) {
        ConnectionState.CONNECTED -> "Verbunden" to Color(0xFF06D6A0)
        ConnectionState.CONNECTING -> "Verbindet..." to Color(0xFFFFA500)
        ConnectionState.DISCONNECTED -> "Getrennt" to Color(0xFF888888)
        ConnectionState.ERROR -> "Fehler" to Color(0xFFE63946)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3E)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = playerRole.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, CircleShape)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor
                )
            }
        }
    }
}
