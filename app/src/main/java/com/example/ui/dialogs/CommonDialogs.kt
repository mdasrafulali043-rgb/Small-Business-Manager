package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.ui.MainViewModel
import com.example.util.Strings
import kotlin.random.Random

@Composable
fun AddProductDialog(
    viewModel: MainViewModel,
    lang: String,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var banglaName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Grocery") }
    var barcode by remember { mutableStateOf("890" + Random.nextInt(100000, 999999)) }
    var buyPriceText by remember { mutableStateOf("") }
    var sellPriceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("10") }
    var lowStockText by remember { mutableStateOf("5") }
    var unit by remember { mutableStateOf("Pcs") }

    val categories = listOf("Grocery", "Beverage", "Toiletries", "Clothing", "Medicine", "Stationery", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("add_product", lang)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                )
                OutlinedTextField(
                    value = banglaName,
                    onValueChange = { banglaName = it },
                    label = { Text("পণ্যের নাম (বাংলা)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_bangla_name_input")
                )

                // Category chips
                Text("Category:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                // Barcode with auto-generator button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode / SKU") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("product_barcode_input")
                    )
                    IconButton(
                        onClick = { barcode = "890" + Random.nextInt(100000, 999999) }
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = "Generate Barcode")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = buyPriceText,
                        onValueChange = { buyPriceText = it },
                        label = { Text(Strings.get("buy_price", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("buy_price_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sellPriceText,
                        onValueChange = { sellPriceText = it },
                        label = { Text(Strings.get("sell_price", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("sell_price_input"),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Stock Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("stock_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val bp = buyPriceText.toDoubleOrNull() ?: 0.0
                        val sp = sellPriceText.toDoubleOrNull() ?: 0.0
                        val sq = stockText.toIntOrNull() ?: 0
                        val ls = lowStockText.toIntOrNull() ?: 5

                        viewModel.saveProduct(
                            Product(
                                name = name,
                                banglaName = banglaName,
                                category = category,
                                skuBarcode = barcode,
                                buyPrice = bp,
                                sellPrice = sp,
                                stockQuantity = sq,
                                lowStockThreshold = ls,
                                unit = unit
                            )
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_add_product_btn")
            ) {
                Text("Add Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddCustomerDialog(
    viewModel: MainViewModel,
    lang: String,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var initialDueText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("add_customer_action", lang)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("customer_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("customer_phone_input")
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initialDueText,
                    onValueChange = { initialDueText = it },
                    label = { Text("Opening Due (পূর্বের বাকি, ঐচ্ছিক)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("customer_initial_due_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val due = initialDueText.toDoubleOrNull() ?: 0.0
                        viewModel.saveCustomer(
                            Customer(
                                name = name,
                                phone = phone,
                                address = address,
                                totalDue = due
                            )
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_add_customer_btn")
            ) {
                Text("Add Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddExpenseDialog(
    viewModel: MainViewModel,
    lang: String,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tea & Snacks") }
    var amountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var note by remember { mutableStateOf("") }

    val categories = listOf("Rent", "Electricity", "Staff Salary", "Transport", "Tea & Snacks", "Maintenance", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("record_expense", lang)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title / Purpose") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                )

                Text("Category:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                )

                Text("Payment Method:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Cash", "bKash", "Nagad", "Bank").forEach { m ->
                        FilterChip(
                            selected = paymentMethod == m,
                            onClick = { paymentMethod = m },
                            label = { Text(m, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        viewModel.addExpense(
                            title = title,
                            category = category,
                            amount = amt,
                            paymentMethod = paymentMethod,
                            note = note
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_add_expense_btn")
            ) {
                Text("Record Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
