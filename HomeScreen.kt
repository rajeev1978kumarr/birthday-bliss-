package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BirthdayCard
import com.example.data.model.CardThemeStyle
import com.example.ui.components.BirthdayCardVisual
import com.example.ui.components.ConfettiBurstOverlay
import com.example.ui.theme.PacificoFontFamily
import com.example.ui.viewmodel.StudioSection
import com.example.ui.viewmodel.UiState
import com.example.util.CardShareHelper
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    cardsState: UiState<List<BirthdayCard>>,
    userDisplayName: String,
    onCreateCard: (CardThemeStyle?, StudioSection) -> Unit,
    onOpenCard: (String) -> Unit,
    onEditCard: (String) -> Unit,
    onDeleteCard: (BirthdayCard) -> Unit,
    onOpenGames: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var cardToDelete by remember { mutableStateOf<BirthdayCard?>(null) }
    val cards = (cardsState as? UiState.Success)?.data ?: emptyList()

    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 680.dp
            val cardRows = remember(cards, isWide) {
                if (isWide) cards.chunked(2) else cards.map { listOf(it) }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 960.dp)
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Header Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = if (userDisplayName.isNotBlank()) "Signed in as $userDisplayName" else "Craft wishes, add photos & celebrate!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilledTonalButton(
                                onClick = { confettiTrigger++ },
                                modifier = Modifier.testTag("party_burst_button"),
                                shape = RoundedCornerShape(50),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = "Trigger party confetti",
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Burst!")
                            }
                            IconButton(
                                onClick = onSignOut,
                                modifier = Modifier.testTag("sign_out_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign out",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Hero Banner
                item {
                    HeroCelebrationBanner(
                        onCreateCard = { onCreateCard(null, StudioSection.CARD_PHOTO) },
                        onPlayGames = onOpenGames,
                    )
                }

                // Quick Surprise Builders (Photo, Quiz, Letter, Secret Gift)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Craft a Birthday Surprise",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                SurpriseShortcutCard(
                                    title = "Photo Card",
                                    subtitle = "Upload B'Day Star Photo",
                                    icon = Icons.Default.AddPhotoAlternate,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    testTag = "shortcut_photo_card",
                                    onClick = { onCreateCard(null, StudioSection.CARD_PHOTO) },
                                )
                            }
                            item {
                                SurpriseShortcutCard(
                                    title = "Love Quiz",
                                    subtitle = "Questions for Loved One",
                                    icon = Icons.Default.Quiz,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    testTag = "shortcut_love_quiz",
                                    onClick = { onCreateCard(null, StudioSection.QUIZ) },
                                )
                            }
                            item {
                                SurpriseShortcutCard(
                                    title = "Sealed Letter",
                                    subtitle = "Write Dil Se Letter",
                                    icon = Icons.Default.MailOutline,
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    testTag = "shortcut_letter",
                                    onClick = { onCreateCard(null, StudioSection.LETTER) },
                                )
                            }
                            item {
                                SurpriseShortcutCard(
                                    title = "Secret Gift",
                                    subtitle = "Tap-to-Reveal Treat",
                                    icon = Icons.Default.CardGiftcard,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    testTag = "shortcut_gift",
                                    onClick = { onCreateCard(null, StudioSection.GIFT) },
                                )
                            }
                        }
                    }
                }

                // Quick-Start Templates
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Start From a Card Theme",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(CardThemeStyle.entries) { theme ->
                                ThemeStarterChip(
                                    theme = theme,
                                    onClick = { onCreateCard(theme, StudioSection.CARD_PHOTO) },
                                )
                            }
                        }
                    }
                }

                // Saved Cards Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (cards.isEmpty()) "Your Card Gallery" else "Your Birthday Surprises (${cards.size})",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        if (cards.isNotEmpty()) {
                            TextButton(
                                onClick = { onCreateCard(null, StudioSection.CARD_PHOTO) },
                                modifier = Modifier.testTag("add_another_card_button"),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Surprise")
                            }
                        }
                    }
                }

                when (val state = cardsState) {
                    is UiState.Loading -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Syncing your birthday cards...")
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            ) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(20.dp),
                                )
                            }
                        }
                    }
                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            item {
                                EmptyCardsState(
                                    onCreateFirst = { onCreateCard(null, StudioSection.CARD_PHOTO) },
                                )
                            }
                        } else {
                            items(cardRows, key = { row -> row.joinToString("-") { it.id } }) { rowCards ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    rowCards.forEach { card ->
                                        SavedCardItem(
                                            card = card,
                                            onOpen = { onOpenCard(card.id) },
                                            onEdit = { onEditCard(card.id) },
                                            onShare = {
                                                scope.launch { CardShareHelper.shareCard(context, card) }
                                            },
                                            onDelete = { cardToDelete = card },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    if (rowCards.size == 1 && isWide) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }

        ConfettiBurstOverlay(burstTrigger = confettiTrigger)
    }

    cardToDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("Delete Birthday Card?") },
            text = {
                Text(
                    "Remove the card for ${pending.recipientName.ifBlank { "this recipient" }}? This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCard(pending)
                        cardToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun HeroCelebrationBanner(
    onCreateCard: () -> Unit,
    onPlayGames: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(215.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_celebration),
                contentDescription = "Celebration balloons and sparklers banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xE828111D),
                                Color(0xB83D162C),
                                Color(0x4528111D),
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    color = Color(0x33FFFFFF),
                    shape = RoundedCornerShape(50),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFCD34D),
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Make Every Birthday Shine",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                        )
                    }
                }

                Column {
                    Text(
                        text = "Personalized Cards & Party Fun",
                        fontFamily = PacificoFontFamily,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Upload a photo, customize wishes in English or हिंदी, and play party games.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFEEF4),
                        maxLines = 2,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onCreateCard,
                            modifier = Modifier.testTag("hero_create_card_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_create_card))
                        }
                        FilledTonalButton(
                            onClick = onPlayGames,
                            modifier = Modifier.testTag("hero_play_games_button"),
                            shape = RoundedCornerShape(50),
                        ) {
                            Text("Play Games")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeStarterChip(
    theme: CardThemeStyle,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(142.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("starter_theme_${theme.name.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.3f)),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(theme.gradientStart, theme.gradientEnd)))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(theme.accentColor)
            )
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = theme.textColor,
                maxLines = 1,
            )
            Text(
                text = "Tap to design →",
                style = MaterialTheme.typography.labelSmall,
                color = theme.subtitleColor,
            )
        }
    }
}

@Composable
private fun EmptyCardsState(
    onCreateFirst: () -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.96f),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_empty_cards),
                    contentDescription = "Empty birthday cards illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(136.dp)
                        .clip(RoundedCornerShape(24.dp)),
                )
                Text(
                    text = stringResource(R.string.empty_cards_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.empty_cards_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onCreateFirst,
                    modifier = Modifier.testTag("empty_create_card_button"),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Design First Birthday Card")
                }
            }
        }
    }
}

@Composable
private fun SurpriseShortcutCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(156.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.8f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SavedCardItem(
    card: BirthdayCard,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val quizCount = card.quizQuestions.size
    val extras = remember(card.photoPath, quizCount, card.letterBody, card.giftSurprise) {
        buildList {
            if (!card.photoPath.isNullOrBlank()) add("📸 Photo")
            if (quizCount > 0) add("❓ $quizCount Quiz")
            if (card.hasLetter()) add("💌 Letter")
            if (card.hasGiftSurprise()) add("🎁 Gift")
        }
    }

    Card(
        modifier = modifier.testTag("saved_card_${card.id}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = onOpen)
            ) {
                BirthdayCardVisual(
                    card = card,
                    compact = true,
                    modifier = Modifier.height(260.dp),
                )
            }
            if (extras.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    extras.forEach { badge ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = onOpen,
                    modifier = Modifier.testTag("open_card_${card.id}"),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Gift")
                }
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_card_${card.id}"),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit card")
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.testTag("share_card_${card.id}"),
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share card")
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_card_${card.id}"),
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete card",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
