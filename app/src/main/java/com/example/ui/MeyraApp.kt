package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class MeyraScreenTab(
    val route: String,
    val titleEn: String,
    val titleAm: String,
    val titleOm: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DOCTOR(
        route = "doctor",
        titleEn = "Doctor",
        titleAm = "ዶክተር",
        titleOm = "Doktor",
        selectedIcon = Icons.Filled.Healing,
        unselectedIcon = Icons.Outlined.Healing
    ),
    CROP_CARE(
        route = "crop_care",
        titleEn = "Crop Care",
        titleAm = "እፅዋት እንክብካቤ",
        titleOm = "Kunnoottii",
        selectedIcon = Icons.Filled.Eco,
        unselectedIcon = Icons.Outlined.Eco
    ),
    POST_HARVEST(
        route = "post_harvest",
        titleEn = "Post-Harvest",
        titleAm = "ድህረ-ምርት",
        titleOm = "Gabaa",
        selectedIcon = Icons.Filled.Inventory2,
        unselectedIcon = Icons.Outlined.Inventory2
    ),
    OUTBREAKS(
        route = "outbreaks",
        titleEn = "Alerts",
        titleAm = "ማስጠንቀቂያ",
        titleOm = "Akeekkachiisa",
        selectedIcon = Icons.Filled.WarningAmber,
        unselectedIcon = Icons.Outlined.WarningAmber
    ),
    RECORDS(
        route = "records",
        titleEn = "Records",
        titleAm = "መዝገብ",
        titleOm = "Galmee",
        selectedIcon = Icons.Filled.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeyraApp(
    viewModel: MeyraViewModel = viewModel()
) {
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    var currentTab by remember { mutableStateOf(MeyraScreenTab.DOCTOR) }

    Scaffold(
        topBar = {
            MeyraTopBar(
                selectedLanguage = selectedLang,
                onLanguageSelected = { viewModel.selectLanguage(it) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, MeyraCardBorder)
            ) {
                MeyraScreenTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val tabTitle = when (selectedLang) {
                        MeyraLanguage.AMHARIC -> tab.titleAm
                        MeyraLanguage.OROMO -> tab.titleOm
                        else -> tab.titleEn
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tabTitle
                            )
                        },
                        label = {
                            Text(
                                text = tabTitle,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MeyraGreenPrimary,
                            selectedTextColor = MeyraGreenPrimary,
                            indicatorColor = MeyraGreenLight,
                            unselectedIconColor = MeyraTextMuted,
                            unselectedTextColor = MeyraTextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MeyraScreenTab.DOCTOR -> DoctorScreen(viewModel = viewModel)
                MeyraScreenTab.CROP_CARE -> CropCareScreen(viewModel = viewModel)
                MeyraScreenTab.POST_HARVEST -> PostHarvestScreen(viewModel = viewModel)
                MeyraScreenTab.OUTBREAKS -> OutbreakScreen(viewModel = viewModel)
                MeyraScreenTab.RECORDS -> RecordsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MeyraTopBar(
    selectedLanguage: MeyraLanguage,
    onLanguageSelected: (MeyraLanguage) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_meyra_icon_1786398008630),
                        contentDescription = "Meyra Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Meyra AI Plant Doctor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MeyraGreenDark
                        )
                        Text(
                            text = "Digital Plant Health Clinic (DPHC)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeyraGreenPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MeyraGreenPrimary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MeyraGreenPrimary,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Online",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeyraGreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Language Bar Selector (Afaan Oromo, Amharic, English)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MeyraLanguage.entries.forEach { lang ->
                    val isSelected = lang == selectedLanguage
                    FilterChip(
                        selected = isSelected,
                        onClick = { onLanguageSelected(lang) },
                        label = { Text(lang.nativeName, fontSize = 11.sp) },
                        modifier = Modifier.height(30.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeyraGreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}
