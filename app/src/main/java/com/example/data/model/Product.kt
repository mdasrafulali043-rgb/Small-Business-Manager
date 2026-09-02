package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val banglaName: String = "",
    val category: String = "General",
    val skuBarcode: String = "",
    val buyPrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val stockQuantity: Int = 0,
    val lowStockThreshold: Int = 5,
    val unit: String = "Pcs",
    val updatedAt: Long = System.currentTimeMillis()
)
