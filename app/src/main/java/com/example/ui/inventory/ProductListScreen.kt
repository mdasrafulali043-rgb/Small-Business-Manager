package com.example.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberSecondaryLight
import com.example.util.Strings
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: MainViewModel,
    lang: String,
    onOpenAddProduct: () -> Unit
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currency = settings?.currencySymbol ?: "৳"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var onlyLowStock by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToAddStock by remember { mutableStateOf<Product?>(null) }

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory, onlyLowStock) {
        products.filter { p ->
            val matchCategory = selectedCategory == "All" || p.category == selectedCategory
            val matchStock = !onlyLowStock || p.stockQuantity <= p.lowStockThreshold
            val matchQuery = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.banglaName.contains(searchQuery, ignoreCase = true) ||
                    p.skuBarcode.contains(searchQuery, ignoreCase = true)
            matchCategory && matchStock && matchQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("products_catalog", lang),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddProduct,
                modifier = Modifier.testTag("add_product_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Strings.get("search_inventory", lang)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("inventory_search_field")
            )

            // Category & Low Stock Filter Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = onlyLowStock,
                        onClick = { onlyLowStock = !onlyLowStock },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (onlyLowStock) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("⚠️ ${Strings.get("low_stock", lang)} (${lowStockProducts.size})")
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberSecondaryLight.copy(alpha = 0.3f),
                            selectedLabelColor = Color(0xFF78350F)
                        ),
                        modifier = Modifier.testTag("filter_low_stock_chip")
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = !onlyLowStock && cat == selectedCategory,
                        onClick = {
                            onlyLowStock = false
                            selectedCategory = cat
                        },
                        label = { Text(cat) }
                    )
                }
            }

            // Products List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    val isLow = product.stockQuantity <= product.lowStockThreshold
                    val profitMargin = product.sellPrice - product.buyPrice

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_card_${product.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLow) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (lang == "bn" && product.banglaName.isNotEmpty()) product.banglaName else product.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (product.banglaName.isNotEmpty() && lang != "bn") {
                                        Text(
                                            text = product.banglaName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (product.skuBarcode.isNotEmpty()) {
                                        Text(
                                            text = "Barcode: ${product.skuBarcode} • ${product.category}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Stock badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (product.stockQuantity <= 0) Color(0xFFFEE2E2)
                                    else if (isLow) Color(0xFFFEF3C7)
                                    else Color(0xFFD1FAE5)
                                ) {
                                    Text(
                                        text = "${product.stockQuantity} ${product.unit}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (product.stockQuantity <= 0) Color(0xFF991B1B)
                                        else if (isLow) Color(0xFF92400E)
                                        else Color(0xFF065F46),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Price breakdown: Cost, Selling, Margin
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${Strings.get("buy_price", lang)}: $currency ${product.buyPrice}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${Strings.get("sell_price", lang)}: $currency ${product.sellPrice}",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${Strings.get("margin", lang)}: +$currency ${String.format(Locale.US, "%.1f", profitMargin)}",
                                        color = Color(0xFF059669),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Quick add stock button
                                        IconButton(
                                            onClick = { productToAddStock = product },
                                            modifier = Modifier.size(32.dp).testTag("quick_add_stock_${product.id}")
                                        ) {
                                            Icon(Icons.Default.AddCircle, contentDescription = "Add Stock", tint = MaterialTheme.colorScheme.primary)
                                        }

                                        // Edit product button
                                        IconButton(
                                            onClick = { productToEdit = product },
                                            modifier = Modifier.size(32.dp).testTag("edit_product_${product.id}")
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
        }
    }

    // Quick Add Stock Dialog
    if (productToAddStock != null) {
        val target = productToAddStock!!
        var addQuantityText by remember { mutableStateOf("10") }

        AlertDialog(
            onDismissRequest = { productToAddStock = null },
            title = { Text("Add Stock - ${target.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Stock: ${target.stockQuantity} ${target.unit}")
                    OutlinedTextField(
                        value = addQuantityText,
                        onValueChange = { addQuantityText = it },
                        label = { Text("Quantity to Add") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_stock_qty_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = addQuantityText.toIntOrNull() ?: 0
                        if (qty > 0) {
                            viewModel.addProductStock(target.id, qty)
                            productToAddStock = null
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_stock_btn")
                ) {
                    Text("Update Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToAddStock = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Product Dialog
    if (productToEdit != null) {
        val current = productToEdit!!
        var name by remember { mutableStateOf(current.name) }
        var banglaName by remember { mutableStateOf(current.banglaName) }
        var barcode by remember { mutableStateOf(current.skuBarcode) }
        var buyPrice by remember { mutableStateOf("${current.buyPrice}") }
        var sellPrice by remember { mutableStateOf("${current.sellPrice}") }
        var stockQty by remember { mutableStateOf("${current.stockQuantity}") }
        var lowLimit by remember { mutableStateOf("${current.lowStockThreshold}") }
        var unit by remember { mutableStateOf(current.unit) }

        AlertDialog(
            onDismissRequest = { productToEdit = null },
            title = { Text(Strings.get("edit_product", lang)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name (English)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = banglaName,
                        onValueChange = { banglaName = it },
                        label = { Text("পণ্যের নাম (বাংলা)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode / SKU") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = buyPrice,
                            onValueChange = { buyPrice = it },
                            label = { Text(Strings.get("buy_price", lang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sellPrice,
                            onValueChange = { sellPrice = it },
                            label = { Text(Strings.get("sell_price", lang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockQty,
                            onValueChange = { stockQty = it },
                            label = { Text("Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
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
                        val bp = buyPrice.toDoubleOrNull() ?: current.buyPrice
                        val sp = sellPrice.toDoubleOrNull() ?: current.sellPrice
                        val sq = stockQty.toIntOrNull() ?: current.stockQuantity
                        val ll = lowLimit.toIntOrNull() ?: current.lowStockThreshold

                        viewModel.saveProduct(
                            current.copy(
                                name = name,
                                banglaName = banglaName,
                                skuBarcode = barcode,
                                buyPrice = bp,
                                sellPrice = sp,
                                stockQuantity = sq,
                                lowStockThreshold = ll,
                                unit = unit,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        productToEdit = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
