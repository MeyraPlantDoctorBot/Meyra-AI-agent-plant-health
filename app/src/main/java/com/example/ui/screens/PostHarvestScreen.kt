package com.example.ui.screens

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
import com.example.data.local.PostHarvestRecordEntity
import com.example.ui.MeyraLanguage
import com.example.ui.MeyraViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostHarvestScreen(
    viewModel: MeyraViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val postHarvestLogs by viewModel.postHarvestRecords.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (selectedLang) {
                        MeyraLanguage.AMHARIC -> "ድህረ-ምርት አያያዝ እና እሴት መጨመር"
                        MeyraLanguage.OROMO -> "Eegumsa Oomisha Boodaa fi Gabaa"
                        else -> "Post-Harvest & Value Chain"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MeyraGreenDark
                )
                Text(
                    text = "Reduce storage loss • PICS Hermetic Bags • Market Access",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { showAddDialog = true },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MeyraGreenPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Storage Log", tint = Color.White)
            }
        }

        // PICS Hermetic Bag Feature Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = MeyraGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PICS Hermetic Storage Technology",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "PICS (Purdue Improved Crop Storage) triple-layer polyethylene bags eliminate 98% of grain borers and weevils without synthetic chemicals. Keeps maize and wheat fresh for up to 12 months.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Value Addition Opportunities Section
        Text(
            text = "Small-Scale Agro-Processing & Value Addition",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Avocado Oil
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MeyraCardBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MeyraHarvestAmber)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Avocado Oil", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Cold press surplus avocados into premium culinary oil.", fontSize = 11.sp, color = Color.Gray)
                }
            }

            // Card 2: Teff Packaging
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MeyraCardBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.TakeoutDining, contentDescription = null, tint = MeyraSoilGold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Teff Flour Branding", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Milled and sealed teff flour for high-value urban markets.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // Logged Harvest Batches
        Text(
            text = "Digital Post-Harvest & Storage Records",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        if (postHarvestLogs.isEmpty()) {
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No storage batches recorded yet. Tap '+' to record harvest batch.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(postHarvestLogs) { record ->
                    PostHarvestRecordRow(record = record)
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddPostHarvestDialog(
            onDismiss = { showAddDialog = false },
            onSave = { crop, kg, moisture, method ->
                viewModel.savePostHarvestRecord(crop, kg, moisture, method)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PostHarvestRecordRow(record: PostHarvestRecordEntity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MeyraCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = record.cropName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "Qty: ${record.harvestQuantityKg} kg • Moisture: ${record.moistureContentPercent}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Text(
                    text = "Storage: ${record.storageMethod}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MeyraGreenPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MeyraGreenPrimary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "-${record.expectedLossReductionPercent}% Loss",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MeyraGreenPrimary
                )
            }
        }
    }
}

@Composable
fun AddPostHarvestDialog(
    onDismiss: () -> Unit,
    onSave: (crop: String, kg: Double, moisture: Double, method: String) -> Unit
) {
    var crop by remember { mutableStateOf("Maize") }
    var kg by remember { mutableStateOf("250") }
    var moisture by remember { mutableStateOf("12.5") }
    var method by remember { mutableStateOf("PICS Hermetic Bag") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Harvest Batch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = crop,
                    onValueChange = { crop = it },
                    label = { Text("Crop Type") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = kg,
                    onValueChange = { kg = it },
                    label = { Text("Quantity (kg)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = moisture,
                    onValueChange = { moisture = it },
                    label = { Text("Moisture Content (%)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = method,
                    onValueChange = { method = it },
                    label = { Text("Storage Technology") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        crop,
                        kg.toDoubleOrNull() ?: 100.0,
                        moisture.toDoubleOrNull() ?: 12.0,
                        method
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeyraGreenPrimary)
            ) {
                Text("Save Batch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
