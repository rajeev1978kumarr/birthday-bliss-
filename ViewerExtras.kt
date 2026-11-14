package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BirthdayCard
import com.example.data.model.QuizQuestionItem
import com.example.ui.theme.PacificoFontFamily
import com.example.ui.theme.PlayfairFontFamily

@Composable
fun InteractiveSurpriseQuiz(
    questions: List<QuizQuestionItem>,
    recipientName: String,
    onBurstConfetti: () -> Unit,
    onAddQuizQuestions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedAnswers = remember(questions) { mutableStateMapOf<String, Int>() }
    var currentIndex by remember(questions) { mutableIntStateOf(0) }
    var showSummary by remember(questions) { mutableStateOf(false) }

    if (questions.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Default.Quiz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp),
                )
                Text(
                    text = "No Quiz Questions Yet",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Add fun personal questions for ${recipientName.ifBlank { "your loved one" }} to click and discover secret notes!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onAddQuizQuestions,
                    modifier = Modifier.testTag("viewer_add_quiz_button"),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Quiz Questions")
                }
            }
        }
        return
    }

    if (showSummary) {
        val correctCount = questions.count { q -> selectedAnswers[q.id] == q.correctIndex }
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("🏆", fontSize = 48.sp)
                Text(
                    text = "Quiz Complete!",
                    fontFamily = PacificoFontFamily,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "${recipientName.ifBlank { "You" }} matched $correctCount out of ${questions.size}!",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Every answer is special because this quiz was made just for you!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    onClick = {
                        selectedAnswers.clear()
                        currentIndex = 0
                        showSummary = false
                    },
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Quiz Again")
                }
            }
        }
        return
    }

    val currentQ = questions[currentIndex.coerceIn(0, questions.lastIndex)]
    val chosenIdx = selectedAnswers[currentQ.id]
    val isAnswered = chosenIdx != null

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${questions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                }
                Text(
                    text = "Tap an option!",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = currentQ.question,
                fontFamily = PlayfairFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentQ.options.forEachIndexed { idx, optionText ->
                    val isSelected = chosenIdx == idx
                    val isRight = idx == currentQ.correctIndex
                    val bgColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.surface
                        isRight -> Color(0xFFD1FAE5)
                        isSelected -> Color(0xFFFFE4EC)
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    }
                    val borderColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.outline
                        isRight -> Color(0xFF059669)
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(enabled = !isAnswered) {
                                selectedAnswers[currentQ.id] = idx
                                onBurstConfetti()
                            }
                            .testTag("viewer_quiz_option_$idx"),
                        shape = RoundedCornerShape(18.dp),
                        color = bgColor,
                        border = BorderStroke(if (isSelected || (isAnswered && isRight)) 2.dp else 1.dp, borderColor),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = Color(0xFF28161F),
                                modifier = Modifier.weight(1f),
                            )
                            if (isAnswered && isRight) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Best match",
                                    tint = Color(0xFF059669),
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isAnswered) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val headline = if (chosenIdx == currentQ.correctIndex) {
                            "Spot on! 🎉"
                        } else {
                            "Aww, nice guess! 💖"
                        }
                        Text(
                            text = headline,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        if (currentQ.revealNote.isNotBlank()) {
                            Text(
                                text = currentQ.revealNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }

            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentIndex < questions.lastIndex) {
                            currentIndex++
                        } else {
                            onBurstConfetti()
                            showSummary = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("viewer_quiz_next_button"),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(if (currentIndex < questions.lastIndex) "Next Question →" else "See Quiz Finale 🏆")
                }
            }
        }
    }
}

@Composable
fun WaxSealLetterView(
    card: BirthdayCard,
    onBreakSeal: () -> Unit,
    onEditLetter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSealBroken by remember(card.id) { mutableStateOf(false) }

    if (!card.hasLetter()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Default.MailOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp),
                )
                Text(
                    text = "No Letter Written Yet",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Write a heartfelt birthday letter so it opens with an interactive wax seal!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onEditLetter,
                    modifier = Modifier.testTag("viewer_write_letter_button"),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Write Birthday Letter")
                }
            }
        }
        return
    }

    AnimatedContent(
        targetState = isSealBroken,
        transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith fadeOut() },
        label = "waxSealLetter",
        modifier = modifier.fillMaxWidth(),
    ) { opened ->
        if (!opened) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isSealBroken = true
                        onBreakSeal()
                    }
                    .testTag("break_wax_seal_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E6)),
                border = BorderStroke(1.5.dp, Color(0xFFD97706).copy(alpha = 0.45f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFB42318),
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(76.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Wax seal",
                                tint = Color(0xFFFDE68A),
                                modifier = Modifier.size(34.dp),
                            )
                        }
                    }
                    Text(
                        text = "A Letter Sealed With Love",
                        fontFamily = PacificoFontFamily,
                        fontSize = 24.sp,
                        color = Color(0xFF4A2511),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "For ${card.recipientName.ifBlank { "You" }} • Tap the wax seal to read",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7C4A2D),
                    )
                }
            }
        } else {
            // Vintage Warm Stationery Letter
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFFFBF2),
                shadowElevation = 6.dp,
                border = BorderStroke(1.5.dp, Color(0xFFE5CFAF)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = card.letterSalutation.ifBlank {
                            "Dear ${card.recipientName.ifBlank { "Birthday Star" }},"
                        },
                        fontFamily = PacificoFontFamily,
                        fontSize = 23.sp,
                        color = Color(0xFFD81B60),
                    )
                    HorizontalDivider(color = Color(0xFFEBD9C0))
                    Text(
                        text = card.letterBody,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 26.sp,
                        ),
                        color = Color(0xFF2D1E16),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                    ) {
                        if (card.letterClosing.isNotBlank()) {
                            Text(
                                text = card.letterClosing,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B4E3D),
                            )
                        }
                        if (card.senderName.isNotBlank()) {
                            Text(
                                text = card.senderName,
                                fontFamily = PacificoFontFamily,
                                fontSize = 20.sp,
                                color = Color(0xFFD81B60),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecretGiftRevealCard(
    giftText: String,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var revealed by remember(giftText) { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !revealed) {
                revealed = true
                onReveal()
            }
            .testTag("secret_gift_reveal_card"),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (revealed) {
                        Brush.linearGradient(listOf(Color(0xFFFFF7E6), Color(0xFFFFE4EC)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFBBF24)))
                    }
                )
                .padding(22.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (!revealed) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Default.CardGiftcard,
                        contentDescription = "Secret gift",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp),
                    )
                    Text(
                        text = "Tap to Uncover Your Birthday Treat!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "🎁 YOUR SURPRISE TREAT",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFD81B60),
                    )
                    Text(
                        text = giftText,
                        fontFamily = PlayfairFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF28161F),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
