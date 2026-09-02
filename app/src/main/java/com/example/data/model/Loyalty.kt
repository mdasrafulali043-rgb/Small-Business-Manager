package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loyalty_rewards")
data class LoyaltyReward(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val banglaTitle: String = "",
    val description: String = "",
    val pointsRequired: Int = 50,
    val rewardType: String = "DISCOUNT_FIXED", // DISCOUNT_FIXED, DISCOUNT_PERCENT, SPECIAL_OFFER_GIFT
    val discountValue: Double = 50.0,
    val minPurchaseAmount: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "loyalty_transactions")
data class LoyaltyTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val points: Int, // Positive for earned/bonus, negative for redeemed/deducted
    val type: String, // EARNED_PURCHASE, REDEEMED_DISCOUNT, REDEEMED_OFFER, MANUAL_BONUS, MANUAL_DEDUCT
    val description: String,
    val saleId: Long? = null,
    val invoiceNumber: String = "",
    val rewardId: Long? = null,
    val rewardTitle: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class LoyaltyTier(
    val titleEn: String,
    val titleBn: String,
    val minPoints: Int,
    val pointMultiplier: Double
) {
    BRONZE("Bronze", "ব্রোঞ্জ", 0, 1.0),
    SILVER("Silver", "সিলভার", 100, 1.2),
    GOLD("Gold", "গোল্ড", 300, 1.5),
    PLATINUM("Platinum", "প্লাটিনাম", 700, 2.0);

    companion object {
        fun fromPoints(points: Int): LoyaltyTier {
            return when {
                points >= PLATINUM.minPoints -> PLATINUM
                points >= GOLD.minPoints -> GOLD
                points >= SILVER.minPoints -> SILVER
                else -> BRONZE
            }
        }
    }
}
