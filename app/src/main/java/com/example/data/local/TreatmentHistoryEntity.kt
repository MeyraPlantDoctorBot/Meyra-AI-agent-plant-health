package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "treatment_history")
data class TreatmentHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cropRecordId: Long = 0,
    val cropName: String,
    val treatmentName: String,
    val treatmentType: String = "IPM", // Organic, IPM, Chemical, Cultural
    val appliedDosage: String,
    val appliedDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val effectiveness: String = "Effective" // Effective, Partially Effective, Pending, Ineffective
)
