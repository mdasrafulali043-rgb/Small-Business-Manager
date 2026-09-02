package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "Other", // Rent, Electricity, Staff Salary, Transport, Tea & Snacks, Maintenance, Other
    val amount: Double = 0.0,
    val paymentMethod: String = "Cash", // Cash, bKash, Nagad, Bank
    val staffName: String = "Admin",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "staff_accounts")
data class StaffAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val role: String = "ADMIN", // ADMIN, MANAGER, CASHIER
    val pinCode: String = "1234",
    val permissions: String = "ALL", // Comma-separated or ALL
    val isActive: Boolean = true
)

@Entity(tableName = "business_settings")
data class BusinessSettings(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "Small Business Store",
    val ownerName: String = "Store Owner",
    val phone: String = "+880 1711-000000",
    val email: String = "business@store.com",
    val address: String = "Dhaka, Bangladesh",
    val currencySymbol: String = "৳",
    val vatRate: Double = 0.0,
    val invoiceFooterMessage: String = "ধন্যবাদ, আবার আসবেন! / Thank you!",
    val isPinLockEnabled: Boolean = false,
    val masterPin: String = "1234",
    val isBiometricEnabled: Boolean = false,
    val language: String = "bn", // "bn" or "en"
    val isDarkMode: Boolean = false,
    val printerName: String = "POS-58-BT",
    val printerAddress: String = "00:11:22:33:44:55",
    val isCloudSyncEnabled: Boolean = false,
    val lastSyncTimestamp: Long = 0L,
    val isLoyaltyEnabled: Boolean = true,
    val spendPerPoint: Double = 100.0,
    val pointRedeemValue: Double = 1.0,
    val minPointsToRedeem: Int = 20
)
