package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppRepository
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.CustomerPayment
import com.example.data.model.Expense
import com.example.data.model.LoyaltyReward
import com.example.data.model.LoyaltyTier
import com.example.data.model.LoyaltyTransaction
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.StaffAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Screen {
    DASHBOARD,
    POS,
    SALES_HISTORY,
    CUSTOMERS,
    LOYALTY,
    INVENTORY,
    EXPENSES,
    REPORTS,
    SETTINGS
}

data class CartEntry(
    val product: Product,
    var quantity: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = AppRepository(database.appDao())
    }

    // Active Navigation
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Data streams from Room
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val saleItems: StateFlow<List<SaleItem>> = repository.allSaleItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffAccounts: StateFlow<List<StaffAccount>> = repository.allStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<BusinessSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Loyalty Streams
    val loyaltyRewards: StateFlow<List<LoyaltyReward>> = repository.allLoyaltyRewards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeLoyaltyRewards: StateFlow<List<LoyaltyReward>> = repository.activeLoyaltyRewards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loyaltyTransactions: StateFlow<List<LoyaltyTransaction>> = repository.allLoyaltyTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCustomerLoyaltyHistory(customerId: Long): Flow<List<LoyaltyTransaction>> =
        repository.getLoyaltyTransactionsForCustomer(customerId)

    // Active Staff & Authentication
    private val _currentStaff = MutableStateFlow<StaffAccount?>(null)
    val currentStaff: StateFlow<StaffAccount?> = _currentStaff.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun unlockApp(pin: String): Boolean {
        val currentSettings = settings.value
        val validPin = currentSettings?.masterPin ?: "1234"
        if (pin == validPin) {
            _isLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (settings.value?.isPinLockEnabled == true) {
            _isLocked.value = true
        }
    }

    fun switchStaff(staff: StaffAccount) {
        _currentStaff.value = staff
    }

    // Language & Theme
    private val _currentLanguage = MutableStateFlow("bn") // default Bengali
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        viewModelScope.launch {
            val s = settings.value ?: BusinessSettings()
            repository.saveSettings(s.copy(language = lang))
        }
    }

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        viewModelScope.launch {
            val s = settings.value ?: BusinessSettings()
            repository.saveSettings(s.copy(isDarkMode = enabled))
        }
    }

    // --- POS & CART STATE ---
    private val _cart = MutableStateFlow<List<CartEntry>>(emptyList())
    val cart: StateFlow<List<CartEntry>> = _cart.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Cash") // Cash, bKash, Nagad, Bank, Due
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _paidAmountInput = MutableStateFlow<Double?>(null)
    val paidAmountInput: StateFlow<Double?> = _paidAmountInput.asStateFlow()

    // Loyalty Redemption in POS
    private val _selectedLoyaltyReward = MutableStateFlow<LoyaltyReward?>(null)
    val selectedLoyaltyReward: StateFlow<LoyaltyReward?> = _selectedLoyaltyReward.asStateFlow()

    private val _redeemedPoints = MutableStateFlow(0)
    val redeemedPoints: StateFlow<Int> = _redeemedPoints.asStateFlow()

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        // Reset loyalty redemption if customer changes
        clearLoyaltyRedemption()
    }

    fun setDiscount(amount: Double) {
        _discountAmount.value = amount
    }

    fun applyLoyaltyReward(reward: LoyaltyReward) {
        val cartList = _cart.value
        val subtotal = cartList.sumOf { it.product.sellPrice * it.quantity }
        val discount = when (reward.rewardType) {
            "DISCOUNT_PERCENT" -> (subtotal * reward.discountValue / 100.0)
            "DISCOUNT_FIXED" -> reward.discountValue.coerceAtMost(subtotal)
            else -> 0.0
        }
        _selectedLoyaltyReward.value = reward
        _redeemedPoints.value = reward.pointsRequired
        _discountAmount.value = discount
    }

    fun redeemCustomPoints(points: Int, pointValueRate: Double = 1.0) {
        val cartList = _cart.value
        val subtotal = cartList.sumOf { it.product.sellPrice * it.quantity }
        val discount = (points * pointValueRate).coerceAtMost(subtotal)
        _selectedLoyaltyReward.value = null
        _redeemedPoints.value = points
        _discountAmount.value = discount
    }

    fun clearLoyaltyRedemption() {
        _selectedLoyaltyReward.value = null
        _redeemedPoints.value = 0
        _discountAmount.value = 0.0
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun setPaidAmount(amount: Double?) {
        _paidAmountInput.value = amount
    }

    fun addToCart(product: Product) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            if (existing.quantity < product.stockQuantity) {
                currentList[index] = existing.copy(quantity = existing.quantity + 1)
                _cart.value = currentList
            }
        } else {
            if (product.stockQuantity > 0) {
                currentList.add(CartEntry(product, 1))
                _cart.value = currentList
            }
        }
    }

    fun updateCartQuantity(productId: Long, delta: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val current = currentList[index]
            val newQty = current.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else if (newQty <= current.product.stockQuantity) {
                currentList[index] = current.copy(quantity = newQty)
            }
            _cart.value = currentList
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _discountAmount.value = 0.0
        _paidAmountInput.value = null
        _selectedCustomer.value = null
        _paymentMethod.value = "Cash"
        clearLoyaltyRedemption()
    }

    // Checkout / Complete Sale
    private val _lastCompletedSale = MutableStateFlow<Pair<Sale, List<SaleItem>>?>(null)
    val lastCompletedSale: StateFlow<Pair<Sale, List<SaleItem>>?> = _lastCompletedSale.asStateFlow()

    fun dismissLastCompletedSale() {
        _lastCompletedSale.value = null
    }

    fun completeSale(notes: String = "") {
        val cartList = _cart.value
        if (cartList.isEmpty()) return

        val subtotal = cartList.sumOf { it.product.sellPrice * it.quantity }
        val discount = _discountAmount.value
        val grandTotal = (subtotal - discount).coerceAtLeast(0.0)

        val customer = _selectedCustomer.value
        val method = _paymentMethod.value

        val paidAmount = when {
            method == "Due" -> 0.0
            _paidAmountInput.value != null -> _paidAmountInput.value!!.coerceAtMost(grandTotal)
            else -> grandTotal
        }
        val dueAmount = (grandTotal - paidAmount).coerceAtLeast(0.0)

        val paymentStatus = when {
            dueAmount <= 0.0 -> "PAID"
            paidAmount <= 0.0 -> "DUE"
            else -> "PARTIAL"
        }

        val invoiceNumber = "INV-${SimpleDateFormat("yyMMdd-HHmmss", Locale.US).format(Date())}"
        val staff = _currentStaff.value?.name ?: "Admin"

        // Calculate points earned
        val currentSettings = settings.value
        val isLoyaltyActive = currentSettings?.isLoyaltyEnabled ?: true
        val spendUnit = (currentSettings?.spendPerPoint ?: 100.0).coerceAtLeast(1.0)
        val customerTier = customer?.let { LoyaltyTier.fromPoints(it.loyaltyPoints) } ?: LoyaltyTier.BRONZE
        val basePoints = if (isLoyaltyActive && customer != null) {
            kotlin.math.floor(grandTotal / spendUnit).toInt()
        } else 0
        val earnedPoints = if (basePoints > 0) (basePoints * customerTier.pointMultiplier).toInt() else 0

        val pointsRedeemedVal = _redeemedPoints.value
        val rewardTitle = _selectedLoyaltyReward.value?.title ?: if (pointsRedeemedVal > 0) "$pointsRedeemedVal Pts Discount" else ""

        val sale = Sale(
            invoiceNumber = invoiceNumber,
            customerId = customer?.id,
            customerName = customer?.name ?: "Walk-in Customer",
            customerPhone = customer?.phone ?: "",
            subtotal = subtotal,
            discount = discount,
            vatAmount = 0.0,
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            paymentMethod = method,
            paymentStatus = paymentStatus,
            staffName = staff,
            timestamp = System.currentTimeMillis(),
            notes = notes,
            pointsEarned = earnedPoints,
            pointsRedeemed = pointsRedeemedVal,
            loyaltyRewardTitle = rewardTitle
        )

        val saleItems = cartList.map {
            SaleItem(
                saleId = 0,
                productId = it.product.id,
                productName = it.product.name,
                unitPrice = it.product.sellPrice,
                buyPrice = it.product.buyPrice,
                quantity = it.quantity,
                totalPrice = it.product.sellPrice * it.quantity
            )
        }

        viewModelScope.launch {
            val saleId = repository.completeSale(sale, saleItems)
            val finalSale = sale.copy(id = saleId)
            _lastCompletedSale.value = Pair(finalSale, saleItems)
            clearCart()
        }
    }

    // --- PRODUCT ACTIONS ---
    fun saveProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun addProductStock(productId: Long, qtyToAdd: Int) {
        viewModelScope.launch {
            repository.addStock(productId, qtyToAdd)
        }
    }

    // --- CUSTOMER ACTIONS ---
    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun collectCustomerDue(customerId: Long, customerName: String, amount: Double, method: String, note: String) {
        viewModelScope.launch {
            repository.recordCustomerPayment(customerId, customerName, amount, method, note)
        }
    }

    // --- EXPENSE ACTIONS ---
    fun addExpense(title: String, category: String, amount: Double, paymentMethod: String, note: String) {
        viewModelScope.launch {
            val staff = _currentStaff.value?.name ?: "Admin"
            val expense = Expense(
                title = title,
                category = category,
                amount = amount,
                paymentMethod = paymentMethod,
                staffName = staff,
                note = note
            )
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // --- STAFF MANAGEMENT ---
    fun saveStaff(staff: StaffAccount) {
        viewModelScope.launch {
            if (staff.id == 0L) {
                repository.insertStaff(staff)
            } else {
                repository.updateStaff(staff)
            }
        }
    }

    fun deleteStaff(staff: StaffAccount) {
        viewModelScope.launch {
            repository.deleteStaff(staff)
        }
    }

    // --- SETTINGS ---
    fun updateSettings(settings: BusinessSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }

    // --- LOYALTY ACTIONS ---
    fun saveLoyaltyReward(reward: LoyaltyReward) {
        viewModelScope.launch {
            if (reward.id == 0L) {
                repository.insertLoyaltyReward(reward)
            } else {
                repository.updateLoyaltyReward(reward)
            }
        }
    }

    fun deleteLoyaltyReward(reward: LoyaltyReward) {
        viewModelScope.launch {
            repository.deleteLoyaltyReward(reward)
        }
    }

    fun adjustCustomerPoints(customerId: Long, customerName: String, pointsDelta: Int, reason: String) {
        viewModelScope.launch {
            repository.adjustCustomerPoints(customerId, customerName, pointsDelta, reason)
        }
    }
}
