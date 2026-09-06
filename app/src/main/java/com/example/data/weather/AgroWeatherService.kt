package com.example.data.weather

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.roundToInt

data class AgroWeatherCondition(
    val locationName: String = "Jimma, Oromia",
    val latitude: Double = 7.6734,
    val longitude: Double = 36.8344,
    val altitudeMeters: Int = 1780,
    val temperatureCelsius: Double = 23.5,
    val humidityPercent: Int = 82,
    val windSpeedKmh: Double = 6.2,
    val rainfallChancePercent: Int = 65,
    val conditionDescription: String = "Humid & Partly Cloudy",
    val fungalSporeRisk: String = "High Risk", // Low, Moderate, High, Severe
    val sprayingAdvice: String = "Optimal spray window: Early morning (06:30 - 09:30 AM). Avoid spraying after 11 AM due to rain forecast.",
    val cropAlerts: List<String> = listOf(
        "Coffee: Elevated risk of Coffee Berry Disease & Rust due to 82% humidity.",
        "Wheat & Teff: Humid conditions favorable for rust spore germination."
    )
)

class AgroWeatherService(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            suspendCancellableCoroutine { cont ->
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            cont.resume(loc)
                        } else {
                            // Fallback to legacy LocationManager
                            val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                            val gpsLoc = locManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                ?: locManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            cont.resume(gpsLoc)
                        }
                    }
                    .addOnFailureListener {
                        try {
                            val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                            val fallback = locManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                ?: locManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            cont.resume(fallback)
                        } catch (e: Exception) {
                            cont.resume(null)
                        }
                    }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getAgroWeatherForCoordinates(latitude: Double, longitude: Double): AgroWeatherCondition {
        // Map Ethiopian coordinates to regional agro-ecological zones
        val (zoneName, baseAlt, baseTemp, baseHumid) = when {
            // Jimma / Keffa / Southwestern zone (Coffee zone)
            latitude in 6.5..8.5 && longitude in 35.5..38.0 -> {
                Quadruple("Jimma & Keffa Zone, Oromia", 1780, 23.0, 84)
            }
            // Central Highlands / East Shewa (Bishoftu, Mojo, Adama - Teff & Wheat)
            latitude in 8.3..9.5 && longitude in 38.5..40.0 -> {
                Quadruple("East Shewa, Central Highlands", 1920, 25.5, 68)
            }
            // Sidama / Yirgacheffe / Southern Highlands (High grade Coffee)
            latitude in 6.0..7.5 && longitude in 38.0..39.5 -> {
                Quadruple("Sidama & Gedeo Highlands", 1890, 22.0, 86)
            }
            // Amhara / Gojjam / Gondar (Teff, Wheat, Maize)
            latitude in 10.0..12.5 && longitude in 36.5..38.5 -> {
                Quadruple("West Gojjam & Tana Basin", 1820, 24.0, 75)
            }
            // Tigray / Northern drylands (Sorghum, Teff, Chickpea)
            latitude in 12.5..14.5 && longitude in 37.5..39.5 -> {
                Quadruple("Tigray Plateau & Drylands", 2100, 27.0, 52)
            }
            // Rift Valley (Hawassa, Ziway, Arsi)
            latitude in 7.0..8.2 && longitude in 38.2..39.2 -> {
                Quadruple("Rift Valley Agricultural Belt", 1680, 26.5, 62)
            }
            else -> {
                Quadruple("Oromia & Shewa Region", 1850, 24.5, 78)
            }
        }

        val riskLevel = when {
            baseHumid >= 80 -> "High Risk"
            baseHumid >= 65 -> "Moderate Risk"
            else -> "Low Risk"
        }

        val sprayingAdvice = if (baseHumid >= 80) {
            "Optimal spray window: Early morning (06:30 - 09:30 AM). High moisture expected in afternoon."
        } else {
            "Excellent spraying conditions. Low wind drift (< 8 km/h). Safe for foliar bio-fertilizer & neem oil."
        }

        val cropAlerts = mutableListOf<String>()
        if (baseHumid >= 75) {
            cropAlerts.add("Coffee: High humidity elevates risk of Coffee Leaf Rust (Hemileia vastatrix) & CBD.")
            cropAlerts.add("Maize & Sorghum: Watch for Northern Corn Leaf Blight in humid lowlands.")
            cropAlerts.add("Tomato: Preventative copper fungicide recommended against Late Blight.")
        } else {
            cropAlerts.add("Cereal crops: Dry conditions favorable for weeding and grain drying.")
            cropAlerts.add("Soil Moisture: Adequate for top-dressing fertilizer applications.")
        }

        return AgroWeatherCondition(
            locationName = zoneName,
            latitude = ((latitude * 1000).roundToInt()) / 1000.0,
            longitude = ((longitude * 1000).roundToInt()) / 1000.0,
            altitudeMeters = baseAlt,
            temperatureCelsius = baseTemp,
            humidityPercent = baseHumid,
            windSpeedKmh = 6.8,
            rainfallChancePercent = if (baseHumid >= 80) 65 else 30,
            conditionDescription = if (baseHumid >= 80) "Humid & Overcast" else "Partly Sunny & Mild",
            fungalSporeRisk = riskLevel,
            sprayingAdvice = sprayingAdvice,
            cropAlerts = cropAlerts
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
