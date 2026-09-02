package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Customer::class,
        CustomerPayment::class,
        Sale::class,
        SaleItem::class,
        Expense::class,
        StaffAccount::class,
        BusinessSettings::class,
        LoyaltyReward::class,
        LoyaltyTransaction::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "small_business_manager.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.appDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: AppDao) {
                // Initial Business Settings
                dao.saveSettings(
                    BusinessSettings(
                        id = 1,
                        businessName = "সুপার ট্রেডার্স (Super Traders)",
                        ownerName = "মোঃ আশরাফুল আলী",
                        phone = "+880 1711-000000",
                        email = "supertraders.bd@gmail.com",
                        address = "মিরপুর-১০, ঢাকা, বাংলাদেশ",
                        currencySymbol = "৳",
                        vatRate = 0.0,
                        invoiceFooterMessage = "ধন্যবাদ, আবার আসবেন! Visit again!",
                        isPinLockEnabled = false,
                        masterPin = "1234",
                        isBiometricEnabled = false,
                        language = "bn",
                        isDarkMode = false
                    )
                )

                // Initial Staff Accounts
                dao.insertStaff(
                    StaffAccount(
                        name = "মোঃ আশরাফুল আলী (Owner)",
                        email = "mdasrafulali043@gmail.com",
                        phone = "+8801711000000",
                        role = "ADMIN",
                        pinCode = "1234",
                        permissions = "ALL",
                        isActive = true
                    )
                )
                dao.insertStaff(
                    StaffAccount(
                        name = "করিম আহমেদ (Manager)",
                        email = "karim@business.com",
                        phone = "+8801812345678",
                        role = "MANAGER",
                        pinCode = "2222",
                        permissions = "POS,INVENTORY,EXPENSES,REPORTS",
                        isActive = true
                    )
                )
                dao.insertStaff(
                    StaffAccount(
                        name = "রাকিব হাসান (Cashier)",
                        email = "rakib@business.com",
                        phone = "+8801912345678",
                        role = "CASHIER",
                        pinCode = "3333",
                        permissions = "POS,CUSTOMERS",
                        isActive = true
                    )
                )

                // Initial Products
                dao.insertProduct(
                    Product(
                        name = "Basmati Rice 5kg",
                        banglaName = "বাসমতী চাল ৫ কেজি",
                        category = "Grocery",
                        skuBarcode = "890103001",
                        buyPrice = 450.0,
                        sellPrice = 520.0,
                        stockQuantity = 24,
                        lowStockThreshold = 5,
                        unit = "Bag"
                    )
                )
                dao.insertProduct(
                    Product(
                        name = "Teer Soybean Oil 5L",
                        banglaName = "তীর সয়াবিন তেল ৫ লিটার",
                        category = "Grocery",
                        skuBarcode = "890103002",
                        buyPrice = 860.0,
                        sellPrice = 920.0,
                        stockQuantity = 12,
                        lowStockThreshold = 4,
                        unit = "Bottle"
                    )
                )
                dao.insertProduct(
                    Product(
                        name = "Fresh Sugar 1kg",
                        banglaName = "ফ্রেশ চিনি ১ কেজি",
                        category = "Grocery",
                        skuBarcode = "890103003",
                        buyPrice = 125.0,
                        sellPrice = 140.0,
                        stockQuantity = 40,
                        lowStockThreshold = 10,
                        unit = "Kg"
                    )
                )
                dao.insertProduct(
                    Product(
                        name = "Ispahani Mirzapore Tea 400g",
                        banglaName = "ইস্পাহানি মির্জাপুর চা ৪০০ গ্রাম",
                        category = "Beverage",
                        skuBarcode = "890103004",
                        buyPrice = 210.0,
                        sellPrice = 245.0,
                        stockQuantity = 3, // Low stock demo!
                        lowStockThreshold = 5,
                        unit = "Pkt"
                    )
                )
                dao.insertProduct(
                    Product(
                        name = "Dettol Soap 75g",
                        banglaName = "ডেটোল সাবান ৭৫ গ্রাম",
                        category = "Toiletries",
                        skuBarcode = "890103005",
                        buyPrice = 48.0,
                        sellPrice = 60.0,
                        stockQuantity = 2, // Low stock demo!
                        lowStockThreshold = 6,
                        unit = "Pcs"
                    )
                )

                // Initial Customers
                val cust1Id = dao.insertCustomer(
                    Customer(
                        name = "রহিম উদ্দিন (Rahim Uddin)",
                        phone = "01712-345678",
                        address = "রোড #৪, মিরপুর, ঢাকা",
                        totalDue = 1450.0,
                        totalPurchases = 8900.0,
                        loyaltyPoints = 180,
                        totalPointsEarned = 230
                    )
                )
                val cust2Id = dao.insertCustomer(
                    Customer(
                        name = "সেলিম হোসেন (Selim Hossain)",
                        phone = "01819-876543",
                        address = "সেকশন-৬, ঢাকা",
                        totalDue = 0.0,
                        totalPurchases = 5200.0,
                        loyaltyPoints = 350,
                        totalPointsEarned = 400
                    )
                )

                // Initial Loyalty Rewards Catalog
                val reward1Id = dao.insertLoyaltyReward(
                    LoyaltyReward(
                        title = "৳50 Flat Cash Discount",
                        banglaTitle = "৫০ টাকা নগদ ছাড়",
                        description = "Get ৳50 flat discount on your cart total for 50 points",
                        pointsRequired = 50,
                        rewardType = "DISCOUNT_FIXED",
                        discountValue = 50.0,
                        minPurchaseAmount = 300.0,
                        isActive = true
                    )
                )
                val reward2Id = dao.insertLoyaltyReward(
                    LoyaltyReward(
                        title = "10% Off Cart Voucher",
                        banglaTitle = "১০% ছাড় ভাউচার",
                        description = "Enjoy 10% discount on total order for 100 points",
                        pointsRequired = 100,
                        rewardType = "DISCOUNT_PERCENT",
                        discountValue = 10.0,
                        minPurchaseAmount = 500.0,
                        isActive = true
                    )
                )
                dao.insertLoyaltyReward(
                    LoyaltyReward(
                        title = "Free Tea & Snack Gift Box",
                        banglaTitle = "ফ্রি চা ও বিস্কুট গিফট বক্স",
                        description = "Complimentary tea & biscuit hamper on your visit",
                        pointsRequired = 80,
                        rewardType = "SPECIAL_OFFER_GIFT",
                        discountValue = 0.0,
                        minPurchaseAmount = 0.0,
                        isActive = true
                    )
                )
                dao.insertLoyaltyReward(
                    LoyaltyReward(
                        title = "৳150 Mega Super Saver",
                        banglaTitle = "১৫০ টাকা মেগা সেভার ছাড়",
                        description = "Get ৳150 off on orders above ৳1,000 for 150 points",
                        pointsRequired = 150,
                        rewardType = "DISCOUNT_FIXED",
                        discountValue = 150.0,
                        minPurchaseAmount = 1000.0,
                        isActive = true
                    )
                )
                dao.insertLoyaltyReward(
                    LoyaltyReward(
                        title = "VIP ৳300 Off for Gold Members",
                        banglaTitle = "ভিআইপি ৩০০ টাকা ছাড়",
                        description = "Exclusive high-tier discount for Gold & Platinum loyal members",
                        pointsRequired = 250,
                        rewardType = "DISCOUNT_FIXED",
                        discountValue = 300.0,
                        minPurchaseAmount = 1500.0,
                        isActive = true
                    )
                )

                // Seed initial Loyalty Transactions for demonstration
                dao.insertLoyaltyTransaction(
                    LoyaltyTransaction(
                        customerId = cust1Id,
                        customerName = "রহিম উদ্দিন (Rahim Uddin)",
                        points = 100,
                        type = "EARNED_PURCHASE",
                        description = "Earned 100 pts on initial shopping (INV-240825-101)",
                        invoiceNumber = "INV-240825-101",
                        timestamp = System.currentTimeMillis() - 864000000L
                    )
                )
                dao.insertLoyaltyTransaction(
                    LoyaltyTransaction(
                        customerId = cust1Id,
                        customerName = "রহিম উদ্দিন (Rahim Uddin)",
                        points = -50,
                        type = "REDEEMED_DISCOUNT",
                        description = "Redeemed for ৳50 Flat Cash Discount",
                        rewardTitle = "৳50 Flat Cash Discount",
                        rewardId = reward1Id,
                        timestamp = System.currentTimeMillis() - 432000000L
                    )
                )
                dao.insertLoyaltyTransaction(
                    LoyaltyTransaction(
                        customerId = cust1Id,
                        customerName = "রহিম উদ্দিন (Rahim Uddin)",
                        points = 130,
                        type = "EARNED_PURCHASE",
                        description = "Earned 130 pts on grocery shopping (INV-240830-105)",
                        invoiceNumber = "INV-240830-105",
                        timestamp = System.currentTimeMillis() - 172800000L
                    )
                )

                dao.insertLoyaltyTransaction(
                    LoyaltyTransaction(
                        customerId = cust2Id,
                        customerName = "সেলিম হোসেন (Selim Hossain)",
                        points = 400,
                        type = "EARNED_PURCHASE",
                        description = "Earned 400 pts on bulk shopping",
                        timestamp = System.currentTimeMillis() - 604800000L
                    )
                )
                dao.insertLoyaltyTransaction(
                    LoyaltyTransaction(
                        customerId = cust2Id,
                        customerName = "সেলিম হোসেন (Selim Hossain)",
                        points = -50,
                        type = "REDEEMED_DISCOUNT",
                        description = "Redeemed for ৳50 Flat Cash Discount",
                        rewardTitle = "৳50 Flat Cash Discount",
                        rewardId = reward1Id,
                        timestamp = System.currentTimeMillis() - 86400000L
                    )
                )

                // Initial Expenses
                dao.insertExpense(
                    Expense(
                        title = "দোকানের বিদ্যুৎ বিল (Electricity Bill)",
                        category = "Electricity",
                        amount = 1200.0,
                        paymentMethod = "bKash",
                        staffName = "Admin",
                        timestamp = System.currentTimeMillis() - 86400000L
                    )
                )
                dao.insertExpense(
                    Expense(
                        title = "দোকানের নাস্তা খরচ (Tea & Snacks)",
                        category = "Tea & Snacks",
                        amount = 180.0,
                        paymentMethod = "Cash",
                        staffName = "Admin",
                        timestamp = System.currentTimeMillis() - 3600000L
                    )
                )
            }
        }
    }
}
