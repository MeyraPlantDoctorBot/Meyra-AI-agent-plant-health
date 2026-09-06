package com.example.data

import android.content.Context
import android.graphics.Bitmap
import com.example.data.api.DiagnosticResult
import com.example.data.api.PlantDoctorAiEngine
import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

class MeyraRepository(context: Context) {

    private val db = MeyraDatabase.getDatabase(context)
    private val dao = db.meyraDao()

    val farmerProfile: Flow<FarmerProfileEntity?> = dao.getFarmerProfile()
    val cropHealthRecords: Flow<List<CropHealthRecordEntity>> = dao.getAllCropHealthRecords()
    val postHarvestRecords: Flow<List<PostHarvestRecordEntity>> = dao.getAllPostHarvestRecords()
    val treatmentHistory: Flow<List<TreatmentHistoryEntity>> = dao.getAllTreatmentHistory()

    suspend fun saveProfile(profile: FarmerProfileEntity) {
        dao.saveFarmerProfile(profile)
    }

    suspend fun performDiagnosisAndSave(
        cropName: String,
        symptoms: String,
        bitmapImage: Bitmap? = null,
        languageCode: String = "EN",
        regionLocation: String = "Oromia, Ethiopia",
        growthStage: String = "Vegetative Stage"
    ): DiagnosticResult {
        val result = PlantDoctorAiEngine.diagnoseCropIssue(
            cropName = cropName,
            userSymptoms = symptoms,
            bitmapImage = bitmapImage,
            languageCode = languageCode,
            regionLocation = regionLocation,
            growthStage = growthStage
        )

        // Save into local Room DB for digital record trace
        val record = CropHealthRecordEntity(
            cropName = cropName,
            symptomDescription = symptoms,
            diagnosisName = result.diagnosisTitle,
            confidenceScore = result.confidencePercent,
            ipmTreatmentPlan = result.ipmStrategy,
            organicControl = result.organicOptions,
            chemicalControl = result.safeChemicalOptions,
            dosageAndSafety = result.dosageAndSafety,
            severityLevel = if (result.confidencePercent > 80) "High" else "Moderate",
            languageUsed = languageCode
        )

        val recordId = dao.insertCropHealthRecord(record)

        // Automatically log initial treatment recommendation into local Room DB
        val initialTreatment = TreatmentHistoryEntity(
            cropRecordId = recordId,
            cropName = cropName,
            treatmentName = result.organicOptions.split("\n").firstOrNull() ?: result.diagnosisTitle,
            treatmentType = "IPM Recommended",
            appliedDosage = result.dosageAndSafety.take(80),
            notes = "Recommended by Meyra AI Clinic based on diagnosis.",
            effectiveness = "Pending Application"
        )
        dao.insertTreatmentHistory(initialTreatment)

        return result
    }

    suspend fun markRecordResolved(id: Long, isResolved: Boolean) {
        dao.updateRecordStatus(id, isResolved)
    }

    suspend fun addPostHarvestRecord(record: PostHarvestRecordEntity) {
        dao.insertPostHarvestRecord(record)
    }

    suspend fun addTreatmentRecord(treatment: TreatmentHistoryEntity) {
        dao.insertTreatmentHistory(treatment)
    }
}
