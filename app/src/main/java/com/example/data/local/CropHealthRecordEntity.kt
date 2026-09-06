package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crop_health_records")
data class CropHealthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val farmerId: String = "default_farmer",
    val cropName: String, // Teff, Maize, Coffee, Avocado, Wheat, Sorghum, Tomato, etc.
    val symptomDescription: String,
    val imagePath: String? = null,
    val diagnosisName: String,
    val confidenceScore: Int, // 0 - 100%
    val ipmTreatmentPlan: String,
    val organicControl: String,
    val chemicalControl: String,
    val dosageAndSafety: String,
    val severityLevel: String, // Low, Moderate, High, Severe
    val languageUsed: String = "EN",
    val isResolved: Boolean = false,
    val dateLogged: Long = System.currentTimeMillis()
)
