package com.example.util

import com.example.data.model.BusinessSettings
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrinterHelper {

    fun generateReceiptText(
        settings: BusinessSettings,
        sale: Sale,
        items: List<SaleItem>
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(sale.timestamp))
        val sb = StringBuilder()

        // 32-character wide for 58mm thermal printer standard
        val width = 32
        val line = "=".repeat(width)
        val dash = "-".repeat(width)

        sb.append(centerText(settings.businessName, width)).append("\n")
        if (settings.address.isNotEmpty()) {
            sb.append(centerText(settings.address, width)).append("\n")
        }
        if (settings.phone.isNotEmpty()) {
            sb.append(centerText("Phone: ${settings.phone}", width)).append("\n")
        }
        sb.append(line).append("\n")

        sb.append("Invoice: ${sale.invoiceNumber}\n")
        sb.append("Date   : $dateStr\n")
        sb.append("Cust   : ${sale.customerName}\n")
        if (sale.customerPhone.isNotEmpty()) {
            sb.append("Phone  : ${sale.customerPhone}\n")
        }
        sb.append("Staff  : ${sale.staffName}\n")
        sb.append(dash).append("\n")

        // Items Header: Item (16), Qty (4), Price (12)
        sb.append(formatRow("Item", "Qty", "Price", width)).append("\n")
        sb.append(dash).append("\n")

        for (item in items) {
            val itemName = if (item.productName.length > 15) item.productName.take(13) + ".." else item.productName
            val qtyStr = "${item.quantity}"
            val totalStr = String.format(Locale.US, "%.1f", item.totalPrice)
            sb.append(formatRow(itemName, qtyStr, totalStr, width)).append("\n")
        }
        sb.append(dash).append("\n")

        sb.append(formatSummary("Subtotal:", String.format(Locale.US, "%.2f", sale.subtotal), width)).append("\n")
        if (sale.discount > 0) {
            sb.append(formatSummary("Discount:", "-${String.format(Locale.US, "%.2f", sale.discount)}", width)).append("\n")
        }
        if (sale.vatAmount > 0) {
            sb.append(formatSummary("VAT/Tax:", String.format(Locale.US, "%.2f", sale.vatAmount), width)).append("\n")
        }
        sb.append(line).append("\n")
        sb.append(formatSummary("TOTAL:", "${settings.currencySymbol} ${String.format(Locale.US, "%.2f", sale.grandTotal)}", width)).append("\n")
        sb.append(formatSummary("Paid (${sale.paymentMethod}):", "${settings.currencySymbol} ${String.format(Locale.US, "%.2f", sale.paidAmount)}", width)).append("\n")
        if (sale.dueAmount > 0) {
            sb.append(formatSummary("DUE BALANCE:", "${settings.currencySymbol} ${String.format(Locale.US, "%.2f", sale.dueAmount)}", width)).append("\n")
        }
        if (sale.pointsEarned > 0 || sale.pointsRedeemed > 0) {
            sb.append(dash).append("\n")
            sb.append(centerText("-- LOYALTY PROGRAM --", width)).append("\n")
            if (sale.pointsRedeemed > 0) {
                val title = if (sale.loyaltyRewardTitle.isNotEmpty()) " (${sale.loyaltyRewardTitle.take(12)})" else ""
                sb.append(formatSummary("Redeemed$title:", "-${sale.pointsRedeemed} pts", width)).append("\n")
            }
            if (sale.pointsEarned > 0) {
                sb.append(formatSummary("Points Earned:", "+${sale.pointsEarned} pts", width)).append("\n")
            }
        }
        sb.append(line).append("\n")
        sb.append(centerText(settings.invoiceFooterMessage, width)).append("\n")
        sb.append(centerText("* Thank You! *", width)).append("\n\n")

        return sb.toString()
    }

    private fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val padding = (width - text.length) / 2
        return " ".repeat(padding) + text
    }

    private fun formatRow(left: String, middle: String, right: String, totalWidth: Int): String {
        // total 32 chars: left=16, middle=6, right=10
        val leftPart = left.padEnd(16).take(16)
        val midPart = middle.padStart(5).take(5)
        val rightPart = right.padStart(totalWidth - 21)
        return "$leftPart$midPart $rightPart"
    }

    private fun formatSummary(label: String, value: String, totalWidth: Int): String {
        val spaces = totalWidth - label.length - value.length
        return if (spaces > 0) label + " ".repeat(spaces) + value else "$label $value"
    }

    // ESC/POS raw command generator for Bluetooth Thermal Printers
    fun getEscPosBytes(receiptText: String): ByteArray {
        val init = byteArrayOf(0x1B, 0x40) // ESC @: Initialize printer
        val textBytes = receiptText.toByteArray(Charsets.UTF_8)
        val feedAndCut = byteArrayOf(0x1D, 0x56, 0x41, 0x00) // GS V A 0: Cut paper
        return init + textBytes + feedAndCut
    }
}
