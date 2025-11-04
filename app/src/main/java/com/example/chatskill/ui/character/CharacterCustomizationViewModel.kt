// 路径: app/src/main/java/com/example/chatskill/ui/character/CharacterCustomizationViewModel.kt
// 类型: class

package com.example.chatskill.ui.character

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatskill.data.model.*
import com.example.chatskill.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterCustomizationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application.applicationContext)

    private val _selectedAgeRange = MutableStateFlow<AgeRange?>(null)
    val selectedAgeRange: StateFlow<AgeRange?> = _selectedAgeRange.asStateFlow()

    private val _selectedPersonality = MutableStateFlow<PersonalityType?>(null)
    val selectedPersonality: StateFlow<PersonalityType?> = _selectedPersonality.asStateFlow()

    private val _selectedEducation = MutableStateFlow<EducationLevel?>(null)
    val selectedEducation: StateFlow<EducationLevel?> = _selectedEducation.asStateFlow()

    private val _selectedWorkStatus = MutableStateFlow<WorkStatus?>(null)
    val selectedWorkStatus: StateFlow<WorkStatus?> = _selectedWorkStatus.asStateFlow()

    private val _isValid = MutableStateFlow(false)
    val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    // 修改：返回角色和简历的Pair
    private val _generatedCharacterWithBackground = MutableStateFlow<Pair<CustomCharacter, CharacterBackground>?>(null)
    val generatedCharacterWithBackground: StateFlow<Pair<CustomCharacter, CharacterBackground>?> = _generatedCharacterWithBackground.asStateFlow()

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
        if (education == EducationLevel.MIDDLE_SCHOOL || education == EducationLevel.HIGH_SCHOOL) {
            if (_selectedWorkStatus.value == WorkStatus.STUDYING) {
                _selectedWorkStatus.value = null
            }
        }
        validateForm()
    }

    fun onWorkStatusSelected(workStatus: WorkStatus) {
        _selectedWorkStatus.value = workStatus
        validateForm()
    }

    private fun validateForm() {
        _isValid.value = _selectedAgeRange.value != null &&
                _selectedPersonality.value != null &&
                _selectedEducation.value != null &&
                _selectedWorkStatus.value != null
    }

    fun generateCharacter(gender: Gender) {
        val ageRange = _selectedAgeRange.value ?: return
        val personality = _selectedPersonality.value ?: return
        val education = _selectedEducation.value ?: return
        val workStatus = _selectedWorkStatus.value ?: return

        _isGenerating.value = true
        _generationError.value = null

        viewModelScope.launch {
            try {
                repository.generateCharacterProfile(
                    gender = gender,
                    ageRange = ageRange,
                    personality = personality,
                    education = education,
                    workStatus = workStatus
                ).collect { profile ->
                    val character = CustomCharacter(
                        name = profile.name,
                        ageRange = ageRange,
                        personality = personality,
                        education = education,
                        workStatus = workStatus,
                        gender = gender,
                        occupation = profile.occupation,
                        expertHobbies = profile.expert_hobbies.map {
                            HobbyLevel(it.name, it.level)
                        },
                        casualHobbies = profile.casual_hobbies.map {
                            HobbyLevel(it.name, it.level)
                        }
                    )

                    val background = CharacterBackground(
                        education_history = profile.education_history,
                        work_history = profile.work_history,
                        hobby_development = profile.hobby_development
                    )

                    _generatedCharacterWithBackground.value = Pair(character, background)
                    _isGenerating.value = false
                }
            } catch (e: Exception) {
                _generationError.value = e.message ?: "生成角色失败，请重试"
                _isGenerating.value = false
            }
        }
    }

    fun clearError() {
        _generationError.value = null
    }

    fun clearGeneratedCharacter() {
        _generatedCharacterWithBackground.value = null
    }
}