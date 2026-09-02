package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val totalDue: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val loyaltyPoints: Int = 0,
    val totalPointsEarned: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customer_payments")
data class CustomerPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val amount: Double,
    val paymentMethod: String = "Cash", // Cash, bKash, Nagad, Bank
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
