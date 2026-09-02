package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Sale
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.*
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    lang: String,
    onOpenInvoice: (Sale) -> Unit,
    onOpenAddProduct: () -> Unit,
    onOpenAddCustomer: () -> Unit,
    onOpenAddExpense: () -> Unit
) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val saleItems by viewModel.saleItems.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentStaff by viewModel.currentStaff.collectAsStateWithLifecycle()
    val currency = settings?.currencySymbol ?: "৳"

    // Calculate today's time range
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    val startOfToday = calendar.timeInMillis

    val todaySales = remember(sales, startOfToday) {
        sales.filter { it.timestamp >= startOfToday }
    }
    val todaySalesTotal = remember(todaySales) {
        todaySales.sumOf { it.grandTotal }
    }

    val todayExpenses = remember(expenses, startOfToday) {
        expenses.filter { it.timestamp >= startOfToday }
    }
    val todayExpenseTotal = remember(todayExpenses) {
        todayExpenses.sumOf { it.amount }
    }

    // Profit calculation: (Today's sales revenue) - (Cost of goods sold for today's sales) - (Today's expenses)
    val todayCostOfGoods = remember(todaySales, saleItems) {
        val todaySaleIds = todaySales.map { it.id }.toSet()
        saleItems.filter { it.saleId in todaySaleIds }.sumOf { it.buyPrice * it.quantity }
    }
    val todayGrossProfit = remember(todaySalesTotal, todayCostOfGoods) {
        todaySalesTotal - todayCostOfGoods
    }
    val todayNetProfit = remember(todayGrossProfit, todayExpenseTotal) {
        todayGrossProfit - todayExpenseTotal
    }

    val totalDues = remember(customers) {
        customers.sumOf { it.totalDue }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = settings?.businessName ?: Strings.get("app_title", lang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${Strings.get("switch_staff", lang)}: ${currentStaff?.name ?: "Admin"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Security PIN lock button
                    IconButton(
                        onClick = { viewModel.lockApp() },
                        modifier = Modifier.testTag("lock_app_btn")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock App")
                    }

                    // Language switcher button
                    TextButton(
                        onClick = {
                            val newLang = if (lang == "bn") "en" else "bn"
                            viewModel.setLanguage(newLang)
                        },
                        modifier = Modifier.testTag("switch_lang_btn")
                    ) {
                        Text(
                            text = if (lang == "bn") "English" else "বাংলা",
                            fontWeight = FontWeight.Bold
                        )
                    }
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
            // KPI Summary Grid (4 Cards)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            modifier = Modifier.weight(1f).testTag("kpi_today_sales"),
                            title = Strings.get("todays_sales", lang),
                            value = "$currency ${String.format(Locale.US, "%.0f", todaySalesTotal)}",
                            subtext = "${todaySales.size} ${if (lang == "bn") "টি অর্ডার" else "orders"}",
                            icon = Icons.Default.PointOfSale,
                            backgroundColor = EmeraldContainerLight,
                            iconColor = EmeraldPrimaryLight
                        )
                        KpiCard(
                            modifier = Modifier.weight(1f).testTag("kpi_today_profit"),
                            title = Strings.get("todays_profit", lang),
                            value = "$currency ${String.format(Locale.US, "%.0f", todayNetProfit)}",
                            subtext = if (todayNetProfit >= 0) "Net Profit" else "Net Loss",
                            icon = Icons.Default.TrendingUp,
                            backgroundColor = AmberContainerLight,
                            iconColor = AmberSecondaryLight
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            modifier = Modifier.weight(1f).testTag("kpi_today_expenses"),
                            title = Strings.get("todays_expenses", lang),
                            value = "$currency ${String.format(Locale.US, "%.0f", todayExpenseTotal)}",
                            subtext = "${todayExpenses.size} ${if (lang == "bn") "টি এন্ট্রি" else "entries"}",
                            icon = Icons.Default.ReceiptLong,
                            backgroundColor = Color(0xFFFEE2E2),
                            iconColor = ColorDue
                        )
                        KpiCard(
                            modifier = Modifier.weight(1f).testTag("kpi_total_due"),
                            title = Strings.get("total_due_balance", lang),
                            value = "$currency ${String.format(Locale.US, "%.0f", totalDues)}",
                            subtext = "${customers.count { it.totalDue > 0 }} ${if (lang == "bn") "জন বাকিদার" else "customers"}",
                            icon = Icons.Default.AccountBalanceWallet,
                            backgroundColor = Color(0xFFE0E7FF),
                            iconColor = ColorBank
                        )
                    }
                }
            }

            // Quick Actions Horizontal Carousel
            item {
                Text(
                    text = Strings.get("quick_actions", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        QuickActionChip(
                            label = Strings.get("new_sale_action", lang),
                            icon = Icons.Default.ShoppingCartCheckout,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            tag = "quick_new_sale",
                            onClick = { viewModel.navigateTo(Screen.POS) }
                        )
                    }
                    item {
                        QuickActionChip(
                            label = Strings.get("add_product_action", lang),
                            icon = Icons.Default.AddBox,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tag = "quick_add_product",
                            onClick = onOpenAddProduct
                        )
                    }
                    item {
                        QuickActionChip(
                            label = Strings.get("add_customer_action", lang),
                            icon = Icons.Default.PersonAdd,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tag = "quick_add_customer",
                            onClick = onOpenAddCustomer
                        )
                    }
                    item {
                        QuickActionChip(
                            label = Strings.get("add_expense_action", lang),
                            icon = Icons.Default.PostAdd,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tag = "quick_add_expense",
                            onClick = onOpenAddExpense
                        )
                    }
                    item {
                        QuickActionChip(
                            label = Strings.get("loyalty_program", lang),
                            icon = Icons.Default.Stars,
                            containerColor = Color(0xFFFFF8E1),
                            contentColor = Color(0xFFE65100),
                            tag = "quick_loyalty_program",
                            onClick = { viewModel.navigateTo(Screen.LOYALTY) }
                        )
                    }
                }
            }

            // Low Stock Warning Banner
            if (lowStockProducts.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.INVENTORY) }
                            .testTag("low_stock_banner"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Strings.get("low_stock_alert", lang),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = "${lowStockProducts.size} ${Strings.get("items_need_reorder", lang)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFD97706)
                            )
                        }
                    }
                }
            }

            // Recent Transactions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("recent_sales", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.SALES_HISTORY) },
                        modifier = Modifier.testTag("view_all_sales_btn")
                    ) {
                        Text(Strings.get("view_all", lang))
                    }
                }
            }

            if (sales.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Strings.get("no_recent_sales", lang),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(sales.take(5)) { sale ->
                    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(sale.timestamp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenInvoice(sale) }
                            .testTag("recent_sale_${sale.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (sale.paymentMethod) {
                                            "bKash" -> ColorBkash.copy(alpha = 0.15f)
                                            "Nagad" -> ColorNagad.copy(alpha = 0.15f)
                                            "Bank" -> ColorBank.copy(alpha = 0.15f)
                                            "Due" -> ColorDue.copy(alpha = 0.15f)
                                            else -> ColorCash.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sale.paymentMethod.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = when (sale.paymentMethod) {
                                        "bKash" -> ColorBkash
                                        "Nagad" -> ColorNagad
                                        "Bank" -> ColorBank
                                        "Due" -> ColorDue
                                        else -> ColorCash
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sale.customerName,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "$dateFormatted • ${sale.invoiceNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currency ${sale.grandTotal}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (sale.paymentStatus) {
                                        "PAID" -> Color(0xFFD1FAE5)
                                        "DUE" -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFFEF3C7)
                                    }
                                ) {
                                    Text(
                                        text = sale.paymentStatus,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (sale.paymentStatus) {
                                            "PAID" -> Color(0xFF065F46)
                                            "DUE" -> Color(0xFF991B1B)
                                            else -> Color(0xFF92400E)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
