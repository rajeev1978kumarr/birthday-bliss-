package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.BirthdayCard
import com.example.ui.components.BirthdayCardVisual
import com.example.ui.components.CelebrationEffect
import com.example.ui.components.CelebrationStageCanvas
import com.example.ui.components.ConfettiBurstOverlay
import com.example.ui.theme.PacificoFontFamily
import com.example.ui.viewmodel.StudioSection
import com.example.util.BirthdayMusicPlayer
import com.example.util.CardShareHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private enum class ViewerTab(val label: String) {
    CARD("Card"),
    QUIZ("Quiz"),
    LETTER("Letter"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardViewerScreen(
    cardFlow: Flow<BirthdayCard?>,
    onBack: () -> Unit,
    onEdit: (String, StudioSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val card by cardFlow.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val musicPlayer = remember { BirthdayMusicPlayer() }

    var isOpened by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(ViewerTab.CARD) }
    var isPlayingMusic by remember { mutableStateOf(false) }
    var burstTrigger by remember { mutableIntStateOf(0) }
    var activeEffects by remember {
        mutableStateOf(setOf(CelebrationEffect.CONFETTI, CelebrationEffect.SPARKLES))
    }

    DisposableEffect(Unit) {
        onDispose { musicPlayer.stop() }
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isOpened) 1f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardRevealScale",
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (isOpened) {
            CelebrationStageCanvas(
                activeEffects = activeEffects,
                allowBalloonPop = true,
                modifier = Modifier.matchParentSize(),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        musicPlayer.stop()
                        onBack()
                    },
                    modifier = Modifier.testTag("viewer_back_button"),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "Birthday Surprise",
                    style = MaterialTheme.typography.titleLarge,
                )

                Row {
                    IconButton(
                        onClick = {
                            if (isPlayingMusic) {
                                musicPlayer.stop()
                                isPlayingMusic = false
                            } else {
                                isPlayingMusic = true
                                musicPlayer.playHappyBirthday(scope) {
                                    isPlayingMusic = false
                                }
                            }
                        },
                        modifier = Modifier.testTag("viewer_music_toggle"),
                    ) {
                        Icon(
                            imageVector = if (isPlayingMusic) Icons.Default.MusicNote else Icons.Default.MusicOff,
                            contentDescription = if (isPlayingMusic) {
                                stringResource(R.string.action_stop_chime)
                            } else {
                                stringResource(R.string.action_play_chime)
                            },
                            tint = if (isPlayingMusic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    card?.let { current ->
                        IconButton(
                            onClick = {
                                musicPlayer.stop()
                                val targetSection = when (activeTab) {
                                    ViewerTab.CARD -> StudioSection.CARD_PHOTO
                                    ViewerTab.QUIZ -> StudioSection.QUIZ
                                    ViewerTab.LETTER -> StudioSection.LETTER
                                }
                                onEdit(current.id, targetSection)
                            },
                            modifier = Modifier.testTag("viewer_edit_button"),
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit card")
                        }
                    }
                }
            }

            if (card == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Loading birthday surprise...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val loadedCard = card!!
                val quizQuestions = loadedCard.quizQuestions

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    AnimatedContent(
                        targetState = isOpened,
                        transitionSpec = {
                            (fadeIn() + scaleIn(initialScale = 0.85f)) togetherWith fadeOut()
                        },
                        label = "giftRevealTransition",
                    ) { opened ->
                        if (!opened) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 28.dp)
                                    .clickable {
                                        isOpened = true
                                        burstTrigger++
                                        isPlayingMusic = true
                                        musicPlayer.playHappyBirthday(scope) {
                                            isPlayingMusic = false
                                        }
                                    }
                                    .testTag("open_surprise_card_seal"),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(84.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CardGiftcard,
                                                contentDescription = "Open birthday gift",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(42.dp),
                                            )
                                        }
                                    }
                                    Text(
                                        text = "A Special Surprise For ${loadedCard.recipientName.ifBlank { "You" }}!",
                                        fontFamily = PacificoFontFamily,
                                        fontSize = 26.sp,
                                        lineHeight = 34.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Center,
                                    )
                                    val includedItems = buildList {
                                        add("Photo Card")
                                        if (quizQuestions.isNotEmpty()) add("${quizQuestions.size} Quiz Qs")
                                        if (loadedCard.hasLetter()) add("Sealed Letter")
                                        if (loadedCard.hasGiftSurprise()) add("Secret Gift")
                                    }.joinToString(" • ")
                                    Text(
                                        text = "Includes: $includedItems",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                    )
                                    Button(
                                        onClick = {
                                            isOpened = true
                                            burstTrigger++
                                            isPlayingMusic = true
                                            musicPlayer.playHappyBirthday(scope) {
                                                isPlayingMusic = false
                                            }
                                        },
                                        shape = RoundedCornerShape(50),
                                    ) {
                                        Text("Tap to Unwrap")
                                    }
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(top = 6.dp, bottom = 28.dp),
                            ) {
                                // Interactive Surprise Section Tabs: Card | Quiz | Letter
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    FilterChip(
                                        selected = activeTab == ViewerTab.CARD,
                                        onClick = { activeTab = ViewerTab.CARD },
                                        leadingIcon = { Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        label = { Text("Card") },
                                        modifier = Modifier.testTag("viewer_tab_card"),
                                    )
                                    FilterChip(
                                        selected = activeTab == ViewerTab.QUIZ,
                                        onClick = { activeTab = ViewerTab.QUIZ },
                                        leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        label = {
                                            Text(
                                                if (quizQuestions.isNotEmpty()) "Quiz (${quizQuestions.size})"
                                                else "Quiz"
                                            )
                                        },
                                        modifier = Modifier.testTag("viewer_tab_quiz"),
                                    )
                                    FilterChip(
                                        selected = activeTab == ViewerTab.LETTER,
                                        onClick = { activeTab = ViewerTab.LETTER },
                                        leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        label = { Text("Letter") },
                                        modifier = Modifier.testTag("viewer_tab_letter"),
                                    )
                                }

                                when (activeTab) {
                                    ViewerTab.CARD -> {
                                        BirthdayCardVisual(
                                            card = loadedCard,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .scale(cardScale),
                                        )

                                        if (loadedCard.hasGiftSurprise()) {
                                            SecretGiftRevealCard(
                                                giftText = loadedCard.giftSurprise,
                                                onReveal = { burstTrigger++ },
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                            )
                                        }

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                        ) {
                                            CelebrationEffect.entries.forEach { effect ->
                                                val selected = effect in activeEffects
                                                FilterChip(
                                                    selected = selected,
                                                    onClick = {
                                                        activeEffects = if (selected) activeEffects - effect else activeEffects + effect
                                                    },
                                                    label = { Text(effect.label) },
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            FilledTonalButton(
                                                onClick = { burstTrigger++ },
                                                shape = RoundedCornerShape(50),
                                            ) {
                                                Text("More Confetti!")
                                            }
                                            Button(
                                                onClick = {
                                                    scope.launch { CardShareHelper.shareCard(context, loadedCard) }
                                                },
                                                modifier = Modifier.testTag("viewer_share_button"),
                                                shape = RoundedCornerShape(50),
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(stringResource(R.string.action_share_card))
                                            }
                                        }
                                    }

                                    ViewerTab.QUIZ -> {
                                        InteractiveSurpriseQuiz(
                                            questions = quizQuestions,
                                            recipientName = loadedCard.recipientName,
                                            onBurstConfetti = { burstTrigger++ },
                                            onAddQuizQuestions = {
                                                musicPlayer.stop()
                                                onEdit(loadedCard.id, StudioSection.QUIZ)
                                            },
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                        )
                                    }

                                    ViewerTab.LETTER -> {
                                        WaxSealLetterView(
                                            card = loadedCard,
                                            onBreakSeal = { burstTrigger++ },
                                            onEditLetter = {
                                                musicPlayer.stop()
                                                onEdit(loadedCard.id, StudioSection.LETTER)
                                            },
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }

        ConfettiBurstOverlay(burstTrigger = burstTrigger)
    }
}
