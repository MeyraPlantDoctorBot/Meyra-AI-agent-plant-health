package com.example.ui.screens

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
import com.example.data.local.CropHealthRecordEntity
import com.example.data.local.FarmerProfileEntity
import com.example.data.local.TreatmentHistoryEntity
import com.example.ui.MeyraLanguage
import com.example.ui.MeyraViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    viewModel: MeyraViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val records by viewModel.cropHealthRecords.collectAsState()
    val treatments by viewModel.treatmentHistory.collectAsState()
    val profile by viewModel.farmerProfile.collectAsState()

    var showProfileEditDialog by remember { mutableStateOf(false) }
    var selectedSubtab by remember { mutableStateOf(0) } // 0 = Health Records, 1 = Treatment History, 2 = Analytics & Impact

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Profile Header Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MeyraCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MeyraGreenPrimary,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${profile.zoneWoreda}, ${profile.region} • ${profile.farmSizeHectares} Ha",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Phone: ${profile.phone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeyraGreenPrimary
                        )
                    }
                }

                IconButton(onClick = { showProfileEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MeyraGreenPrimary)
                }
            }
        }

        // Subtabs: Records vs Treatments vs Analytics
        TabRow(
            selectedTabIndex = selectedSubtab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MeyraGreenPrimary
        ) {
            Tab(
                selected = selectedSubtab == 0,
                onClick = { selectedSubtab = 0 },
                text = { Text("Crop Health (${records.size})", fontSize = 11.sp) }
            )
            Tab(
                selected = selectedSubtab == 1,
                onClick = { selectedSubtab = 1 },
                text = { Text("Treatments (${treatments.size})", fontSize = 11.sp) }
            )
            Tab(
                selected = selectedSubtab == 2,
                onClick = { selectedSubtab = 2 },
                text = { Text("Analytics", fontSize = 11.sp) }
            )
        }

        when (selectedSubtab) {
            0 -> CropHealthHistoryList(
                records = records,
                onToggleResolved = { id, status -> viewModel.toggleRecordResolved(id, status) }
            )
            1 -> TreatmentHistoryList(treatments = treatments)
            2 -> AgronomistAnalyticsDashboard(records = records)
        }
    }

    if (showProfileEditDialog) {
        EditProfileDialog(
            currentProfile = profile,
            onDismiss = { showProfileEditDialog = false },
            onSave = { name, phone, region, zone, size ->
                viewModel.updateProfile(name, phone, region, zone, size)
                showProfileEditDialog = false
            }
        )
    }
}

@Composable
fun CropHealthHistoryList(
    records: List<CropHealthRecordEntity>,
    onToggleResolved: (Long, Boolean) -> Unit
) {
    if (records.isEmpty()) {
        OutlinedCard(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No crop diagnosis records stored yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(records) { record ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeyraCardBorder),
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
                            Text(
                                text = "${record.cropName} • ${record.diagnosisName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            FilterChip(
                                selected = record.isResolved,
                                onClick = { onToggleResolved(record.id, record.isResolved) },
                                label = {
                                    Text(
                                        if (record.isResolved) "Resolved" else "Active",
                                        fontSize = 10.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MeyraGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        Text(
                            text = "Symptoms: ${record.symptomDescription}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )

                        Text(
                            text = "IPM Plan: ${record.ipmTreatmentPlan}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeyraGreenPrimary
                        )

                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Text(
                            text = "Logged: ${sdf.format(Date(record.dateLogged))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TreatmentHistoryList(treatments: List<TreatmentHistoryEntity>) {
    if (treatments.isEmpty()) {
        OutlinedCard(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Healing, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No treatment history logged yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(treatments) { treatment ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeyraCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${treatment.cropName} • ${treatment.treatmentName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = CircleShape,
                                color = MeyraGreenLight
                            ) {
                                Text(
                                    text = treatment.treatmentType,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MeyraGreenDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (treatment.appliedDosage.isNotBlank()) {
                            Text(
                                text = "Dosage/Rate: ${treatment.appliedDosage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MeyraTextDark
                            )
                        }

                        if (treatment.notes.isNotBlank()) {
                            Text(
                                text = "Notes: ${treatment.notes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MeyraTextMuted
                            )
                        }

                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status: ${treatment.effectiveness}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MeyraGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Applied: ${sdf.format(Date(treatment.appliedDate))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgronomistAnalyticsDashboard(records: List<CropHealthRecordEntity>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Impact Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Loss Reduction",
                    value = "38%",
                    subtitle = "Post-harvest & disease",
                    color = MeyraGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Chemical Use",
                    value = "-42%",
                    subtitle = "IPM adoption rate",
                    color = MeyraSoilGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Farmer Consults",
                    value = "${records.size + 14}",
                    subtitle = "Digital DPHC logs",
                    color = MeyraInfoBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Income Lift",
                    value = "+22%",
                    subtitle = "Quality aggregation",
                    color = MeyraHarvestAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Cooperative Aggregation Summary Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MeyraCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = MeyraGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cooperative & DPHC Clinic Insights",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Coffee Leaf Rust and Fall Armyworm represent 65% of recorded symptoms in Jimma zone. High PICS bag adoption among smallholders prevented an estimated 1,200 kg of grain spoilage this season.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun EditProfileDialog(
    currentProfile: FarmerProfileEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, region: String, zone: String, hectares: Double) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.name) }
    var phone by remember { mutableStateOf(currentProfile.phone) }
    var region by remember { mutableStateOf(currentProfile.region) }
    var zone by remember { mutableStateOf(currentProfile.zoneWoreda) }
    var hectares by remember { mutableStateOf(currentProfile.farmSizeHectares.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Digital Farmer Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Farmer Name") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true)
                OutlinedTextField(value = region, onValueChange = { region = it }, label = { Text("Region") }, singleLine = true)
                OutlinedTextField(value = zone, onValueChange = { zone = it }, label = { Text("Zone / Woreda") }, singleLine = true)
                OutlinedTextField(value = hectares, onValueChange = { hectares = it }, label = { Text("Farm Size (Hectares)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, phone, region, zone, hectares.toDoubleOrNull() ?: 1.0)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeyraGreenPrimary)
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
