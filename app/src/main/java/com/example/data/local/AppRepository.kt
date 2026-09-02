package com.example.data.local

import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.CustomerPayment
import com.example.data.model.Expense
import com.example.data.model.LoyaltyReward
import com.example.data.model.LoyaltyTransaction
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.StaffAccount
import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {

    // Products
    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = dao.getLowStockProducts()

    suspend fun insertProduct(product: Product): Long = dao.insertProduct(product)
    suspend fun updateProduct(product: Product) = dao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)
    suspend fun getProductByBarcode(barcode: String): Product? = dao.getProductByBarcode(barcode)
    suspend fun addStock(productId: Long, quantity: Int) = dao.addStock(productId, quantity)

    // Customers
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()

    suspend fun insertCustomer(customer: Customer): Long = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = dao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = dao.deleteCustomer(customer)

    fun getPaymentsForCustomer(customerId: Long): Flow<List<CustomerPayment>> =
        dao.getPaymentsForCustomer(customerId)

    suspend fun recordCustomerPayment(
        customerId: Long,
        customerName: String,
        amount: Double,
        method: String,
        note: String
    ) {
        val payment = CustomerPayment(
            customerId = customerId,
            customerName = customerName,
            amount = amount,
            paymentMethod = method,
            note = note,
            timestamp = System.currentTimeMillis()
        )
        dao.insertCustomerPayment(payment)
        dao.deductCustomerDue(customerId, amount)
    }

    // Sales
    val allSales: Flow<List<Sale>> = dao.getAllSales()
    val allSaleItems: Flow<List<SaleItem>> = dao.getAllSaleItems()

    suspend fun getSaleItems(saleId: Long): List<SaleItem> = dao.getSaleItemsForSale(saleId)

    suspend fun completeSale(sale: Sale, items: List<SaleItem>): Long {
        val saleId = dao.insertSale(sale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
        dao.insertSaleItems(itemsWithSaleId)

        // Deduct inventory stock
        for (item in items) {
            dao.deductStock(item.productId, item.quantity)
        }

        // Update customer due, total purchases, and loyalty points if customer attached
        if (sale.customerId != null && sale.customerId > 0) {
            dao.updateCustomerTotals(
                customerId = sale.customerId,
                dueDifference = sale.dueAmount,
                purchaseAmount = sale.grandTotal
            )

            // Deduct redeemed loyalty points if any
            if (sale.pointsRedeemed > 0) {
                dao.deductCustomerPoints(sale.customerId, sale.pointsRedeemed)
                val rewardDesc = if (sale.loyaltyRewardTitle.isNotBlank()) " (${sale.loyaltyRewardTitle})" else ""
                dao.insertLoyaltyTransaction(
                    LoyaltyTransaction(
                        customerId = sale.customerId,
                        customerName = sale.customerName,
                        points = -sale.pointsRedeemed,
                        type = "REDEEMED_DISCOUNT",
                        description = "Redeemed ${sale.pointsRedeemed} pts on ${sale.invoiceNumber}$rewardDesc",
                        saleId = saleId,
                        invoiceNumber = sale.invoiceNumber,
                        rewardTitle = sale.loyaltyRewardTitle
                    )
                )
            }

            // Award earned loyalty points
            if (sale.pointsEarned > 0) {
                dao.addCustomerPoints(sale.customerId, sale.pointsEarned)
                dao.insertLoyaltyTransaction(
                    LoyaltyTransaction(
                        customerId = sale.customerId,
                        customerName = sale.customerName,
                        points = sale.pointsEarned,
                        type = "EARNED_PURCHASE",
                        description = "Earned ${sale.pointsEarned} pts on purchase (${sale.invoiceNumber})",
                        saleId = saleId,
                        invoiceNumber = sale.invoiceNumber
                    )
                )
            }
        }
        return saleId
    }

    suspend fun deleteSale(sale: Sale) {
        dao.deleteSaleItemsBySaleId(sale.id)
        dao.deleteSale(sale)
    }

    // Loyalty Rewards
    val allLoyaltyRewards: Flow<List<LoyaltyReward>> = dao.getAllLoyaltyRewards()
    val activeLoyaltyRewards: Flow<List<LoyaltyReward>> = dao.getActiveLoyaltyRewards()

    suspend fun insertLoyaltyReward(reward: LoyaltyReward): Long = dao.insertLoyaltyReward(reward)
    suspend fun updateLoyaltyReward(reward: LoyaltyReward) = dao.updateLoyaltyReward(reward)
    suspend fun deleteLoyaltyReward(reward: LoyaltyReward) = dao.deleteLoyaltyReward(reward)

    // Loyalty Transactions
    val allLoyaltyTransactions: Flow<List<LoyaltyTransaction>> = dao.getAllLoyaltyTransactions()
    fun getLoyaltyTransactionsForCustomer(customerId: Long): Flow<List<LoyaltyTransaction>> =
        dao.getLoyaltyTransactionsForCustomer(customerId)

    suspend fun adjustCustomerPoints(
        customerId: Long,
        customerName: String,
        pointsDelta: Int,
        reason: String
    ) {
        if (pointsDelta > 0) {
            dao.addCustomerPoints(customerId, pointsDelta)
        } else {
            dao.deductCustomerPoints(customerId, -pointsDelta)
        }
        val type = if (pointsDelta >= 0) "MANUAL_BONUS" else "MANUAL_DEDUCT"
        dao.insertLoyaltyTransaction(
            LoyaltyTransaction(
                customerId = customerId,
                customerName = customerName,
                points = pointsDelta,
                type = type,
                description = reason.ifBlank { if (pointsDelta >= 0) "Manual bonus points added" else "Manual points deducted" }
            )
        )
    }

    // Expenses
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()

    suspend fun insertExpense(expense: Expense): Long = dao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    // Staff
    val allStaff: Flow<List<StaffAccount>> = dao.getAllStaff()

    suspend fun insertStaff(staff: StaffAccount): Long = dao.insertStaff(staff)
    suspend fun updateStaff(staff: StaffAccount) = dao.updateStaff(staff)
    suspend fun deleteStaff(staff: StaffAccount) = dao.deleteStaff(staff)

    // Settings
    val settings: Flow<BusinessSettings?> = dao.getSettings()

    suspend fun getSettingsDirect(): BusinessSettings? = dao.getSettingsDirect()
    suspend fun saveSettings(settings: BusinessSettings) = dao.saveSettings(settings)
}
