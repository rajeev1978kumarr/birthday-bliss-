package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.BirthdayWishPresets
import com.example.data.model.CardThemeStyle
import com.example.data.model.PhotoFrameShape
import com.example.data.model.StickerAccent
import com.example.ui.components.BirthdayCardVisual
import com.example.ui.components.ConfettiBurstOverlay
import com.example.ui.viewmodel.CardStudioUiState
import com.example.ui.viewmodel.CardStudioViewModel
import com.example.ui.viewmodel.StudioSection
import com.example.util.CardShareHelper
import kotlinx.coroutines.launch

@Composable
fun CardStudioScreen(
    viewModel: CardStudioViewModel,
    cardId: String?,
    initialTheme: String?,
    initialSection: StudioSection = StudioSection.CARD_PHOTO,
    onBack: (() -> Unit)? = null,
    onCardSavedNavigate: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(cardId, initialTheme, initialSection) {
        viewModel.initEditor(cardId, initialTheme, initialSection)
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onPhotoPicked(uri)
            }
        },
    )

    val launchPhotoPicker = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 760.dp

            Column(modifier = Modifier.fillMaxSize()) {
                // Studio Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        if (onBack != null) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("studio_back_button"),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                        Column {
                            Text(
                                text = if (state.cardId.isNotBlank()) "Edit Surprise" else "Surprise Studio",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Text(
                                text = "Card, photo, quiz, letter & secret gift",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    CardShareHelper.shareCard(context, state.toCard())
                                }
                            },
                            modifier = Modifier.testTag("studio_share_button"),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_share_card))
                        }
                        Button(
                            onClick = {
                                viewModel.saveCard { savedId ->
                                    onCardSavedNavigate?.invoke(savedId)
                                }
                            },
                            enabled = !state.isSaving,
                            modifier = Modifier.testTag("studio_save_button"),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_save_card))
                        }
                    }
                }

                // Studio Section Tabs (Card/Photo, Quiz, Letter, Secret Gift)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(StudioSection.entries) { section ->
                        val selected = state.activeSection == section
                        val icon = when (section) {
                            StudioSection.CARD_PHOTO -> Icons.Default.Brush
                            StudioSection.QUIZ -> Icons.Default.Quiz
                            StudioSection.LETTER -> Icons.Default.MailOutline
                            StudioSection.GIFT -> Icons.Default.CardGiftcard
                        }
                        val badgeSuffix = when (section) {
                            StudioSection.QUIZ -> if (state.quizQuestions.isNotEmpty()) " (${state.quizQuestions.size})" else ""
                            StudioSection.LETTER -> if (state.letterBody.isNotBlank()) " ✓" else ""
                            StudioSection.GIFT -> if (state.giftSurprise.isNotBlank()) " ✓" else ""
                            else -> ""
                        }
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectSection(section) },
                            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp)) },
                            label = { Text("${section.label}$badgeSuffix") },
                            modifier = Modifier.testTag("studio_tab_${section.name.lowercase()}"),
                        )
                    }
                }

                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 24.dp),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            BirthdayCardVisual(
                                card = state.toCard(),
                                onPhotoSlotClick = launchPhotoPicker,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            ActiveStudioSectionContent(
                                state = state,
                                viewModel = viewModel,
                                onPickPhoto = launchPhotoPicker,
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (state.activeSection == StudioSection.CARD_PHOTO) {
                            BirthdayCardVisual(
                                card = state.toCard(),
                                onPhotoSlotClick = launchPhotoPicker,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                        ActiveStudioSectionContent(
                            state = state,
                            viewModel = viewModel,
                            onPickPhoto = launchPhotoPicker,
                        )
                    }
                }
            }
        }

        ConfettiBurstOverlay(burstTrigger = state.saveBurstTrigger)

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

@Composable
private fun ActiveStudioSectionContent(
    state: CardStudioUiState,
    viewModel: CardStudioViewModel,
    onPickPhoto: () -> Unit,
) {
    when (state.activeSection) {
        StudioSection.CARD_PHOTO -> StudioControlsColumn(
            state = state,
            viewModel = viewModel,
            onPickPhoto = onPickPhoto,
            modifier = Modifier.fillMaxWidth(),
        )
        StudioSection.QUIZ -> QuizBuilderSection(
            state = state,
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth(),
        )
        StudioSection.LETTER -> LetterComposerSection(
            state = state,
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth(),
        )
        StudioSection.GIFT -> GiftSurpriseSection(
            state = state,
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StudioControlsColumn(
    state: CardStudioUiState,
    viewModel: CardStudioViewModel,
    onPickPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val languages = listOf("All", "English", "हिंदी", "Hinglish")
    val filteredPresets = remember(state.selectedPresetLang) {
        if (state.selectedPresetLang == "All") BirthdayWishPresets.all
        else BirthdayWishPresets.all.filter { it.languageTag == state.selectedPresetLang }
    }

    Column(
        modifier = modifier.padding(bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Prominent Birthday Star Photo Upload Section
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Birthday Star's Photo",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Jiska birthday hai unki photo upload karein",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.isProcessingPhoto) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onPickPhoto,
                        modifier = Modifier.testTag("upload_photo_button"),
                        shape = RoundedCornerShape(50),
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (state.photoPath != null) "Change Photo"
                            else stringResource(R.string.action_pick_photo)
                        )
                    }
                    if (state.photoPath != null) {
                        TextButton(
                            onClick = { viewModel.removePhoto() },
                            modifier = Modifier.testTag("remove_photo_button"),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.action_remove_photo))
                        }
                    }
                }

                if (state.photoPath != null) {
                    Text(
                        text = "Frame Style",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PhotoFrameShape.entries.forEach { shape ->
                            FilterChip(
                                selected = state.photoFrameShape == shape,
                                onClick = { viewModel.selectFrameShape(shape) },
                                label = { Text(shape.displayName) },
                                modifier = Modifier.testTag("frame_shape_${shape.name.lowercase()}"),
                            )
                        }
                    }
                }
            }
        }

        // Names & Milestone
        OutlinedTextField(
            value = state.recipientName,
            onValueChange = viewModel::updateRecipient,
            label = { Text("Birthday Star Name *") },
            placeholder = { Text("e.g., Priya, Mom, Aarav") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recipient_name_input"),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = state.milestone,
                onValueChange = viewModel::updateMilestone,
                label = { Text("Milestone (Optional)") },
                placeholder = { Text("25th / Sweet 16") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("milestone_input"),
            )
            OutlinedTextField(
                value = state.senderName,
                onValueChange = viewModel::updateSender,
                label = { Text("From") },
                placeholder = { Text("Your name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("sender_name_input"),
            )
        }

        // Theme Picker
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Card Theme",
                style = MaterialTheme.typography.titleMedium,
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
            ) {
                items(CardThemeStyle.entries) { theme ->
                    val selected = state.themeStyle == theme
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { viewModel.selectTheme(theme) }
                            .testTag("theme_option_${theme.name.lowercase()}"),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = if (selected) 2.5.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Brush.linearGradient(listOf(theme.gradientStart, theme.gradientEnd)))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(theme.accentColor)
                                    .border(1.dp, theme.textColor.copy(alpha = 0.25f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (theme.isDarkCard) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(13.dp),
                                    )
                                }
                            }
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = theme.textColor,
                            )
                        }
                    }
                }
            }
        }

        // Sticker Picker
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Card Sticker",
                style = MaterialTheme.typography.titleMedium,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StickerAccent.entries) { sticker ->
                    val selected = state.stickerAccent == sticker
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectSticker(sticker) },
                        label = { Text("${sticker.emoji} ${sticker.label}") },
                        modifier = Modifier.testTag("sticker_${sticker.name.lowercase()}"),
                    )
                }
            }
        }

        // Wish Presets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Quick Wish Templates",
                style = MaterialTheme.typography.titleMedium,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { lang ->
                    FilterChip(
                        selected = state.selectedPresetLang == lang,
                        onClick = { viewModel.selectPresetLanguage(lang) },
                        label = { Text(lang) },
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredPresets, key = { it.id }) { preset ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.applyWishPreset(preset) }
                            .testTag("preset_${preset.id}"),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = "${preset.title} (${preset.languageTag})",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                text = preset.greeting,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                            )
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.greetingTitle,
            onValueChange = viewModel::updateGreeting,
            label = { Text("Card Headline") },
            placeholder = { Text("Happy Birthday!") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("greeting_title_input"),
        )

        OutlinedTextField(
            value = state.message,
            onValueChange = viewModel::updateMessage,
            label = { Text("Your Birthday Wish") },
            placeholder = { Text("Write a heartfelt, funny, or festive message...") },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wish_message_input"),
        )
    }
}
