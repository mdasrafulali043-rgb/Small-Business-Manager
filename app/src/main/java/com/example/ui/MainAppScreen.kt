package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BusinessSettings
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.ui.auth.PinLockScreen
import com.example.ui.customers.CustomerListScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dialogs.AddCustomerDialog
import com.example.ui.dialogs.AddExpenseDialog
import com.example.ui.dialogs.AddProductDialog
import com.example.ui.expenses.ExpenseListScreen
import com.example.ui.inventory.ProductListScreen
import com.example.ui.loyalty.LoyaltyScreen
import com.example.ui.pos.PosScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.sales.InvoiceDialog
import com.example.ui.sales.SalesHistoryScreen
import com.example.ui.settings.SettingsScreen
import com.example.util.Strings

data class NavItem(
    val screen: Screen,
    val titleKey: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun MainAppScreen(
    viewModel: MainViewModel
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val lastCompletedSale by viewModel.lastCompletedSale.collectAsStateWithLifecycle()
    val saleItems by viewModel.saleItems.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var viewingInvoiceSale by remember { mutableStateOf<Sale?>(null) }

    // If PIN is locked
    if (isLocked) {
        PinLockScreen(viewModel = viewModel, lang = lang)
        return
    }

    val navItems = listOf(
        NavItem(Screen.DASHBOARD, "dashboard", Icons.Default.Dashboard, "nav_dashboard"),
        NavItem(Screen.POS, "pos", Icons.Default.PointOfSale, "nav_pos"),
        NavItem(Screen.CUSTOMERS, "customers", Icons.Default.People, "nav_customers"),
        NavItem(Screen.INVENTORY, "inventory", Icons.Default.Inventory2, "nav_inventory"),
        NavItem(Screen.REPORTS, "reports", Icons.Default.BarChart, "nav_reports"),
        NavItem(Screen.SETTINGS, "settings", Icons.Default.Settings, "nav_settings")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                tonalElevation = 6.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(item.screen) },
                        icon = {
                            if (item.screen == Screen.INVENTORY && lowStockProducts.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${lowStockProducts.size}") } }) {
                                    Icon(item.icon, contentDescription = Strings.get(item.titleKey, lang))
                                }
                            } else {
                                Icon(item.icon, contentDescription = Strings.get(item.titleKey, lang))
                            }
                        },
                        label = {
                            Text(
                                text = Strings.get(item.titleKey, lang),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag(item.testTag)
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
            when (currentScreen) {
                Screen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    lang = lang,
                    onOpenInvoice = { viewingInvoiceSale = it },
                    onOpenAddProduct = { showAddProductDialog = true },
                    onOpenAddCustomer = { showAddCustomerDialog = true },
                    onOpenAddExpense = { showAddExpenseDialog = true }
                )
                Screen.POS -> PosScreen(
                    viewModel = viewModel,
                    lang = lang
                )
                Screen.SALES_HISTORY -> SalesHistoryScreen(
                    viewModel = viewModel,
                    lang = lang,
                    onOpenInvoice = { viewingInvoiceSale = it }
                )
                Screen.CUSTOMERS -> CustomerListScreen(
                    viewModel = viewModel,
                    lang = lang,
                    onOpenAddCustomer = { showAddCustomerDialog = true }
                )
                Screen.INVENTORY -> ProductListScreen(
                    viewModel = viewModel,
                    lang = lang,
                    onOpenAddProduct = { showAddProductDialog = true }
                )
                Screen.EXPENSES -> ExpenseListScreen(
                    viewModel = viewModel,
                    lang = lang,
                    onOpenAddExpense = { showAddExpenseDialog = true }
                )
                Screen.REPORTS -> ReportsScreen(
                    viewModel = viewModel,
                    lang = lang
                )
                Screen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    lang = lang
                )
                Screen.LOYALTY -> LoyaltyScreen(
                    viewModel = viewModel,
                    lang = lang
                )
            }
        }
    }

    // Common Dialogs
    if (showAddProductDialog) {
        AddProductDialog(
            viewModel = viewModel,
            lang = lang,
            onDismiss = { showAddProductDialog = false }
        )
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            viewModel = viewModel,
            lang = lang,
            onDismiss = { showAddCustomerDialog = false }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            viewModel = viewModel,
            lang = lang,
            onDismiss = { showAddExpenseDialog = false }
        )
    }

    // Last completed sale invoice dialog
    if (lastCompletedSale != null) {
        val (sale, items) = lastCompletedSale!!
        InvoiceDialog(
            sale = sale,
            items = items,
            settings = settings ?: BusinessSettings(),
            lang = lang,
            onDismiss = { viewModel.dismissLastCompletedSale() }
        )
    }

    // Viewing historical invoice dialog
    if (viewingInvoiceSale != null) {
        val targetSale = viewingInvoiceSale!!
        val itemsForSale = saleItems.filter { it.saleId == targetSale.id }
        InvoiceDialog(
            sale = targetSale,
            items = itemsForSale,
            settings = settings ?: BusinessSettings(),
            lang = lang,
            onDismiss = { viewingInvoiceSale = null }
        )
    }
}
