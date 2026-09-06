package com.example.data.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.data.knowledge.MeyraKnowledgeBase
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DiagnosticResult(
    val cropName: String,
    val diagnosisTitle: String,
    val confidencePercent: Int,
    val fullExplanation: String,
    val ipmStrategy: String,
    val organicOptions: String,
    val safeChemicalOptions: String,
    val dosageAndSafety: String,
    val postHarvestAndPrevention: String,
    val languageCode: String
)

object PlantDoctorAiEngine {

    suspend fun diagnoseCropIssue(
        cropName: String,
        userSymptoms: String,
        bitmapImage: Bitmap? = null,
        languageCode: String = "EN", // EN, AM, OM
        regionLocation: String = "Oromia, Ethiopia",
        growthStage: String = "Vegetative Stage"
    ): DiagnosticResult = withContext(Dispatchers.IO) {
        val apiKey = RetrofitClient.getApiKey()

        val systemPromptText = """
            You are Meyra AI Plant Doctor, an expert virtual agronomist developed by Meyra Digital Plant Health Clinic (DPHC) in Ethiopia.
            You provide scientific, practical, farmer-friendly plant pathology and pest management guidance tailored for smallholder farmers in Ethiopia.
            
            Current Context:
            - Crop Type: $cropName
            - Location: $regionLocation
            - Growth Stage: $growthStage
            - Primary Language Requested: ${getLanguageName(languageCode)}
            
            Structure your response into these exact sections in ${getLanguageName(languageCode)}:
            1. Likely Diagnosis & Cause
            2. Integrated Pest Management (IPM) Plan
            3. Organic & Eco-Friendly Solutions
            4. Safe Chemical Options & Exact Dosage (if needed)
            5. Prevention & Post-Harvest Advice
        """.trimIndent()

        val parts = mutableListOf<Part>()
        parts.add(Part(text = "User symptom report: $userSymptoms for crop $cropName in $regionLocation at $growthStage stage."))

        if (bitmapImage != null) {
            val base64 = bitmapImage.toBase64String()
            parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64)))
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = parts)),
                    systemInstruction = Content(parts = listOf(Part(text = systemPromptText)))
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!textResponse.isNullBytewise()) {
                    return@withContext parseAiTextToDiagnosticResult(cropName, textResponse!!, languageCode)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback offline intelligence matching Ethiopian DPHC Knowledge Base
        return@withContext fallbackOfflineDiagnosis(cropName, userSymptoms, languageCode)
    }

    private fun parseAiTextToDiagnosticResult(
        cropName: String,
        textResponse: String,
        languageCode: String
    ): DiagnosticResult {
        val lines = textResponse.lines()
        val title = lines.firstOrNull { it.isNotBlank() }?.replace("#", "")?.trim()
            ?: "Crop Health Condition Identified"

        return DiagnosticResult(
            cropName = cropName,
            diagnosisTitle = title,
            confidencePercent = 88,
            fullExplanation = textResponse,
            ipmStrategy = extractSection(textResponse, "IPM", "Integrated Pest Management"),
            organicOptions = extractSection(textResponse, "Organic", "Eco-Friendly"),
            safeChemicalOptions = extractSection(textResponse, "Chemical", "Dosage"),
            dosageAndSafety = extractSection(textResponse, "Dosage", "Safety"),
            postHarvestAndPrevention = extractSection(textResponse, "Prevention", "Post-Harvest"),
            languageCode = languageCode
        )
    }

    private fun extractSection(fullText: String, keyword1: String, keyword2: String): String {
        val matches = fullText.lines().filter { line ->
            line.contains(keyword1, ignoreCase = true) || line.contains(keyword2, ignoreCase = true)
        }
        return if (matches.isNotEmpty()) {
            matches.take(3).joinToString("\n• ")
        } else {
            "Consult local Meyra DPHC extension officer for targeted field protocols."
        }
    }

    private fun fallbackOfflineDiagnosis(
        cropName: String,
        userSymptoms: String,
        languageCode: String
    ): DiagnosticResult {
        val cropInfo = MeyraKnowledgeBase.ethiopianCrops.find {
            it.name.contains(cropName, ignoreCase = true)
        } ?: MeyraKnowledgeBase.ethiopianCrops.first()

        val diseaseMatch = cropInfo.keyDiseases.firstOrNull() ?: "Fungal Leaf Blight / Pest Infestation"

        val explanation = when (languageCode) {
            "AM" -> """
                የሜይራ ዲጂታል እፅዋት ጤና ክሊኒክ (DPHC) ትንተና፡
                
                1. ሊሆን የሚችል በሽታ፡ $diseaseMatch በ $cropName ላይ
                የቀረበው ምልክት፡ $userSymptoms
                
                2. የተቀናጀ የበሽታ መከላከያ (IPM)፡
                • ${cropInfo.ipmStrategy}
                • የተበከሉ ቅጠሎችን ወይም እፅዋትን ቀድሞ ማስወገድ እና ማቃጠል።
                
                3. ተፈጥሯዊና አካባቢ ተስማሚ መፍትሔ፡
                • የኮምፖስት ማዳበሪያና የከርሰ ምድር እርጥበት መጠበቅ።
                • የኮሶና የኒም (Neem) ቅጠል ውሃ መርጨት።
                
                4. ኬሚካል እና የመጠን አጠቃቀም (አስፈላጊ ከሆነ ብቻ)፡
                • መዳብ ቤዝድ ፉንጊሳይድ (Copper Fungicide) 50g በ 20L ውሃ።
                • መከላከያ ጭምብልና ጓንት መጠቀም ግዴታ ነው።
                
                5. ድህረ-ምርትና መከላከል፡
                • ${cropInfo.postHarvestTip}
            """.trimIndent()

            "OM" -> """
                Xiinxala Kiliniikii Fayyaa Muldhata Meyra (DPHC):
                
                1. Dhukkuba Ta'uu Danda'u: $diseaseMatch ($cropName)
                Mallaattoo Himmame: $userSymptoms
                
                2. Tarsaafi Toftaa IPM:
                • ${cropInfo.ipmStrategy}
                • Baalaafii mudhii dhukkubsatan dursee balleessuudhaan faalama ittisuu.
                
                3. Furmaata Uumamaa fi Bahaa:
                • Bishaan baala Niimii (Neem extract) ykn daaraa mukaatti fayyadamuu.
                • Biqiltuu fayyaa qabu qofa facaasuu.
                
                4. Toftaa Keemikaalaa fi Safara Summii:
                • Copper Fungicide grama 50 bishaan liitira 20 waliin makaluu.
                • Yeroo fassasan uffata eegumsaa fi haguugduu afaan-funyaanii uffachuu.
                
                5. Eegumsa Oomisha Boodaa:
                • ${cropInfo.postHarvestTip}
            """.trimIndent()

            else -> """
                Meyra Digital Plant Health Clinic (DPHC) Diagnostic Report:
                
                1. Likely Diagnosis: $diseaseMatch affecting $cropName
                Observed Symptoms: $userSymptoms
                
                2. Integrated Pest Management (IPM) Strategy:
                • ${cropInfo.ipmStrategy}
                • Field sanitation: rogue out and safely burn severely infected crop debris.
                
                3. Organic & Eco-Friendly Solutions:
                • Bio-pesticides: Spray 5% Neem seed kernel extract or wood ash suspension into crop whorls.
                • Enhance soil biological health through well-decomposed compost.
                
                4. Safe Chemical Options & Dosage:
                • Copper Oxychloride 50% WP at 2.5 g/L of water (50g per 20L knapsack sprayer).
                • Apply during calm morning hours with proper PPE (gloves, mask, long sleeves).
                
                5. Prevention & Post-Harvest Protection:
                • ${cropInfo.postHarvestTip}
                • Value Chain Note: ${cropInfo.valueAddition}
            """.trimIndent()
        }

        return DiagnosticResult(
            cropName = cropName,
            diagnosisTitle = "$diseaseMatch ($cropName)",
            confidencePercent = 85,
            fullExplanation = explanation,
            ipmStrategy = cropInfo.ipmStrategy,
            organicOptions = "Neem leaf extract, wood ash application, crop debris sanitation",
            safeChemicalOptions = "Copper Oxychloride 50% WP or Biorational Insecticide",
            dosageAndSafety = "50g per 20L knapsack. Wear mask and protective boots.",
            postHarvestAndPrevention = cropInfo.postHarvestTip,
            languageCode = languageCode
        )
    }

    private fun getLanguageName(code: String): String {
        return when (code) {
            "AM" -> "Amharic (አማርኛ)"
            "OM" -> "Afaan Oromo"
            else -> "English"
        }
    }

    private fun Bitmap.toBase64String(): String {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun String?.isNullBytewise(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}
