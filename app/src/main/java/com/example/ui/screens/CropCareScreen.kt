package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.MeyraLanguage
import com.example.ui.MeyraViewModel
import com.example.ui.theme.*

@Composable
fun CropCareScreen(
    viewModel: MeyraViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Ethiopian Crops, 1 = Soil & Nutrition, 2 = IPM & Organic
    val crops = MeyraKnowledgeBase.ethiopianCrops

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Screen Title
        Text(
            text = when (selectedLang) {
                MeyraLanguage.AMHARIC -> "የእፅዋት እንክብካቤ እና IPM መመሪያ"
                MeyraLanguage.OROMO -> "Qajeelfama Kunnoottii Midhaanii fi IPM"
                else -> "Crop Care & IPM Strategy Hub"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MeyraGreenDark
        )

        // Subtabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MeyraGreenPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Crops", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Soil Health", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("IPM Tactics", fontSize = 12.sp) }
            )
        }

        when (selectedTab) {
            0 -> EthiopianCropsList(crops = crops)
            1 -> SoilHealthGuide(selectedLang = selectedLang)
            2 -> IpmTacticsGuide(selectedLang = selectedLang)
        }
    }
}

@Composable
fun EthiopianCropsList(crops: List<com.example.data.knowledge.EthiopianCropInfo>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(crops) { crop ->
            var isExpanded by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MeyraCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Grass,
                                contentDescription = null,
                                tint = MeyraGreenPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = crop.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = crop.localNames,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Divider()

                            Text(
                                text = "Major Diseases & Threats:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MeyraSoilGold
                            )
                            crop.keyDiseases.forEach { disease ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FiberManualRecord, contentDescription = null, modifier = Modifier.size(8.dp), tint = MeyraAlertRed)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = disease, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Recommended IPM Strategy:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MeyraGreenPrimary
                            )
                            Text(
                                text = crop.ipmStrategy,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Post-Harvest Handling:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MeyraHarvestAmber
                            )
                            Text(
                                text = crop.postHarvestTip,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoilHealthGuide(selectedLang: MeyraLanguage) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Landscape, contentDescription = null, tint = MeyraSoilGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ethiopian Soil & Crop Nutrition",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Soil acidity in Ethiopian highlands (pH < 5.5) reduces phosphorus availability. Lime application (CaCO3) combined with bio-fertilizers increases teff and wheat yield by up to 35%.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "1. Organic Composting Protocols", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Layer crop residue, cattle manure, green leaf biomass, and wood ash in 1:1 ratio. Turn every 14 days for optimal decomposition.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "2. Micro-dosing NPSB Fertilizers", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Apply blended NPSB (Nitrogen, Phosphorus, Sulfur, Boron) near crop roots rather than broadcasting to reduce fertilizer waste.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun IpmTacticsGuide(selectedLang: MeyraLanguage) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MeyraLeafMint.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = MeyraGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Push-Pull Technology for Maize",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Intercrop maize with Silverleaf Desmodium (pushes pests away) and plant Napier grass along field borders (attracts and traps stemborers and Fall Armyworm).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Biological Control & Trichoderma", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Trichoderma bio-fungicides applied to soil suppress Phytophthora root rot in Avocado and Fusarium wilt in horticultural crops.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
