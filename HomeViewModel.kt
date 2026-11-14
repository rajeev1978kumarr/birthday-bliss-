package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BirthdayCard
import com.example.data.repository.BirthdayRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: BirthdayRepository,
    private val currentUserId: String,
) : ViewModel() {

    val cardsState: StateFlow<UiState<List<BirthdayCard>>> = repository.observeCards(currentUserId)
        .map<List<BirthdayCard>, UiState<List<BirthdayCard>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing birthday cards", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load birthday cards"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading,
        )

    fun observeCard(cardId: String): Flow<BirthdayCard?> =
        repository.observeCard(currentUserId, cardId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing card $cardId", firestoreError ?: error)
                emit(null)
            }

    fun deleteCard(card: BirthdayCard) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    private companion object {
        const val TAG = "HomeVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
