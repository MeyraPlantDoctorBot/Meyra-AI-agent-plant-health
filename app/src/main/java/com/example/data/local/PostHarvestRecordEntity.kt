package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_harvest_records")
data class PostHarvestRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cropName: String,
    val harvestQuantityKg: Double,
    val moistureContentPercent: Double,
    val storageMethod: String, // PICS Hermetic Bag, Ventilated Crate, Solar Dryer, Granary
    val expectedLossReductionPercent: Int,
    val marketTarget: String, // Local Union, Exporter, Cooperative
    val dateRecorded: Long = System.currentTimeMillis()
)
