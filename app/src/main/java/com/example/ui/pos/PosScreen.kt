package com.example.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.LoyaltyReward
import com.example.data.model.LoyaltyTier
import com.example.data.model.Product
import com.example.ui.CartEntry
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.util.Strings
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: MainViewModel,
    lang: String
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val discount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val paidAmountInput by viewModel.paidAmountInput.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val activeRewards by viewModel.activeLoyaltyRewards.collectAsStateWithLifecycle()
    val selectedLoyaltyReward by viewModel.selectedLoyaltyReward.collectAsStateWithLifecycle()
    val redeemedPoints by viewModel.redeemedPoints.collectAsStateWithLifecycle()
    val currency = settings?.currencySymbol ?: "৳"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showCheckoutSheet by remember { mutableStateOf(false) }

    // Categories extraction
    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            val matchCategory = selectedCategory == "All" || p.category == selectedCategory
            val matchQuery = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.banglaName.contains(searchQuery, ignoreCase = true) ||
                    p.skuBarcode.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    val cartSubtotal = remember(cart) {
        cart.sumOf { it.product.sellPrice * it.quantity }
    }
    val cartGrandTotal = remember(cartSubtotal, discount) {
        (cartSubtotal - discount).coerceAtLeast(0.0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("pos", lang),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Barcode Scanner button
                    IconButton(
                        onClick = { showBarcodeDialog = true },
                        modifier = Modifier.testTag("open_barcode_scanner")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
                    }

                    // Cart Icon with Badge
                    BadgedBox(
                        badge = {
                            if (cart.isNotEmpty()) {
                                Badge {
                                    Text("${cart.sumOf { it.quantity }}")
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { showCheckoutSheet = true }
                            .testTag("open_cart_btn")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                }
            )
        },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().testTag("pos_bottom_bar"),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${cart.sumOf { it.quantity }} items in Cart",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currency ${String.format(Locale.US, "%.2f", cartGrandTotal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { showCheckoutSheet = true },
                            modifier = Modifier.testTag("checkout_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Strings.get("complete_sale", lang))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search & Customer Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Customer Selector Chip
                Surface(
                    onClick = { showCustomerPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_customer_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedCustomer?.name ?: Strings.get("walk_in_customer", lang),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (selectedCustomer != null) {
                                    val tier = LoyaltyTier.fromPoints(selectedCustomer!!.loyaltyPoints)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFFF8E1)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                "${selectedCustomer!!.loyaltyPoints} pts",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }
                            }
                            if (selectedCustomer != null && selectedCustomer!!.totalDue > 0) {
                                Text(
                                    text = "${Strings.get("total_due", lang)}: $currency ${selectedCustomer!!.totalDue}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(Strings.get("search_products", lang)) },
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
                        .testTag("pos_search_input")
                )
            }

            // Category Filter Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("filter_cat_$cat")
                    )
                }
            }

            // Product Catalog List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    val inCart = cart.find { it.product.id == product.id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (product.stockQuantity > 0) {
                                    viewModel.addToCart(product)
                                }
                            }
                            .testTag("product_item_${product.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (inCart != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Product icon / category avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang == "bn" && product.banglaName.isNotEmpty()) product.banglaName else product.name,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "$currency ${product.sellPrice} / ${product.unit}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    val isLow = product.stockQuantity <= product.lowStockThreshold
                                    Text(
                                        text = "${Strings.get("stock", lang)}: ${product.stockQuantity}",
                                        color = if (product.stockQuantity <= 0) MaterialTheme.colorScheme.error
                                        else if (isLow) AmberSecondaryLight
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            // Add to cart or quantity controller
                            if (inCart != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(product.id, -1) },
                                        modifier = Modifier.size(32.dp).testTag("decrease_qty_${product.id}")
                                    ) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Minus", tint = MaterialTheme.colorScheme.error)
                                    }
                                    Text(
                                        text = "${inCart.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(product.id, 1) },
                                        enabled = inCart.quantity < product.stockQuantity,
                                        modifier = Modifier.size(32.dp).testTag("increase_qty_${product.id}")
                                    ) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Plus", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.addToCart(product) },
                                    enabled = product.stockQuantity > 0,
                                    modifier = Modifier.testTag("add_to_cart_btn_${product.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(if (product.stockQuantity > 0) "+ Add" else "Out of Stock", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Customer Picker Dialog
    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text(Strings.get("select_customer", lang)) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Surface(
                            onClick = {
                                viewModel.selectCustomer(null)
                                showCustomerPicker = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCustomer == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = Strings.get("walk_in_customer", lang),
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    items(customers) { c ->
                        Surface(
                            onClick = {
                                viewModel.selectCustomer(c)
                                showCustomerPicker = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCustomer?.id == c.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(c.name, fontWeight = FontWeight.SemiBold)
                                    Text(c.phone, style = MaterialTheme.typography.bodySmall)
                                }
                                if (c.totalDue > 0) {
                                    Text(
                                        "$currency ${c.totalDue}",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Barcode Scanner Dialog
    if (showBarcodeDialog) {
        var barcodeInput by remember { mutableStateOf("") }
        var scanFeedback by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showBarcodeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Strings.get("scan_barcode", lang))
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (lang == "bn") "ক্যামেরা বারকোড রিডার বা বারকোড নম্বর লিখুন:" else "Camera Barcode Reader or Enter Code:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Scanner simulation viewport
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.Black, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CropFree, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                            Text("Aim Camera at Barcode", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick test scan chips of available inventory
                    Text("Quick Test Codes:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        products.take(3).forEach { p ->
                            AssistChip(
                                onClick = { barcodeInput = p.skuBarcode },
                                label = { Text(p.skuBarcode, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = { barcodeInput = it },
                        label = { Text("Barcode / SKU") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("barcode_input_field")
                    )

                    if (scanFeedback != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(scanFeedback!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val matched = products.find { it.skuBarcode == barcodeInput.trim() }
                        if (matched != null) {
                            viewModel.addToCart(matched)
                            showBarcodeDialog = false
                        } else {
                            scanFeedback = if (lang == "bn") "পণ্য পাওয়া যায়নি!" else "Product not found!"
                        }
                    },
                    modifier = Modifier.testTag("scan_lookup_btn")
                ) {
                    Text("Add to Cart")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBarcodeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Checkout / Cart Bottom Sheet Dialog
    if (showCheckoutSheet) {
        var discountInput by remember { mutableStateOf(if (discount > 0) String.format(Locale.US, "%.1f", discount) else "") }
        var paidInputText by remember { mutableStateOf(if (paidAmountInput != null) "$paidAmountInput" else "") }
        var saleNotes by remember { mutableStateOf("") }
        var showLoyaltyRedeemDialog by remember { mutableStateOf(false) }

        LaunchedEffect(discount) {
            discountInput = if (discount > 0) String.format(Locale.US, "%.1f", discount) else ""
        }

        val computedGrandTotal = remember(cartSubtotal, discountInput) {
            val disc = discountInput.toDoubleOrNull() ?: 0.0
            (cartSubtotal - disc).coerceAtLeast(0.0)
        }

        val computedDue = remember(computedGrandTotal, paidInputText, paymentMethod) {
            if (paymentMethod == "Due") {
                computedGrandTotal
            } else {
                val paid = paidInputText.toDoubleOrNull() ?: computedGrandTotal
                (computedGrandTotal - paid).coerceAtLeast(0.0)
            }
        }

        Dialog(
            onDismissRequest = { showCheckoutSheet = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f)
                    .imePadding()
                    .testTag("checkout_sheet_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.get("cart", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showCheckoutSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Cart Items List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cart) { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.product.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "$currency ${entry.product.sellPrice} x ${entry.quantity} = $currency ${entry.product.sellPrice * entry.quantity}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.updateCartQuantity(entry.product.id, -1) }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(18.dp))
                                    }
                                    Text("${entry.quantity}", fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { viewModel.updateCartQuantity(entry.product.id, 1) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                     // Calculation & Payment Settings
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Customer Loyalty Card in Checkout
                        if (selectedCustomer != null) {
                            val tier = LoyaltyTier.fromPoints(selectedCustomer!!.loyaltyPoints)
                            val spendUnit = (settings?.spendPerPoint ?: 100.0).coerceAtLeast(1.0)
                            val estimatedEarned = (kotlin.math.floor(computedGrandTotal / spendUnit) * tier.pointMultiplier).toInt()

                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("pos_loyalty_summary_card"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "${selectedCustomer!!.name} (${if (lang == "bn") tier.titleBn else tier.titleEn})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text(
                                            "${selectedCustomer!!.loyaltyPoints} pts",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100),
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (lang == "bn") "এই বিক্রিতে অর্জন: +$estimatedEarned পয়েন্ট" else "Earns: +$estimatedEarned pts on order",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (selectedLoyaltyReward != null || redeemedPoints > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFE8F5E9)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "-$currency ${String.format(Locale.US, "%.1f", discount)} (${selectedLoyaltyReward?.title ?: "$redeemedPoints pts"})",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GreenPrimaryLight
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Remove Reward",
                                                        modifier = Modifier.size(14.dp).clickable { viewModel.clearLoyaltyRedemption() },
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        } else if (selectedCustomer!!.loyaltyPoints > 0) {
                                            TextButton(
                                                onClick = { showLoyaltyRedeemDialog = true },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.testTag("open_redeem_points_btn")
                                            ) {
                                                Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (lang == "bn") "পয়েন্ট রিডিম" else "Redeem Points", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${Strings.get("subtotal", lang)}:")
                            Text("$currency ${String.format(Locale.US, "%.2f", cartSubtotal)}", fontWeight = FontWeight.Bold)
                        }

                        // Discount Field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${Strings.get("discount", lang)} (৳):", modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = discountInput,
                                onValueChange = {
                                    discountInput = it
                                    val d = it.toDoubleOrNull() ?: 0.0
                                    viewModel.setDiscount(d)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(120.dp).testTag("discount_input_field"),
                                singleLine = true
                            )
                        }

                        // Payment Mode Selector (Cash, bKash, Nagad, Bank, Due)
                        Text(Strings.get("payment_method", lang), style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val methods = listOf("Cash", "bKash", "Nagad", "Bank", "Due")
                            for (m in methods) {
                                val isSel = paymentMethod == m
                                val chipBg = when (m) {
                                    "bKash" -> if (isSel) ColorBkash else ColorBkash.copy(alpha = 0.2f)
                                    "Nagad" -> if (isSel) ColorNagad else ColorNagad.copy(alpha = 0.2f)
                                    "Cash" -> if (isSel) ColorCash else ColorCash.copy(alpha = 0.2f)
                                    "Bank" -> if (isSel) ColorBank else ColorBank.copy(alpha = 0.2f)
                                    else -> if (isSel) ColorDue else ColorDue.copy(alpha = 0.2f)
                                }
                                Surface(
                                    onClick = {
                                        viewModel.setPaymentMethod(m)
                                        if (m == "Due") {
                                            paidInputText = "0"
                                            viewModel.setPaidAmount(0.0)
                                        } else {
                                            paidInputText = "$computedGrandTotal"
                                            viewModel.setPaidAmount(computedGrandTotal)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = chipBg,
                                    modifier = Modifier.weight(1f).testTag("pay_method_$m")
                                ) {
                                    Text(
                                        text = m,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Paid Amount & Due Calculation
                        if (paymentMethod != "Due") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${Strings.get("paid_amount", lang)}:", modifier = Modifier.weight(1f))
                                OutlinedTextField(
                                    value = paidInputText,
                                    onValueChange = {
                                        paidInputText = it
                                        val p = it.toDoubleOrNull()
                                        viewModel.setPaidAmount(p)
                                    },
                                    placeholder = { Text("$computedGrandTotal") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(140.dp).testTag("paid_amount_input"),
                                    singleLine = true
                                )
                            }
                        }

                        // Grand Total & Due Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${Strings.get("grand_total", lang)}:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("$currency ${String.format(Locale.US, "%.2f", computedGrandTotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        if (computedDue > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${Strings.get("due_amount", lang)}:", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                Text("$currency ${String.format(Locale.US, "%.2f", computedDue)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                viewModel.completeSale(saleNotes)
                                showCheckoutSheet = false
                            },
                            modifier = Modifier.fillMaxWidth().testTag("confirm_complete_sale_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Strings.get("complete_sale", lang))
                        }
                    }
                }
            }
        }

        // Loyalty Redemption Dialog
        if (showLoyaltyRedeemDialog && selectedCustomer != null) {
            val cust = selectedCustomer!!
            var customPointsInput by remember { mutableStateOf("") }
            val pointValueRate = settings?.pointRedeemValue ?: 1.0

            AlertDialog(
                onDismissRequest = { showLoyaltyRedeemDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFFFB300))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.get("redeem_points", lang), fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Customer Points Header
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(cust.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${Strings.get("tier", lang)}: ${LoyaltyTier.fromPoints(cust.loyaltyPoints).let { if (lang == "bn") it.titleBn else it.titleEn }}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "${cust.loyaltyPoints} pts",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "≈ $currency ${String.format(Locale.US, "%.1f", cust.loyaltyPoints * pointValueRate)}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }

                        // Section 1: Rewards from Catalog
                        if (activeRewards.isNotEmpty()) {
                            item {
                                Text(
                                    text = Strings.get("rewards_catalog", lang),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            items(activeRewards, key = { it.id }) { reward ->
                                val canRedeem = cust.loyaltyPoints >= reward.pointsRequired
                                val meetsMinOrder = reward.minPurchaseAmount <= 0 || cartSubtotal >= reward.minPurchaseAmount

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (canRedeem && meetsMinOrder) MaterialTheme.colorScheme.surfaceVariant
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (lang == "bn" && reward.banglaTitle.isNotBlank()) reward.banglaTitle else reward.title,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            val rewardLabel = when (reward.rewardType) {
                                                "DISCOUNT_PERCENT" -> "${reward.discountValue.toInt()}% ${if (lang == "bn") "ছাড়" else "Discount"}"
                                                "DISCOUNT_FIXED" -> "$currency ${reward.discountValue.toInt()} ${if (lang == "bn") "নগদ ছাড়" else "Flat Off"}"
                                                else -> if (lang == "bn") "গিফট অফার" else "Special Gift"
                                            }
                                            Text(
                                                text = "$rewardLabel • ${reward.pointsRequired} pts",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.applyLoyaltyReward(reward)
                                                showLoyaltyRedeemDialog = false
                                            },
                                            enabled = canRedeem && meetsMinOrder,
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("apply_reward_${reward.id}")
                                        ) {
                                            Text(
                                                text = if (!canRedeem) "${reward.pointsRequired - cust.loyaltyPoints} more"
                                                else (if (lang == "bn") "প্রয়োগ" else "Apply"),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Direct Points to Cash Redemption
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lang == "bn") "কাস্টম পয়েন্ট ছাড় (১ পয়েন্ট = $currency $pointValueRate)" else "Custom Points to Cash (1 pt = $currency $pointValueRate)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val quickPts = listOf(20, 50, 100).filter { it <= cust.loyaltyPoints }
                                for (qp in quickPts) {
                                    AssistChip(
                                        onClick = { customPointsInput = "$qp" },
                                        label = { Text("$qp pts", fontSize = 11.sp) }
                                    )
                                }
                                if (cust.loyaltyPoints > 0) {
                                    AssistChip(
                                        onClick = { customPointsInput = "${cust.loyaltyPoints}" },
                                        label = { Text("All (${cust.loyaltyPoints})", fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customPointsInput,
                                    onValueChange = { customPointsInput = it },
                                    label = { Text(Strings.get("points", lang)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("custom_points_input"),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val pts = customPointsInput.toIntOrNull() ?: 0
                                        if (pts > 0 && pts <= cust.loyaltyPoints) {
                                            viewModel.redeemCustomPoints(pts, pointValueRate)
                                            showLoyaltyRedeemDialog = false
                                        }
                                    },
                                    enabled = (customPointsInput.toIntOrNull() ?: 0) in 1..cust.loyaltyPoints,
                                    modifier = Modifier.testTag("apply_custom_points_btn")
                                ) {
                                    Text(if (lang == "bn") "রিডিম" else "Redeem")
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showLoyaltyRedeemDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
