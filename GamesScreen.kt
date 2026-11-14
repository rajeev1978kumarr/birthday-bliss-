package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GameScore
import com.example.data.model.PartyGameIds
import com.example.ui.screens.games.BalloonPopGame
import com.example.ui.screens.games.CandleBlowGame
import com.example.ui.screens.games.PinataSmashGame
import com.example.ui.viewmodel.GamesViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun GamesScreen(
    viewModel: GamesViewModel,
    modifier: Modifier = Modifier,
) {
    val scoresState by viewModel.scoresState.collectAsStateWithLifecycle()
    val scores = (scoresState as? UiState.Success)?.data ?: emptyMap()
    var activeGameId by rememberSaveable { mutableStateOf<String?>(null) }

    if (activeGameId != null) {
        BackHandler { activeGameId = null }
    }

    when (activeGameId) {
        PartyGameIds.BALLOON_POP -> {
            BalloonPopGame(
                bestScore = scores[PartyGameIds.BALLOON_POP]?.bestScore?.toInt() ?: 0,
                onGameFinished = { viewModel.recordScore(PartyGameIds.BALLOON_POP, it) },
                onBack = { activeGameId = null },
                modifier = modifier,
            )
        }
        PartyGameIds.CANDLE_BLOW -> {
            CandleBlowGame(
                bestScore = scores[PartyGameIds.CANDLE_BLOW]?.bestScore?.toInt() ?: 0,
                onGameFinished = { viewModel.recordScore(PartyGameIds.CANDLE_BLOW, it) },
                onBack = { activeGameId = null },
                modifier = modifier,
            )
        }
        PartyGameIds.PINATA_SMASH -> {
            PinataSmashGame(
                bestScore = scores[PartyGameIds.PINATA_SMASH]?.bestScore?.toInt() ?: 0,
                onGameFinished = { viewModel.recordScore(PartyGameIds.PINATA_SMASH, it) },
                onBack = { activeGameId = null },
                modifier = modifier,
            )
        }
        else -> {
            GamesHub(
                scores = scores,
                onSelectGame = { activeGameId = it },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun GamesHub(
    scores: Map<String, GameScore>,
    onSelectGame: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column {
                    Text(
                        text = "Party Mini-Games",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Play festive birthday challenges & beat your personal best!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                GameCard(
                    title = "Balloon Pop Blitz",
                    subtitle = "30s fast-tap arcade! Pop colorful & golden balloons while dodging storm clouds.",
                    icon = Icons.Default.Celebration,
                    gradient = listOf(Color(0xFFD81B60), Color(0xFFFF758C)),
                    scoreEntity = scores[PartyGameIds.BALLOON_POP],
                    testTag = "play_balloon_pop",
                    onPlay = { onSelectGame(PartyGameIds.BALLOON_POP) },
                )
            }

            item {
                GameCard(
                    title = "Candle Blow-Out",
                    subtitle = "Extinguish all 7 flickering flames on the birthday cake before embers relight them!",
                    icon = Icons.Default.Cake,
                    gradient = listOf(Color(0xFF7E22CE), Color(0xFFD946EF)),
                    scoreEntity = scores[PartyGameIds.CANDLE_BLOW],
                    testTag = "play_candle_blow",
                    onPlay = { onSelectGame(PartyGameIds.CANDLE_BLOW) },
                )
            }

            item {
                GameCard(
                    title = "Pinata Smash",
                    subtitle = "20s combo tapper! Smash swinging star pinatas open to trigger candy showers.",
                    icon = Icons.Default.AutoAwesome,
                    gradient = listOf(Color(0xFFD97706), Color(0xFFF59E0B)),
                    scoreEntity = scores[PartyGameIds.PINATA_SMASH],
                    testTag = "play_pinata_smash",
                    onPlay = { onSelectGame(PartyGameIds.PINATA_SMASH) },
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun GameCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    scoreEntity: GameScore?,
    testTag: String,
    onPlay: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onPlay)
            .testTag(testTag),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Best: ${scoreEntity?.bestScore ?: 0}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                    Text(
                        text = "Played: ${scoreEntity?.gamesPlayed ?: 0}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(50),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play $title")
            }
        }
    }
}
