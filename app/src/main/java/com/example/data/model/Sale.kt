package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val vatAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentMethod: String = "Cash", // Cash, bKash, Nagad, Bank, Due, Split
    val paymentStatus: String = "PAID", // PAID, PARTIAL, DUE
    val staffName: String = "Admin",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val pointsEarned: Int = 0,
    val pointsRedeemed: Int = 0,
    val loyaltyRewardTitle: String = ""
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val buyPrice: Double, // Snapshot at sale time for accurate profit computation
    val quantity: Int,
    val totalPrice: Double
)
