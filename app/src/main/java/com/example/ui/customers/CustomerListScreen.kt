package com.example.ui.customers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    viewModel: MainViewModel,
    lang: String,
    onOpenAddCustomer: () -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currency = settings?.currencySymbol ?: "৳"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCustomerForPayment by remember { mutableStateOf<Customer?>(null) }
    var selectedCustomerForReminder by remember { mutableStateOf<Customer?>(null) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = remember(customers, searchQuery) {
        customers.filter {
            searchQuery.isBlank() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalDueAll = remember(customers) {
        customers.sumOf { it.totalDue }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("customer_directory", lang),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddCustomer,
                modifier = Modifier.testTag("add_customer_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Dues Summary Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF2F2),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Strings.get("total_due_balance", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "$currency ${String.format(Locale.US, "%.2f", totalDueAll)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Strings.get("search_customers", lang)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("customer_search_field")
            )

            // Customer List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCustomers, key = { it.id }) { customer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_card_${customer.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                 Column(modifier = Modifier.weight(1f)) {
                                     Text(
                                         text = customer.name,
                                         fontWeight = FontWeight.Bold,
                                         style = MaterialTheme.typography.bodyLarge
                                     )
                                     if (customer.phone.isNotEmpty()) {
                                         Text(
                                             text = customer.phone,
                                             style = MaterialTheme.typography.bodySmall,
                                             color = MaterialTheme.colorScheme.onSurfaceVariant
                                         )
                                     }
                                     val tier = com.example.data.model.LoyaltyTier.fromPoints(customer.loyaltyPoints)
                                     Row(
                                         verticalAlignment = Alignment.CenterVertically,
                                         modifier = Modifier.padding(top = 2.dp)
                                     ) {
                                         Surface(
                                             shape = RoundedCornerShape(8.dp),
                                             color = Color(0xFFFFF8E1)
                                         ) {
                                             Row(
                                                 modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                 verticalAlignment = Alignment.CenterVertically
                                             ) {
                                                 Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                                 Spacer(modifier = Modifier.width(3.dp))
                                                 Text(
                                                     "${customer.loyaltyPoints} pts",
                                                     fontSize = 11.sp,
                                                     fontWeight = FontWeight.Bold,
                                                     color = Color(0xFFE65100)
                                                 )
                                                 Spacer(modifier = Modifier.width(4.dp))
                                                 Text(
                                                     if (lang == "bn") tier.titleBn else tier.titleEn,
                                                     fontSize = 10.sp,
                                                     fontWeight = FontWeight.Medium,
                                                     color = MaterialTheme.colorScheme.onSurfaceVariant
                                                 )
                                             }
                                         }
                                     }
                                 }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$currency ${customer.totalDue}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (customer.totalDue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = if (customer.totalDue > 0) Strings.get("total_due", lang) else "No Due",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (customer.totalDue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (customer.address.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = customer.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            // Action Buttons: Collect Due, Send Reminder, Edit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (customer.totalDue > 0) {
                                    Button(
                                        onClick = { selectedCustomerForPayment = customer },
                                        modifier = Modifier.weight(1f).testTag("collect_due_btn_${customer.id}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(Strings.get("collect_due", lang), fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { selectedCustomerForReminder = customer },
                                        modifier = Modifier.weight(1f).testTag("send_reminder_btn_${customer.id}"),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SMS", fontSize = 12.sp)
                                    }
                                }

                                IconButton(
                                    onClick = { customerToEdit = customer },
                                    modifier = Modifier.size(36.dp).testTag("edit_customer_btn_${customer.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Collect Due Payment Dialog
    if (selectedCustomerForPayment != null) {
        val target = selectedCustomerForPayment!!
        var payAmountText by remember { mutableStateOf("${target.totalDue}") }
        var payMethod by remember { mutableStateOf("Cash") }
        var payNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedCustomerForPayment = null },
            title = {
                Text("${Strings.get("collect_due", lang)} - ${target.name}")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${Strings.get("total_due", lang)}: $currency ${target.totalDue}",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = { payAmountText = it },
                        label = { Text(Strings.get("paid_amount", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("collect_due_amount_input")
                    )

                    Text(Strings.get("payment_method", lang), style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Cash", "bKash", "Nagad", "Bank").forEach { m ->
                            val isSel = payMethod == m
                            FilterChip(
                                selected = isSel,
                                onClick = { payMethod = m },
                                label = { Text(m, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = payNote,
                        onValueChange = { payNote = it },
                        label = { Text("Note / Transaction ID (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.collectCustomerDue(
                                customerId = target.id,
                                customerName = target.name,
                                amount = amount,
                                method = payMethod,
                                note = payNote
                            )
                            selectedCustomerForPayment = null
                        }
                    },
                    modifier = Modifier.testTag("confirm_collect_due_btn")
                ) {
                    Text("Save & Collect")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCustomerForPayment = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Send SMS Reminder Dialog
    if (selectedCustomerForReminder != null) {
        val target = selectedCustomerForReminder!!
        val storeName = settings?.businessName ?: "Our Store"
        val reminderMessage = if (lang == "bn") {
            "সম্মানিত গ্রাহক ${target.name}, $storeName-এ আপনার বর্তমান বাকি $currency ${target.totalDue} টাকা। অনুগ্রহ করে দ্রুত পরিশোধ করুন। ধন্যবাদ।"
        } else {
            "Dear ${target.name}, your due balance at $storeName is $currency ${target.totalDue}. Kindly clear this at your earliest convenience. Thank you."
        }

        AlertDialog(
            onDismissRequest = { selectedCustomerForReminder = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Strings.get("send_reminder", lang))
                }
            },
            text = {
                Column {
                    Text(
                        text = "Recipient: ${target.name} (${target.phone})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reminderMessage,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (target.phone.isNotEmpty()) {
                            val smsUri = Uri.parse("smsto:${target.phone}")
                            val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                                putExtra("sms_body", reminderMessage)
                            }
                            context.startActivity(intent)
                        }
                        selectedCustomerForReminder = null
                    },
                    modifier = Modifier.testTag("open_sms_app_btn")
                ) {
                    Text("Send via SMS App")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCustomerForReminder = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Customer Dialog
    if (customerToEdit != null) {
        val current = customerToEdit!!
        var name by remember { mutableStateOf(current.name) }
        var phone by remember { mutableStateOf(current.phone) }
        var address by remember { mutableStateOf(current.address) }

        AlertDialog(
            onDismissRequest = { customerToEdit = null },
            title = { Text("Edit Customer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.saveCustomer(
                                current.copy(name = name, phone = phone, address = address)
                            )
                            customerToEdit = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
