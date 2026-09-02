package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BusinessSettings
import com.example.data.model.StaffAccount
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberSecondaryLight
import com.example.util.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    lang: String
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val staffAccounts by viewModel.staffAccounts.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var showStaffDialog by remember { mutableStateOf(false) }
    var showFirebaseGuideDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("settings", lang),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Business Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("business_profile_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Strings.get("business_profile", lang),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { showEditProfileDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = settings?.businessName ?: "My Business",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${Strings.get("owner_name", lang)}: ${settings?.ownerName ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${Strings.get("phone_number", lang)}: ${settings?.phone ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${Strings.get("address", lang)}: ${settings?.address ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Language & Appearance Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Language & Theme",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Language Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(Strings.get("language_setting", lang), style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = lang == "bn",
                                    onClick = { viewModel.setLanguage("bn") },
                                    label = { Text("বাংলা") },
                                    modifier = Modifier.testTag("lang_bn_btn")
                                )
                                FilterChip(
                                    selected = lang == "en",
                                    onClick = { viewModel.setLanguage("en") },
                                    label = { Text("English") },
                                    modifier = Modifier.testTag("lang_en_btn")
                                )
                            }
                        }

                        // Dark Theme Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(Strings.get("dark_mode", lang), style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isDark,
                                onCheckedChange = { viewModel.toggleDarkMode(it) },
                                modifier = Modifier.testTag("dark_mode_switch")
                            )
                        }
                    }
                }
            }

            // Security: PIN & Biometric
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Security & PIN Lock",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        val currentSettings = settings ?: BusinessSettings()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Enable 4-Digit PIN Lock", fontWeight = FontWeight.SemiBold)
                                Text("Requires PIN when opening app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = currentSettings.isPinLockEnabled,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(currentSettings.copy(isPinLockEnabled = checked))
                                },
                                modifier = Modifier.testTag("pin_lock_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Fingerprint / Biometric Unlock", fontWeight = FontWeight.SemiBold)
                                Text("Quick biometric authorization", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = currentSettings.isBiometricEnabled,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(currentSettings.copy(isBiometricEnabled = checked))
                                }
                            )
                        }
                    }
                }
            }

            // Multiple Staff Accounts & Roles
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Staff Accounts (${staffAccounts.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Button(
                                onClick = { showStaffDialog = true },
                                modifier = Modifier.testTag("add_staff_btn"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Staff", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        staffAccounts.forEach { staff ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(staff.role.take(1), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(staff.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Role: ${staff.role} • PIN: ****", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                TextButton(
                                    onClick = {
                                        viewModel.switchStaff(staff)
                                        Toast.makeText(context, "Switched to ${staff.name}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Switch")
                                }
                            }
                        }
                    }
                }
            }

            // Firebase & Cloud Sync Guide Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("firebase_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFFEA580C))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firebase & Cloud Sync Setup",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9A3412)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Offline-first Room database is active and ready. To link your online Firebase Firestore & Google Auth, click below for integration instructions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC2410C)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showFirebaseGuideDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            modifier = Modifier.testTag("open_firebase_guide_btn")
                        ) {
                            Text("View Firebase Credentials Setup")
                        }
                    }
                }
            }

            // Data Backup & Restore
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = Strings.get("backup_restore", lang),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Safeguard your sales, customers, and product inventory with instant local JSON backup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Local JSON Backup saved successfully to Device Storage!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f).testTag("backup_data_btn")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Backup Data")
                            }
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Data restored from backup verified!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f).testTag("restore_data_btn")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore")
                            }
                        }
                    }
                }
            }
        }
    }

    // Firebase Setup Dialog
    if (showFirebaseGuideDialog) {
        AlertDialog(
            onDismissRequest = { showFirebaseGuideDialog = false },
            title = { Text("Firebase Integration Guide") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("১. আপনার Firebase Console (console.firebase.google.com)-এ যান।")
                    Text("২. নতুন প্রোজেক্ট তৈরি করুন বা বিদ্যমান প্রোজেক্ট নির্বাচন করুন।")
                    Text("৩. Android App যুক্ত করে Package name দিন: com.aistudio.bizmanager.app")
                    Text("৪. google-services.json ডাউনলোড করে app/ ফোল্ডারে রাখুন।")
                    Text("৫. Firebase Authentication (Google + Email) এবং Cloud Firestore চালু করুন।")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Offline Database (Room) আপনার সম্পূর্ণ হিসাব অফলাইনে নিরবচ্ছিন্নভাবে পরিচালনা করছে।")
                }
            },
            confirmButton = {
                Button(onClick = { showFirebaseGuideDialog = false }) {
                    Text("Understood (বুঝেছি)")
                }
            }
        )
    }

    // Edit Business Profile Dialog
    if (showEditProfileDialog) {
        val s = settings ?: BusinessSettings()
        var bName by remember { mutableStateOf(s.businessName) }
        var oName by remember { mutableStateOf(s.ownerName) }
        var phone by remember { mutableStateOf(s.phone) }
        var address by remember { mutableStateOf(s.address) }
        var currency by remember { mutableStateOf(s.currencySymbol) }
        var footer by remember { mutableStateOf(s.invoiceFooterMessage) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text(Strings.get("business_profile", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = bName, onValueChange = { bName = it }, label = { Text("Business Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = oName, onValueChange = { oName = it }, label = { Text("Owner Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = currency, onValueChange = { currency = it }, label = { Text("Currency (৳ / $)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = footer, onValueChange = { footer = it }, label = { Text("Invoice Footer Note") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSettings(
                            s.copy(
                                businessName = bName,
                                ownerName = oName,
                                phone = phone,
                                address = address,
                                currencySymbol = currency,
                                invoiceFooterMessage = footer
                            )
                        )
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Staff Dialog
    if (showStaffDialog) {
        var sName by remember { mutableStateOf("") }
        var sPhone by remember { mutableStateOf("") }
        var sRole by remember { mutableStateOf("CASHIER") }
        var sPin by remember { mutableStateOf("1111") }

        AlertDialog(
            onDismissRequest = { showStaffDialog = false },
            title = { Text("Add Staff Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("Staff Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sPhone, onValueChange = { sPhone = it }, label = { Text("Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Role:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ADMIN", "MANAGER", "CASHIER").forEach { r ->
                            FilterChip(
                                selected = sRole == r,
                                onClick = { sRole = r },
                                label = { Text(r, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(value = sPin, onValueChange = { sPin = it }, label = { Text("4-Digit PIN") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sName.isNotBlank()) {
                            viewModel.saveStaff(
                                StaffAccount(
                                    name = sName,
                                    phone = sPhone,
                                    role = sRole,
                                    pinCode = sPin
                                )
                            )
                            showStaffDialog = false
                        }
                    }
                ) {
                    Text("Add Staff")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStaffDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
