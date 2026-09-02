package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface AppDao {

    // --- PRODUCTS ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE skuBarcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE stockQuantity <= lowStockThreshold ORDER BY stockQuantity ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity WHERE id = :productId")
    suspend fun deductStock(productId: Long, quantity: Int)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :quantity WHERE id = :productId")
    suspend fun addStock(productId: Long, quantity: Int)

    // --- CUSTOMERS ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET totalDue = totalDue + :dueDifference, totalPurchases = totalPurchases + :purchaseAmount WHERE id = :customerId")
    suspend fun updateCustomerTotals(customerId: Long, dueDifference: Double, purchaseAmount: Double)

    @Query("UPDATE customers SET totalDue = totalDue - :paidAmount WHERE id = :customerId")
    suspend fun deductCustomerDue(customerId: Long, paidAmount: Double)

    @Query("UPDATE customers SET loyaltyPoints = loyaltyPoints + :points, totalPointsEarned = totalPointsEarned + :points WHERE id = :customerId")
    suspend fun addCustomerPoints(customerId: Long, points: Int)

    @Query("UPDATE customers SET loyaltyPoints = CASE WHEN loyaltyPoints >= :points THEN loyaltyPoints - :points ELSE 0 END WHERE id = :customerId")
    suspend fun deductCustomerPoints(customerId: Long, points: Int)

    @Query("UPDATE customers SET loyaltyPoints = :points WHERE id = :customerId")
    suspend fun setCustomerPoints(customerId: Long, points: Int)

    // --- CUSTOMER PAYMENTS ---
    @Query("SELECT * FROM customer_payments WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<CustomerPayment>>

    @Query("SELECT * FROM customer_payments ORDER BY timestamp DESC")
    fun getAllCustomerPayments(): Flow<List<CustomerPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerPayment(payment: CustomerPayment): Long

    // --- SALES ---
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Delete
    suspend fun deleteSale(sale: Sale)

    // --- SALE ITEMS ---
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsForSale(saleId: Long): List<SaleItem>

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItems(): Flow<List<SaleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteSaleItemsBySaleId(saleId: Long)

    // --- EXPENSES ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // --- STAFF ACCOUNTS ---
    @Query("SELECT * FROM staff_accounts ORDER BY role ASC, name ASC")
    fun getAllStaff(): Flow<List<StaffAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffAccount): Long

    @Update
    suspend fun updateStaff(staff: StaffAccount)

    @Delete
    suspend fun deleteStaff(staff: StaffAccount)

    // --- BUSINESS SETTINGS ---
    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<BusinessSettings?>

    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): BusinessSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: BusinessSettings)

    // --- LOYALTY REWARDS ---
    @Query("SELECT * FROM loyalty_rewards ORDER BY pointsRequired ASC")
    fun getAllLoyaltyRewards(): Flow<List<LoyaltyReward>>

    @Query("SELECT * FROM loyalty_rewards WHERE isActive = 1 ORDER BY pointsRequired ASC")
    fun getActiveLoyaltyRewards(): Flow<List<LoyaltyReward>>

    @Query("SELECT * FROM loyalty_rewards WHERE id = :id LIMIT 1")
    suspend fun getLoyaltyRewardById(id: Long): LoyaltyReward?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoyaltyReward(reward: LoyaltyReward): Long

    @Update
    suspend fun updateLoyaltyReward(reward: LoyaltyReward)

    @Delete
    suspend fun deleteLoyaltyReward(reward: LoyaltyReward)

    // --- LOYALTY TRANSACTIONS ---
    @Query("SELECT * FROM loyalty_transactions ORDER BY timestamp DESC")
    fun getAllLoyaltyTransactions(): Flow<List<LoyaltyTransaction>>

    @Query("SELECT * FROM loyalty_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getLoyaltyTransactionsForCustomer(customerId: Long): Flow<List<LoyaltyTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoyaltyTransaction(transaction: LoyaltyTransaction): Long
}
