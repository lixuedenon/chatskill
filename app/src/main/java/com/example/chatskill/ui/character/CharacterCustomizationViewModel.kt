package com.example.chatskill.ui.character

import androidx.lifecycle.ViewModel
import com.example.chatskill.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CharacterCustomizationViewModel : ViewModel() {

    private val _selectedAgeRange = MutableStateFlow<AgeRange?>(null)
    val selectedAgeRange: StateFlow<AgeRange?> = _selectedAgeRange.asStateFlow()

    private val _selectedPersonality = MutableStateFlow<PersonalityType?>(null)
    val selectedPersonality: StateFlow<PersonalityType?> = _selectedPersonality.asStateFlow()

    private val _selectedEducation = MutableStateFlow<EducationLevel?>(null)
    val selectedEducation: StateFlow<EducationLevel?> = _selectedEducation.asStateFlow()

    private val _isValid = MutableStateFlow(false)
    val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

    fun onAgeRangeSelected(ageRange: AgeRange) {
        _selectedAgeRange.value = ageRange
        validateForm()
    }

    fun onPersonalitySelected(personality: PersonalityType) {
        _selectedPersonality.value = personality
        validateForm()
    }

    fun onEducationSelected(education: EducationLevel) {
        _selectedEducation.value = education
        validateForm()
    }

    private fun validateForm() {
        _isValid.value = _selectedAgeRange.value != null &&
                _selectedPersonality.value != null &&
                _selectedEducation.value != null
    }

    fun createCustomCharacter(gender: Gender): CustomCharacter? {
        val ageRange = _selectedAgeRange.value ?: return null
        val personality = _selectedPersonality.value ?: return null
        val education = _selectedEducation.value ?: return null

        val name = CustomCharacter.generateName(personality, gender)

        return CustomCharacter(
            name = name,
            ageRange = ageRange,
            personality = personality,
            education = education,
            gender = gender
        )
    }
}