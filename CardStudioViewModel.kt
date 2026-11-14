package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BirthdayCard
import com.example.data.model.BirthdayWishPresets
import com.example.data.model.CardThemeStyle
import com.example.data.model.LetterStarterTemplate
import com.example.data.model.PhotoFrameShape
import com.example.data.model.QuizQuestionItem
import com.example.data.model.StarterQuizTemplates
import com.example.data.model.StickerAccent
import com.example.data.model.WishPreset
import com.example.data.repository.BirthdayRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StudioSection(val label: String) {
    CARD_PHOTO("Photo & Card"),
    QUIZ("Quiz"),
    LETTER("Letter"),
    GIFT("Secret Gift"),
}

data class CardStudioUiState(
    val cardId: String = "",
    val activeSection: StudioSection = StudioSection.CARD_PHOTO,
    val recipientName: String = "",
    val senderName: String = "",
    val milestone: String = "",
    val greetingTitle: String = BirthdayWishPresets.all.first().greeting,
    val message: String = BirthdayWishPresets.all.first().message,
    val themeStyle: CardThemeStyle = CardThemeStyle.GOLDEN_CONFETTI,
    val photoPath: String? = null,
    val photoFrameShape: PhotoFrameShape = PhotoFrameShape.POLAROID,
    val stickerAccent: StickerAccent = StickerAccent.CAKE,
    val letterSalutation: String = "",
    val letterBody: String = "",
    val letterClosing: String = "",
    val giftSurprise: String = "",
    val quizQuestions: List<QuizQuestionItem> = emptyList(),
    val createdAt: Timestamp? = null,
    val selectedPresetLang: String = "All",
    val isSaving: Boolean = false,
    val isProcessingPhoto: Boolean = false,
    val saveBurstTrigger: Int = 0,
    val statusMessage: String? = null,
) {
    fun toCard(userId: String = ""): BirthdayCard = BirthdayCard(
        id = cardId,
        userId = userId,
        recipientName = recipientName.trim(),
        senderName = senderName.trim(),
        milestone = milestone.trim(),
        greetingTitle = greetingTitle.trim().ifBlank { "Happy Birthday!" },
        message = message.trim(),
        themeStyle = themeStyle.name,
        photoPath = photoPath,
        photoFrameShape = photoFrameShape.name,
        stickerAccent = stickerAccent.name,
        letterSalutation = letterSalutation.trim(),
        letterBody = letterBody.trim(),
        letterClosing = letterClosing.trim(),
        giftSurprise = giftSurprise.trim(),
        quizQuestions = quizQuestions,
        createdAt = createdAt,
    )
}

class CardStudioViewModel(
    private val repository: BirthdayRepository,
    private val currentUserId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardStudioUiState())
    val uiState: StateFlow<CardStudioUiState> = _uiState.asStateFlow()

    fun initEditor(
        cardId: String?,
        initialThemeName: String?,
        initialSection: StudioSection = StudioSection.CARD_PHOTO,
    ) {
        if (!cardId.isNullOrBlank()) {
            viewModelScope.launch {
                val result = repository.getCard(currentUserId, cardId)
                val existing = result.getOrNull()
                if (existing != null) {
                    _uiState.value = CardStudioUiState(
                        cardId = existing.id,
                        activeSection = initialSection,
                        recipientName = existing.recipientName,
                        senderName = existing.senderName,
                        milestone = existing.milestone,
                        greetingTitle = existing.greetingTitle,
                        message = existing.message,
                        themeStyle = existing.resolveTheme(),
                        photoPath = existing.photoPath,
                        photoFrameShape = existing.resolveFrameShape(),
                        stickerAccent = existing.resolveSticker(),
                        letterSalutation = existing.letterSalutation,
                        letterBody = existing.letterBody,
                        letterClosing = existing.letterClosing,
                        giftSurprise = existing.giftSurprise,
                        quizQuestions = existing.resolveQuizQuestions(),
                        createdAt = existing.createdAt,
                    )
                } else if (result.isFailure) {
                    _uiState.update {
                        it.copy(statusMessage = result.exceptionOrNull()?.localizedMessage ?: "Unable to load card.")
                    }
                }
            }
        } else {
            val theme = initialThemeName
                ?.let { runCatching { CardThemeStyle.valueOf(it) }.getOrNull() }
                ?: CardThemeStyle.GOLDEN_CONFETTI
            _uiState.value = CardStudioUiState(
                themeStyle = theme,
                activeSection = initialSection,
            )
        }
    }

    fun selectSection(section: StudioSection) = _uiState.update { it.copy(activeSection = section) }
    fun updateRecipient(value: String) = _uiState.update { it.copy(recipientName = value) }
    fun updateSender(value: String) = _uiState.update { it.copy(senderName = value) }
    fun updateMilestone(value: String) = _uiState.update { it.copy(milestone = value) }
    fun updateGreeting(value: String) = _uiState.update { it.copy(greetingTitle = value) }
    fun updateMessage(value: String) = _uiState.update { it.copy(message = value) }
    fun selectTheme(theme: CardThemeStyle) = _uiState.update { it.copy(themeStyle = theme) }
    fun selectFrameShape(shape: PhotoFrameShape) = _uiState.update { it.copy(photoFrameShape = shape) }
    fun selectSticker(sticker: StickerAccent) = _uiState.update { it.copy(stickerAccent = sticker) }
    fun selectPresetLanguage(lang: String) = _uiState.update { it.copy(selectedPresetLang = lang) }

    fun updateLetterSalutation(value: String) = _uiState.update { it.copy(letterSalutation = value) }
    fun updateLetterBody(value: String) = _uiState.update { it.copy(letterBody = value) }
    fun updateLetterClosing(value: String) = _uiState.update { it.copy(letterClosing = value) }
    fun updateGiftSurprise(value: String) = _uiState.update { it.copy(giftSurprise = value) }

    fun applyWishPreset(preset: WishPreset) {
        _uiState.update {
            it.copy(
                greetingTitle = preset.greeting,
                message = preset.message,
            )
        }
    }

    fun applyLetterTemplate(template: LetterStarterTemplate) {
        _uiState.update {
            it.copy(
                letterSalutation = template.salutation,
                letterBody = template.body,
                letterClosing = template.closing,
                statusMessage = "Letter template applied — customize it with your own memories!",
            )
        }
    }

    fun addQuizQuestion(item: QuizQuestionItem) {
        _uiState.update { state ->
            state.copy(
                quizQuestions = state.quizQuestions + item,
                statusMessage = "Quiz question added!",
            )
        }
    }

    fun removeQuizQuestion(id: String) {
        _uiState.update { state ->
            state.copy(quizQuestions = state.quizQuestions.filterNot { it.id == id })
        }
    }

    fun addStarterQuizPack() {
        _uiState.update { state ->
            val existingIds = state.quizQuestions.map { it.id }.toSet()
            val toAdd = StarterQuizTemplates.starterQuestions.filterNot { it.id in existingIds }
            state.copy(
                quizQuestions = state.quizQuestions + toAdd,
                statusMessage = if (toAdd.isEmpty()) "Starter questions already added!" else "Added ${toAdd.size} starter quiz questions!",
            )
        }
    }

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPhoto = true, statusMessage = null) }
            val savedPath = repository.persistPickedPhoto(uri)
            _uiState.update { state ->
                if (savedPath != null) {
                    state.copy(
                        photoPath = savedPath,
                        isProcessingPhoto = false,
                        statusMessage = "Birthday star's photo uploaded!",
                    )
                } else {
                    state.copy(
                        isProcessingPhoto = false,
                        statusMessage = "Could not load selected photo.",
                    )
                }
            }
        }
    }

    fun removePhoto() {
        _uiState.update { it.copy(photoPath = null) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun saveCard(onSaved: (String) -> Unit = {}) {
        val current = _uiState.value
        if (current.recipientName.isBlank()) {
            _uiState.update {
                it.copy(
                    activeSection = StudioSection.CARD_PHOTO,
                    statusMessage = "Please enter the birthday star's name.",
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = repository.saveCard(current.toCard(currentUserId))
            result.onSuccess { savedId ->
                _uiState.update {
                    it.copy(
                        cardId = savedId,
                        isSaving = false,
                        saveBurstTrigger = it.saveBurstTrigger + 1,
                        statusMessage = "Birthday surprise saved to cloud!",
                    )
                }
                onSaved(savedId)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        statusMessage = err.localizedMessage ?: "Failed to save birthday card.",
                    )
                }
            }
        }
    }
}
