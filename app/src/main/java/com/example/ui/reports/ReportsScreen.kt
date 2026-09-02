package com.example.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.util.Strings
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    lang: String
) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val saleItems by viewModel.saleItems.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currency = settings?.currencySymbol ?: "৳"

    var selectedPeriod by remember { mutableStateOf("TODAY") } // TODAY, WEEK, MONTH, ALL

    val cutoffTime = remember(selectedPeriod) {
        val cal = Calendar.getInstance()
        when (selectedPeriod) {
            "TODAY" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            "WEEK" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.timeInMillis
            }
            "MONTH" -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                cal.timeInMillis
            }
            else -> 0L
        }
    }

    val periodSales = remember(sales, cutoffTime) {
        sales.filter { it.timestamp >= cutoffTime }
    }
    val periodExpenses = remember(expenses, cutoffTime) {
        expenses.filter { it.timestamp >= cutoffTime }
    }

    val grossRevenue = remember(periodSales) {
        periodSales.sumOf { it.grandTotal }
    }
    val totalExpenseAmount = remember(periodExpenses) {
        periodExpenses.sumOf { it.amount }
    }

    // Cost of goods sold for sales within this period
    val periodSaleIds = remember(periodSales) { periodSales.map { it.id }.toSet() }
    val costOfGoodsSold = remember(saleItems, periodSaleIds) {
        saleItems.filter { it.saleId in periodSaleIds }.sumOf { it.buyPrice * it.quantity }
    }

    val grossProfit = remember(grossRevenue, costOfGoodsSold) {
        grossRevenue - costOfGoodsSold
    }
    val netProfit = remember(grossProfit, totalExpenseAmount) {
        grossProfit - totalExpenseAmount
    }

    // Top Selling Products ranking
    val topSellingItems = remember(saleItems, periodSaleIds) {
        saleItems.filter { it.saleId in periodSaleIds }
            .groupBy { it.productName }
            .map { (name, items) ->
                Triple(name, items.sumOf { it.quantity }, items.sumOf { it.totalPrice })
            }
            .sortedByDescending { it.second }
            .take(5)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("reports", lang),
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
            // Period Selector Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val periods = listOf(
                        "TODAY" to Strings.get("period_today", lang),
                        "WEEK" to Strings.get("period_week", lang),
                        "MONTH" to Strings.get("period_month", lang),
                        "ALL" to Strings.get("period_all", lang)
                    )
                    periods.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedPeriod == key,
                            onClick = { selectedPeriod = key },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f).testTag("period_filter_$key")
                        )
                    }
                }
            }

            // Net Profit / Loss Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("net_profit_loss_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (netProfit >= 0) Color(0xFFE6F4EA) else Color(0xFFFCE8E6)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Strings.get("net_profit_loss", lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (netProfit >= 0) Color(0xFF137333) else Color(0xFFC5221F),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$currency ${String.format(Locale.US, "%.2f", netProfit)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netProfit >= 0) Color(0xFF137333) else Color(0xFFC5221F)
                                )
                            }
                            Icon(
                                imageVector = if (netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (netProfit >= 0) Color(0xFF137333) else Color(0xFFC5221F),
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (netProfit >= 0) "✅ Healthy business margin!" else "⚠️ Operating expenses exceed gross profits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (netProfit >= 0) Color(0xFF137333) else Color(0xFFC5221F)
                        )
                    }
                }
            }

            // Financial Breakdown Breakdown Rows
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = Strings.get("business_overview", lang),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        HorizontalDivider()

                        ReportRow(
                            title = Strings.get("gross_sales", lang),
                            value = "$currency ${String.format(Locale.US, "%.2f", grossRevenue)}",
                            color = MaterialTheme.colorScheme.primary
                        )
                        ReportRow(
                            title = Strings.get("cost_of_goods", lang),
                            value = "- $currency ${String.format(Locale.US, "%.2f", costOfGoodsSold)}",
                            color = Color(0xFFD97706)
                        )
                        ReportRow(
                            title = Strings.get("gross_profit", lang),
                            value = "$currency ${String.format(Locale.US, "%.2f", grossProfit)}",
                            color = if (grossProfit >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                        ReportRow(
                            title = Strings.get("operating_expenses", lang),
                            value = "- $currency ${String.format(Locale.US, "%.2f", totalExpenseAmount)}",
                            color = Color(0xFFDC2626)
                        )

                        HorizontalDivider()

                        ReportRow(
                            title = Strings.get("net_profit_loss", lang),
                            value = "$currency ${String.format(Locale.US, "%.2f", netProfit)}",
                            color = if (netProfit >= 0) Color(0xFF137333) else Color(0xFFC5221F),
                            isBold = true
                        )
                    }
                }
            }

            // Top Selling Products Section
            item {
                Text(
                    text = Strings.get("top_selling_products", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (topSellingItems.isEmpty()) {
                item {
                    Text(
                        text = "No sales in selected period.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(topSellingItems.size) { idx ->
                    val (productName, quantity, total) = topSellingItems[idx]
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("top_selling_item_$idx"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("#${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(productName, fontWeight = FontWeight.SemiBold)
                                    Text("$quantity sold", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text("$currency ${String.format(Locale.US, "%.2f", total)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportRow(
    title: String,
    value: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = if (isBold) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
