package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.knowledge.MeyraKnowledgeBase
import com.example.data.knowledge.OutbreakAlert
import com.example.ui.MeyraLanguage
import com.example.ui.MeyraViewModel
import com.example.ui.theme.*

@Composable
fun OutbreakScreen(
    viewModel: MeyraViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val weather by viewModel.agroWeather.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val alerts = MeyraKnowledgeBase.activeOutbreaks

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Banner with GPS Live Threat Monitoring
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MeyraWarningOrange.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MeyraWarningOrange,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (selectedLang) {
                                MeyraLanguage.AMHARIC -> "ቅድመ-ማስጠንቀቂያ እና የተባይ ስጋት መረጃ"
                                MeyraLanguage.OROMO -> "Of Eeggannoo Duratti fi Akeekkachiisa"
                                else -> "Early Warning & Outbreak Intelligence"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MeyraWarningOrange
                        )
                        Text(
                            text = "Threat monitoring for ${weather.locationName} (${weather.altitudeMeters}m)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.speakCustomText("Active agricultural pest and disease alerts for ${weather.locationName}. High humidity ${weather.humidityPercent} percent increases risk of fungal rust.")
                        }
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.Default.VolumeUp,
                            contentDescription = "Read Alerts Out Loud",
                            tint = MeyraGreenPrimary
                        )
                    }
                }
            }
        }

        // Live Microclimate Weather Alert Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
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
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MeyraInfoBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GPS Agro-Climate Alert (${weather.locationName})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (weather.fungalSporeRisk.contains("High")) MeyraAlertRed.copy(alpha = 0.15f) else MeyraGreenLight
                        ) {
                            Text(
                                text = weather.fungalSporeRisk,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (weather.fungalSporeRisk.contains("High")) MeyraAlertRed else MeyraGreenDark
                            )
                        }
                    }

                    weather.cropAlerts.forEach { alert ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text("⚠️ ", fontSize = 12.sp)
                            Text(
                                text = alert,
                                style = MaterialTheme.typography.bodySmall,
                                color = MeyraTextDark
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Active Outbreak Warnings (${alerts.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(alerts) { alert ->
            OutbreakAlertCard(
                alert = alert,
                selectedLang = selectedLang,
                onListen = {
                    val alertText = "${alert.title}. Affecting ${alert.cropAffected} in ${alert.regionsAffected}. Action: ${alert.actionRequired}"
                    viewModel.speakCustomText(alertText)
                }
            )
        }
    }
}

@Composable
fun OutbreakAlertCard(
    alert: OutbreakAlert,
    selectedLang: MeyraLanguage,
    onListen: () -> Unit
) {
    val cardTitle = when (selectedLang) {
        MeyraLanguage.AMHARIC -> alert.titleAmharic
        MeyraLanguage.OROMO -> alert.titleOromo
        else -> alert.title
    }

    val riskColor = when (alert.riskLevel) {
        "Critical" -> MeyraAlertRed
        "High" -> MeyraWarningOrange
        else -> MeyraHarvestAmber
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, riskColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = riskColor,
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.riskLevel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onListen, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = MeyraGreenPrimary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = alert.datePosted,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = cardTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Crops Affected: ${alert.cropAffected}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MeyraGreenPrimary
            )

            Text(
                text = "Regions: ${alert.regionsAffected}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodySmall
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = riskColor.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = riskColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.actionRequired,
                        style = MaterialTheme.typography.labelSmall,
                        color = riskColor
                    )
                }
            }
        }
    }
}
