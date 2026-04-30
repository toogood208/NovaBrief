package com.example.novabrief.feature.personalization.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.novabrief.core.preferences.InterestPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PersonalizationUiState(
    val selectedInterests: Set<String> = emptySet()
)

class PersonalizationViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = InterestPreferences(application)

    private val _state = MutableStateFlow(PersonalizationUiState())
    val state: StateFlow<PersonalizationUiState> = _state.asStateFlow()

    init {
        _state.value = PersonalizationUiState(
            selectedInterests = prefs.getInterests()
        )
    }

    fun toggleInterest(interest: String) {
        val current = _state.value.selectedInterests.toMutableSet()
        if (current.contains(interest)) {
            current.remove(interest)
        } else {
            current.add(interest)
        }
        _state.value = PersonalizationUiState(selectedInterests = current)
    }

    fun saveInterests() {
        prefs.saveInterests(_state.value.selectedInterests)
    }

    fun clearAllInterests() {
        _state.value = PersonalizationUiState(selectedInterests = emptySet())
    }
}
