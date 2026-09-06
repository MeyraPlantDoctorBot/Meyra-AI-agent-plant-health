package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.api.DiagnosticResult
import com.example.data.weather.AgroWeatherCondition
import com.example.ui.MeyraLanguage
import com.example.ui.MeyraViewModel
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorScreen(
    viewModel: MeyraViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.diagnosisUiState.collectAsState()
    val weather by viewModel.agroWeather.collectAsState()
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val context = LocalContext.current

    var showCameraViewFinder by remember { mutableStateOf(false) }

    // Live Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.setCapturedImage(bitmap)
        }
    }

    // Gallery photo picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                viewModel.setCapturedImage(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Speech-to-Text Recognition Launcher
    val speechRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.onSpeechRecognized(spokenText)
            }
        }
    }

    fun startSpeechToText() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                val langTag = when (selectedLang) {
                    MeyraLanguage.AMHARIC -> "am-ET"
                    MeyraLanguage.OROMO -> "om-ET"
                    else -> Locale.getDefault().toLanguageTag()
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe plant leaf symptoms...")
            }
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            // Fallback to simulated agricultural speech recording
            viewModel.toggleVoiceRecording()
        }
    }

    val cropList = listOf("Coffee", "Maize", "Teff", "Avocado", "Wheat", "Sorghum", "Tomato", "Onion")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Hero Greeting & GPS Weather Microclimate Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Greeting & Profile Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (selectedLang) {
                                MeyraLanguage.AMHARIC -> "ሰላም፣ አበበ"
                                MeyraLanguage.OROMO -> "Akkam, Abebe"
                                else -> "Selam, Abebe"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Light,
                            color = MeyraTextDark
                        )
                        Text(
                            text = "${weather.locationName} • ${weather.altitudeMeters}m alt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MeyraTextMuted
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MeyraGreenLight,
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MeyraGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", fontWeight = FontWeight.Bold, color = MeyraGreenDark)
                        }
                    }
                }

                // GPS Agro-Weather Alert & Microclimate Card
                GpsAgroWeatherCard(
                    weather = weather,
                    isDetecting = state.isDetectingGps,
                    onRefreshGps = { viewModel.fetchGpsWeather() },
                    onListenAdvisory = {
                        viewModel.speakCustomText("Weather update for ${weather.locationName}: Temperature ${weather.temperatureCelsius.toInt()} degrees, Humidity ${weather.humidityPercent} percent. ${weather.sprayingAdvice}")
                    }
                )

                // Clean Minimalism Highlight Banner Button (Diagnose Crop)
                Card(
                    onClick = { viewModel.runDiagnosis() },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MeyraAccentHighlight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when (selectedLang) {
                                    MeyraLanguage.AMHARIC -> "ሰብል ይመረምሩ"
                                    MeyraLanguage.OROMO -> "Midhaan Qoradhu"
                                    else -> "Diagnose Crop Health"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MeyraGreenDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (selectedLang) {
                                    MeyraLanguage.AMHARIC -> "ቅጠሎችን ይቃኙ ወይም ምልክቶችን ይግለጹ"
                                    MeyraLanguage.OROMO -> "Baala Waraabi ykn Mallattoollee Iddoo Buusi"
                                    else -> "Live leaf camera scanner & AI pathology"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MeyraGreenDark.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MeyraGreenPrimary,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Scan",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                // Quick Action Grid (Voice Advice & Camera Leaf Scan)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Voice Input Button
                    Card(
                        onClick = { startSpeechToText() },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.isRecordingVoice) MeyraWarningOrange.copy(alpha = 0.15f) else Color.White
                        ),
                        border = BorderStroke(1.dp, MeyraCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MeyraGreenLight,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice",
                                        tint = MeyraGreenPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Voice Input",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MeyraTextDark
                                )
                                Text(
                                    text = "Amharic / Oromo / EN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MeyraTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Live Camera Leaf Scanner Trigger
                    Card(
                        onClick = { showCameraViewFinder = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MeyraCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MeyraGreenLight,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DocumentScanner,
                                        contentDescription = "Camera Scanner",
                                        tint = MeyraGreenPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Camera Scan",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MeyraTextDark
                                )
                                Text(
                                    text = "Target Leaf Frame",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MeyraTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Crop Selection Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when (selectedLang) {
                        MeyraLanguage.AMHARIC -> "1. ሰብል ይምረጡ"
                        MeyraLanguage.OROMO -> "1. Midhaan Filadhu"
                        else -> "1. Select Crop Type"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cropList) { crop ->
                        val isSelected = crop == state.selectedCrop
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateSelectedCrop(crop) },
                            label = { Text(crop) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSelected) Icons.Filled.Check else Icons.Outlined.Eco,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MeyraGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Multimodal Input: Live Camera Scanner & Voice Assistant
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MeyraCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = when (selectedLang) {
                            MeyraLanguage.AMHARIC -> "2. ፎቶ አንሳ ወይም ድምፅ አስገባ"
                            MeyraLanguage.OROMO -> "2. Suuraa waraabi ykn Sagalee dubbadhu"
                            else -> "2. Live Leaf Photo & Voice Note"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Live Camera Shutter Card
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clickable {
                                    try {
                                        cameraLauncher.launch(null)
                                    } catch (e: Exception) {
                                        showCameraViewFinder = true
                                    }
                                },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (state.capturedImage != null) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        bitmap = state.capturedImage!!.asImageBitmap(),
                                        contentDescription = "Crop Sample",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = MeyraGreenPrimary,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(22.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Captured",
                                            tint = Color.White,
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = MeyraGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (selectedLang) {
                                            MeyraLanguage.AMHARIC -> "ካሜራ ክፈት"
                                            MeyraLanguage.OROMO -> "Kaameraa bani"
                                            else -> "Take Leaf Photo"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Gallery Picker Card
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clickable { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = MeyraHarvestAmber
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (selectedLang) {
                                        MeyraLanguage.AMHARIC -> "ከጋለሪ ምረጥ"
                                        MeyraLanguage.OROMO -> "Kuusaa suuraa"
                                        else -> "Choose Gallery"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Text Symptoms Input with Voice Mic Button inside
                    OutlinedTextField(
                        value = state.symptomInput,
                        onValueChange = { viewModel.updateSymptomInput(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                when (selectedLang) {
                                    MeyraLanguage.AMHARIC -> "የበሽታውን ወይም ተባዩን ምልክት ይግለጹ"
                                    MeyraLanguage.OROMO -> "Mallaattoo dhukkubaa ykn baaktiriyaa barreessi"
                                    else -> "Describe symptoms, spots, wilting, or insects"
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                when (selectedLang) {
                                    MeyraLanguage.AMHARIC -> "ምሳሌ፡ ቅጠሉ ላይ ቡናማ ነጠብጣቦች አሉ..."
                                    MeyraLanguage.OROMO -> "Fakkeenya: Baala irra waan gurraacha muraasi..."
                                    else -> "e.g., Orange powder under leaves, stunted growth..."
                                }
                            )
                        },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { startSpeechToText() }) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = if (state.isRecordingVoice) MeyraAlertRed else MeyraGreenPrimary
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Dynamic Follow-Up Questions (Interactive Agronomist Quiz)
        if (state.followUpQuestions.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = MeyraGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (selectedLang) {
                                    MeyraLanguage.AMHARIC -> "3. የሜይራ አግሮኖሚስት ጥያቄዎች"
                                    MeyraLanguage.OROMO -> "3. Gaaffii Xiinxala Agronomist"
                                    else -> "3. Agronomist Field Questions"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        state.followUpQuestions.forEach { question ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = question,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                val currentAnswer = state.followUpAnswers[question] ?: ""
                                OutlinedTextField(
                                    value = currentAnswer,
                                    onValueChange = { viewModel.answerFollowUpQuestion(question, it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Yes / No / Specific observations...", fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Growth Stage & Location Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.growthStage,
                    onValueChange = { viewModel.updateGrowthStage(it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Growth Stage", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = state.locationRegion,
                    onValueChange = { viewModel.updateLocationRegion(it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("GPS Zone", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Action Button: Diagnose
        item {
            Button(
                onClick = { viewModel.runDiagnosis() },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MeyraGreenPrimary)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Analyzing Plant Pathology...")
                } else {
                    Icon(Icons.Default.Healing, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (selectedLang) {
                            MeyraLanguage.AMHARIC -> "ምርመራ ጀምር"
                            MeyraLanguage.OROMO -> "Qorannoo Eegali"
                            else -> "Diagnose Plant Health"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Diagnostic Report Output Card with Voice Readout
        state.currentResult?.let { result ->
            item {
                DiagnosticResultCard(
                    result = result,
                    selectedLang = selectedLang,
                    isSpeaking = isSpeaking,
                    onSpeak = { viewModel.speakDiagnosisResult(result) },
                    onStopSpeech = { viewModel.stopSpeaking() },
                    onReset = { viewModel.resetDiagnosisForm() }
                )
            }
        }
    }

    // Live Camera Scanner Guide & Viewfinder Modal
    if (showCameraViewFinder) {
        CameraScannerModal(
            selectedLang = selectedLang,
            onDismiss = { showCameraViewFinder = false },
            onCapture = {
                showCameraViewFinder = false
                cameraLauncher.launch(null)
            },
            onSelectSample = { sampleRes ->
                try {
                    val sampleBitmap = BitmapFactory.decodeResource(context.resources, sampleRes)
                    viewModel.setCapturedImage(sampleBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                showCameraViewFinder = false
            }
        )
    }
}

@Composable
fun GpsAgroWeatherCard(
    weather: AgroWeatherCondition,
    isDetecting: Boolean,
    onRefreshGps: () -> Unit,
    onListenAdvisory: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MeyraCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MeyraGreenPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GPS Microclimate & Spray Risk",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MeyraGreenDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onListenAdvisory,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Audio Weather", tint = MeyraGreenPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onRefreshGps,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isDetecting) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh GPS", tint = MeyraGreenPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MeyraLeafMint.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Temperature", style = MaterialTheme.typography.labelSmall, color = MeyraTextMuted, fontSize = 10.sp)
                        Text("${weather.temperatureCelsius.toInt()}°C", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MeyraInfoBlue.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Humidity", style = MaterialTheme.typography.labelSmall, color = MeyraTextMuted, fontSize = 10.sp)
                        Text("${weather.humidityPercent}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (weather.fungalSporeRisk.contains("High")) MeyraAlertRed.copy(alpha = 0.15f) else MeyraGreenLight,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Spore Risk", style = MaterialTheme.typography.labelSmall, color = MeyraTextMuted, fontSize = 10.sp)
                        Text(weather.fungalSporeRisk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = if (weather.fungalSporeRisk.contains("High")) MeyraAlertRed else MeyraGreenDark)
                    }
                }
            }

            Text(
                text = "🚜 ${weather.sprayingAdvice}",
                style = MaterialTheme.typography.labelSmall,
                color = MeyraTextDark,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun DiagnosticResultCard(
    result: DiagnosticResult,
    selectedLang: MeyraLanguage,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onStopSpeech: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MeyraGreenPrimary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Badge & Voice Audio Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MeyraGreenPrimary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = result.cropName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Meyra DPHC Medical Diagnosis",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MeyraGreenPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Confidence: ${result.confidencePercent}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MeyraGreenPrimary
                    )
                }
            }

            // Voice Audio Readout Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSpeaking) MeyraGreenLight else MeyraLeafMint.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.Default.VolumeUp,
                            contentDescription = "Audio Voice",
                            tint = MeyraGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSpeaking) "Reading Out Diagnosis..." else "Listen Audio Readout",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MeyraGreenDark
                        )
                    }

                    if (isSpeaking) {
                        IconButton(onClick = onStopSpeech, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MeyraAlertRed)
                        }
                    } else {
                        IconButton(onClick = onSpeak, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MeyraGreenPrimary)
                        }
                    }
                }
            }

            Divider()

            // Title
            Text(
                text = result.diagnosisTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MeyraGreenDark
            )

            // Section 1: Explanation
            Text(
                text = result.fullExplanation,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            // Section 2: IPM Strategy
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MeyraLeafMint.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = MeyraGreenPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Integrated Pest Management (IPM)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MeyraGreenPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.ipmStrategy,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Section 3: Safe Chemical & Dosage Warning
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MeyraWarningOrange.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MeyraWarningOrange)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chemical Control & Dosage Safety",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MeyraWarningOrange
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result.safeChemicalOptions}\nSafety: ${result.dosageAndSafety}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Reset Button
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Another Crop")
            }
        }
    }
}

@Composable
fun CameraScannerModal(
    selectedLang: MeyraLanguage,
    onDismiss: () -> Unit,
    onCapture: () -> Unit,
    onSelectSample: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Leaf Camera Scanner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Viewfinder Target Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(2.dp, MeyraGreenPrimary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CropFree,
                            contentDescription = "Target Frame",
                            tint = MeyraAccentHighlight,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Align infected leaf blade inside frame",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Text(
                            text = "Ensure bright natural sunlight",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Shutter Button
                Button(
                    onClick = onCapture,
                    colors = ButtonDefaults.buttonColors(containerColor = MeyraGreenPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Launch Device Camera", fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Or pick sample leaf image for demo:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MeyraTextMuted
                )

                // Quick test sample button
                OutlinedButton(
                    onClick = { onSelectSample(R.drawable.img_hero_meyra_1786397995908) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = MeyraGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Load Sample Leaf (Coffee / Teff)")
                }
            }
        }
    }
}
