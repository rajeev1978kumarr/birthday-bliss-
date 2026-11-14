package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.BirthdayRepository

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class BirthdayViewModelFactory(
    private val repository: BirthdayRepository,
    private val currentUserId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repository, currentUserId) as T
            modelClass.isAssignableFrom(CardStudioViewModel::class.java) ->
                CardStudioViewModel(repository, currentUserId) as T
            modelClass.isAssignableFrom(GamesViewModel::class.java) ->
                GamesViewModel(repository, currentUserId) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
