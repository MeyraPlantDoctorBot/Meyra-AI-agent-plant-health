package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MeyraRepository
import com.example.data.api.DiagnosticResult
import com.example.data.local.CropHealthRecordEntity
import com.example.data.local.FarmerProfileEntity
import com.example.data.local.PostHarvestRecordEntity
import com.example.data.local.TreatmentHistoryEntity
import com.example.data.voice.VoiceSpeechManager
import com.example.data.weather.AgroWeatherCondition
import com.example.data.weather.AgroWeatherService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MeyraLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("EN", "English", "English"),
    AMHARIC("AM", "Amharic", "አማርኛ"),
    OROMO("OM", "Afaan Oromo", "Afaan Oromo")
}

data class DiagnosisUiState(
    val selectedCrop: String = "Coffee",
    val symptomInput: String = "",
    val growthStage: String = "Vegetative / Flowering",
    val locationRegion: String = "Jimma, Oromia",
    val capturedImage: Bitmap? = null,
    val isRecordingVoice: Boolean = false,
    val voiceTranscript: String = "",
    val isLoading: Boolean = false,
    val currentResult: DiagnosticResult? = null,
    val errorMessage: String? = null,
    // Follow-Up Questions
    val followUpQuestions: List<String> = emptyList(),
    val followUpAnswers: Map<String, String> = emptyMap(),
    // GPS & Weather
    val isDetectingGps: Boolean = false,
    val showCameraGuide: Boolean = false
)

class MeyraViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MeyraRepository(application.applicationContext)
    private val speechManager = VoiceSpeechManager(application.applicationContext)
    private val agroWeatherService = AgroWeatherService(application.applicationContext)

    private val _selectedLanguage = MutableStateFlow(MeyraLanguage.ENGLISH)
    val selectedLanguage: StateFlow<MeyraLanguage> = _selectedLanguage.asStateFlow()

    private val _diagnosisUiState = MutableStateFlow(DiagnosisUiState())
    val diagnosisUiState: StateFlow<DiagnosisUiState> = _diagnosisUiState.asStateFlow()

    private val _agroWeather = MutableStateFlow(AgroWeatherCondition())
    val agroWeather: StateFlow<AgroWeatherCondition> = _agroWeather.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = speechManager.isSpeaking
    val spokenText: StateFlow<String> = speechManager.spokenText

    val farmerProfile: StateFlow<FarmerProfileEntity> = repository.farmerProfile
        .map { it ?: FarmerProfileEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FarmerProfileEntity())

    val cropHealthRecords: StateFlow<List<CropHealthRecordEntity>> = repository.cropHealthRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val postHarvestRecords: StateFlow<List<PostHarvestRecordEntity>> = repository.postHarvestRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val treatmentHistory: StateFlow<List<TreatmentHistoryEntity>> = repository.treatmentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize follow-up questions
        updateSelectedCrop("Coffee")
        // Load initial weather
        fetchGpsWeather()
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }

    fun selectLanguage(language: MeyraLanguage) {
        _selectedLanguage.value = language
        _diagnosisUiState.update {
            it.copy(followUpQuestions = generateFollowUpQuestions(it.selectedCrop, language.code))
        }
    }

    fun updateSelectedCrop(crop: String) {
        _diagnosisUiState.update {
            it.copy(
                selectedCrop = crop,
                followUpQuestions = generateFollowUpQuestions(crop, _selectedLanguage.value.code)
            )
        }
    }

    fun updateSymptomInput(symptoms: String) {
        _diagnosisUiState.update { it.copy(symptomInput = symptoms) }
    }

    fun updateGrowthStage(stage: String) {
        _diagnosisUiState.update { it.copy(growthStage = stage) }
    }

    fun updateLocationRegion(region: String) {
        _diagnosisUiState.update { it.copy(locationRegion = region) }
    }

    fun setCapturedImage(bitmap: Bitmap?) {
        _diagnosisUiState.update { it.copy(capturedImage = bitmap) }
    }

    fun toggleCameraGuide(show: Boolean) {
        _diagnosisUiState.update { it.copy(showCameraGuide = show) }
    }

    fun fetchGpsWeather() {
        _diagnosisUiState.update { it.copy(isDetectingGps = true) }
        viewModelScope.launch {
            try {
                val location = agroWeatherService.getCurrentLocation()
                if (location != null) {
                    val weather = agroWeatherService.getAgroWeatherForCoordinates(
                        location.latitude,
                        location.longitude
                    )
                    _agroWeather.value = weather
                    _diagnosisUiState.update {
                        it.copy(
                            locationRegion = weather.locationName,
                            isDetectingGps = false
                        )
                    }
                } else {
                    // Fallback to regional weather for current location string
                    val fallbackWeather = agroWeatherService.getAgroWeatherForCoordinates(7.6734, 36.8344)
                    _agroWeather.value = fallbackWeather
                    _diagnosisUiState.update { it.copy(isDetectingGps = false) }
                }
            } catch (e: Exception) {
                _diagnosisUiState.update { it.copy(isDetectingGps = false) }
            }
        }
    }

    // Voice Readout (TTS) Methods
    fun speakDiagnosisResult(result: DiagnosticResult) {
        val speechText = buildString {
            append("${result.cropName}: ${result.diagnosisTitle}. ")
            append("Explanation: ${result.fullExplanation}. ")
            append("Recommended IPM: ${result.ipmStrategy}. ")
            if (result.safeChemicalOptions.isNotBlank()) {
                append("Safety Warning: ${result.dosageAndSafety}")
            }
        }
        speechManager.speak(speechText, _selectedLanguage.value.code)
    }

    fun speakCustomText(text: String) {
        speechManager.speak(text, _selectedLanguage.value.code)
    }

    fun stopSpeaking() {
        speechManager.stop()
    }

    fun toggleVoiceRecording() {
        val currentlyRecording = _diagnosisUiState.value.isRecordingVoice
        if (!currentlyRecording) {
            _diagnosisUiState.update { it.copy(isRecordingVoice = true) }
        } else {
            // Simulated voice transcription in Ethiopian agricultural context
            val simulatedTranscript = when (_selectedLanguage.value) {
                MeyraLanguage.AMHARIC -> "በቡናው ቅጠል ላይ ቢጫና ብርቱካናማ ነጠብጣቦች ታይተዋል። ቅጠሎቹም እየረገፉ ነው"
                MeyraLanguage.OROMO -> "Baala bunnaa irra waanti dimaafi keelloon mul'ata. Baallis harca'aa jira"
                MeyraLanguage.ENGLISH -> "Yellow orange spots appear under coffee leaves and leaves are dropping prematurely."
            }
            _diagnosisUiState.update {
                it.copy(
                    isRecordingVoice = false,
                    symptomInput = simulatedTranscript,
                    voiceTranscript = simulatedTranscript
                )
            }
        }
    }

    fun onSpeechRecognized(text: String) {
        _diagnosisUiState.update {
            it.copy(
                isRecordingVoice = false,
                symptomInput = if (it.symptomInput.isBlank()) text else "${it.symptomInput}. $text",
                voiceTranscript = text
            )
        }
    }

    fun answerFollowUpQuestion(question: String, answer: String) {
        _diagnosisUiState.update {
            val updatedAnswers = it.followUpAnswers.toMutableMap()
            updatedAnswers[question] = answer
            it.copy(followUpAnswers = updatedAnswers)
        }
    }

    fun runDiagnosis() {
        val state = _diagnosisUiState.value
        val answersText = state.followUpAnswers.entries.joinToString("; ") { "${it.key}: ${it.value}" }
        val currentWeather = _agroWeather.value
        val weatherContext = "Microclimate: ${currentWeather.temperatureCelsius}°C, ${currentWeather.humidityPercent}% humidity (${currentWeather.conditionDescription})"

        val combinedSymptoms = if (answersText.isNotBlank()) {
            "${state.symptomInput}. Additional observations: $answersText. [Weather: $weatherContext]"
        } else {
            "${state.symptomInput.ifBlank { "Unusual spot marks and yellowing on foliage" }}. [Weather: $weatherContext]"
        }

        _diagnosisUiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val result = repository.performDiagnosisAndSave(
                    cropName = state.selectedCrop,
                    symptoms = combinedSymptoms,
                    bitmapImage = state.capturedImage,
                    languageCode = _selectedLanguage.value.code,
                    regionLocation = state.locationRegion,
                    growthStage = state.growthStage
                )
                _diagnosisUiState.update {
                    it.copy(isLoading = false, currentResult = result)
                }
            } catch (e: Exception) {
                _diagnosisUiState.update {
                    it.copy(isLoading = false, errorMessage = "Diagnosis error: ${e.message}")
                }
            }
        }
    }

    fun resetDiagnosisForm() {
        _diagnosisUiState.value = DiagnosisUiState(
            selectedCrop = _diagnosisUiState.value.selectedCrop,
            followUpQuestions = generateFollowUpQuestions(_diagnosisUiState.value.selectedCrop, _selectedLanguage.value.code)
        )
    }

    fun toggleRecordResolved(id: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.markRecordResolved(id, !currentStatus)
        }
    }

    fun updateProfile(name: String, phone: String, region: String, zone: String, hectares: Double) {
        viewModelScope.launch {
            repository.saveProfile(
                FarmerProfileEntity(
                    id = "default_farmer",
                    name = name,
                    phone = phone,
                    region = region,
                    zoneWoreda = zone,
                    farmSizeHectares = hectares,
                    preferredLanguage = _selectedLanguage.value.code
                )
            )
        }
    }

    fun savePostHarvestRecord(crop: String, kg: Double, moisture: Double, method: String) {
        viewModelScope.launch {
            repository.addPostHarvestRecord(
                PostHarvestRecordEntity(
                    cropName = crop,
                    harvestQuantityKg = kg,
                    moistureContentPercent = moisture,
                    storageMethod = method,
                    expectedLossReductionPercent = if (method.contains("PICS")) 85 else 45,
                    marketTarget = "Local Cooperative Union"
                )
            )
        }
    }

    fun saveTreatmentRecord(
        cropRecordId: Long = 0,
        cropName: String,
        treatmentName: String,
        treatmentType: String,
        dosage: String,
        notes: String = "",
        effectiveness: String = "Effective"
    ) {
        viewModelScope.launch {
            repository.addTreatmentRecord(
                TreatmentHistoryEntity(
                    cropRecordId = cropRecordId,
                    cropName = cropName,
                    treatmentName = treatmentName,
                    treatmentType = treatmentType,
                    appliedDosage = dosage,
                    notes = notes,
                    effectiveness = effectiveness
                )
            )
        }
    }

    private fun generateFollowUpQuestions(crop: String, langCode: String): List<String> {
        return when (crop) {
            "Coffee" -> when (langCode) {
                "AM" -> listOf("በቅጠሉ ስር ብርቱካናማ ብናኝ አለ?", "የቡና ፍሬዎች ጥቁር እና የደረቁ ናቸው?", "የፀሐይ ብርሃን የሚያገኝበት ደረጃ?")
                "OM" -> listOf("Baala jala daaraa keelloon ni jiraa?", "Iji bunnaa gurraacha'ee qoraa?", "Sadarkaa gaaddisa mukaa?")
                else -> listOf("Is there orange dust on leaf undersides?", "Are coffee berries turning black and dry?", "What is the shade tree coverage density?")
            }
            "Maize" -> when (langCode) {
                "AM" -> listOf("በቆሎው እምብርት ውስጥ ጉድጓድና ፍግ አለ?", "የቅጠል ስንጥቆች አሉ?", "የወደቁ እንቁላሎች ታይተዋል?")
                "OM" -> listOf("Sondii boqolloo keessa boojitoonni ni jiru?", "Baalli tarsa'eeraa?", "Hanqaaquun raammos mul'ateeraa?")
                else -> listOf("Is there chewed whorl damage and frass?", "Are leaves torn with saw-tooth edges?", "Are caterpillars visible in central leaves?")
            }
            "Teff" -> when (langCode) {
                "AM" -> listOf("ቅጠሎች ላይ ቀይ ወይም ቡናማ ነጠብጣብ አለ?", "እፅዋቱ መሬት ላይ ወድቋል?", "የአፈር እርጥበት ደረጃ?")
                "OM" -> listOf("Baala allaa irra mallattoon diimaan jiraa?", "Midhaan lafatti kuffiseeraa?", "Bishaan lafaa fi dhiibbaa biyyee?")
                else -> listOf("Are there reddish-brown rust pustules?", "Is the teff crop lodging (falling over)?", "Is field drainage adequate?")
            }
            else -> when (langCode) {
                "AM" -> listOf("የተጎዳው ክፍል ቅጠል፣ ግንድ ወይስ ፍሬ ነው?", "ምልክቱ መቼ ተጀመረ?", "በአቅራቢያ ያሉ ሌሎች ተክሎች ተጎድተዋል?")
                "OM" -> listOf("Miidhaan baala, mujaas ykn fira irra jiraa?", "Mallattoon yoom eegale?", "Biqiltuun biraa ni miidhameeraa?")
                else -> listOf("Is damage primarily on leaves, stems, or fruit?", "How many days ago did symptoms appear?", "Are neighboring farm plots affected?")
            }
        }
    }
}

