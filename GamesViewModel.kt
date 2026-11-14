package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GameScore
import com.example.data.repository.BirthdayRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GamesViewModel(
    private val repository: BirthdayRepository,
    private val currentUserId: String,
) : ViewModel() {

    val scoresState: StateFlow<UiState<Map<String, GameScore>>> = repository.observeAllGameScores(currentUserId)
        .map<List<GameScore>, UiState<Map<String, GameScore>>> { list ->
            UiState.Success(list.associateBy { it.gameId })
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing game scores", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load game scores"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading,
        )

    fun recordScore(gameId: String, score: Int) {
        viewModelScope.launch {
            repository.recordGameScore(gameId, score)
        }
    }

    private companion object {
        const val TAG = "GamesVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
