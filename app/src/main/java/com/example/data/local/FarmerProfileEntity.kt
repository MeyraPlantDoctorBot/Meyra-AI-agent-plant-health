package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmer_profiles")
data class FarmerProfileEntity(
    @PrimaryKey val id: String = "default_farmer",
    val name: String = "Abebe Bikila",
    val phone: String = "+251 91 123 4567",
    val region: String = "Oromia",
    val zoneWoreda: String = "Jimma, Mana Woreda",
    val farmSizeHectares: Double = 1.5,
    val primaryCrops: String = "Coffee, Maize, Avocado, Teff",
    val preferredLanguage: String = "OM", // OM = Afaan Oromo, AM = Amharic, EN = English
    val registeredDate: Long = System.currentTimeMillis()
)
