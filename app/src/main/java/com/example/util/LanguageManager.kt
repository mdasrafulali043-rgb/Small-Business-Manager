package com.example.util

object Strings {
    // English & Bengali bilingual map
    private val en = mapOf(
        "app_title" to "Small Business Manager",
        "dashboard" to "Dashboard",
        "pos" to "New Sale",
        "sales" to "Sales History",
        "customers" to "Customers & Due",
        "inventory" to "Inventory",
        "expenses" to "Expenses",
        "reports" to "Reports & Profit",
        "settings" to "Settings",

        // Dashboard
        "todays_sales" to "Today's Sales",
        "todays_profit" to "Today's Profit",
        "todays_expenses" to "Today's Expenses",
        "total_due_balance" to "Total Due Ledger",
        "quick_actions" to "Quick Actions",
        "new_sale_action" to "New Sale (POS)",
        "add_product_action" to "Add Product",
        "add_customer_action" to "Add Customer",
        "add_expense_action" to "Add Expense",
        "scan_barcode" to "Scan Barcode",
        "low_stock_alert" to "Low Stock Alert",
        "items_need_reorder" to "items need restock immediately",
        "recent_sales" to "Recent Transactions",
        "view_all" to "View All",
        "no_recent_sales" to "No sales recorded yet.",

        // POS
        "select_customer" to "Select Customer (Optional)",
        "walk_in_customer" to "Walk-in Customer",
        "search_products" to "Search products by name or barcode...",
        "cart" to "Current Cart",
        "cart_empty" to "Your cart is empty. Add products from catalog.",
        "subtotal" to "Subtotal",
        "discount" to "Discount",
        "vat_tax" to "VAT / Tax",
        "grand_total" to "Grand Total",
        "paid_amount" to "Paid Amount",
        "due_amount" to "Due Amount",
        "payment_method" to "Payment Method",
        "complete_sale" to "Complete Sale & Print",
        "sale_success" to "Sale completed successfully!",
        "insufficient_stock" to "Insufficient stock for",
        "discount_type" to "Discount (৳)",

        // Customers
        "customer_directory" to "Customer Due Ledger",
        "search_customers" to "Search customer name or phone...",
        "total_due" to "Total Due",
        "total_spent" to "Total Spent",
        "collect_due" to "Collect Payment",
        "send_reminder" to "Send Due Reminder (SMS)",
        "payment_history" to "Payment History",
        "no_customers" to "No customers found. Add your first customer!",

        // Inventory
        "products_catalog" to "Products & Stock",
        "search_inventory" to "Search by product name, SKU or barcode...",
        "stock" to "In Stock",
        "buy_price" to "Cost Price",
        "sell_price" to "Selling Price",
        "margin" to "Margin",
        "low_stock" to "Low Stock",
        "all_categories" to "All Categories",
        "add_product" to "Add New Product",
        "edit_product" to "Edit Product",

        // Expenses
        "expense_tracker" to "Expense Tracker",
        "total_expense" to "Total Expense",
        "category" to "Category",
        "record_expense" to "Record Expense",

        // Reports
        "business_overview" to "Financial Reports",
        "period_today" to "Today",
        "period_week" to "This Week",
        "period_month" to "This Month",
        "period_all" to "All Time",
        "gross_sales" to "Gross Revenue",
        "cost_of_goods" to "Cost of Goods (COGS)",
        "gross_profit" to "Gross Profit",
        "operating_expenses" to "Operating Expenses",
        "net_profit_loss" to "Net Profit / Loss",
        "top_selling_products" to "Best Selling Products",

        // Invoicing & Printing
        "invoice" to "Invoice",
        "invoice_no" to "Invoice #",
        "print_thermal" to "Thermal Printer (58mm/80mm)",
        "share_invoice" to "Share / PDF",
        "print_success" to "Receipt sent to printer!",

        // Auth & Security
        "switch_staff" to "Current Staff",
        "pin_lock" to "PIN Lock Security",
        "enter_pin" to "Enter 4-Digit Security PIN",
        "fingerprint_unlock" to "Unlock with Biometrics / Fingerprint",
        "admin" to "Admin",
        "manager" to "Manager",
        "cashier" to "Cashier",

        // Settings
        "business_profile" to "Business Information",
        "store_name" to "Store Name",
        "owner_name" to "Owner Name",
        "phone_number" to "Phone Number",
        "address" to "Address",
        "language_setting" to "Language (ভাষা)",
        "dark_mode" to "Dark Mode",
        "backup_restore" to "Backup & Restore",
        "cloud_sync" to "Cloud Sync & Firebase",
        "save_changes" to "Save Changes",

        // Loyalty Program
        "loyalty_program" to "Loyalty & Rewards",
        "loyalty_points" to "Loyalty Points",
        "points" to "Points",
        "redeem_points" to "Redeem Points & Offers",
        "rewards_catalog" to "Rewards Catalog",
        "points_ledger" to "Points History & Ledger",
        "points_earned" to "Points Earned",
        "points_redeemed" to "Points Redeemed",
        "add_reward" to "Add New Reward",
        "adjust_points" to "Adjust Points / Bonus",
        "tier" to "Membership Tier",
        "earn_rule" to "Earn 1 point per ৳100 spent",
        "available_points" to "Available Points",
        "reward_applied" to "Reward Applied",
        "special_offer" to "Special Offer / Gift"
    )

    private val bn = mapOf(
        "app_title" to "স্মল বিজনেস ম্যানেজার",
        "dashboard" to "ড্যাশবোর্ড",
        "pos" to "নতুন বিক্রি",
        "sales" to "বিক্রির খাতা",
        "customers" to "গ্রাহক ও বাকি",
        "inventory" to "পণ্য ও স্টক",
        "expenses" to "খরচের খাতা",
        "reports" to "হিসাব ও লাভ",
        "settings" to "সেটিংস",

        // Dashboard
        "todays_sales" to "আজকের মোট বিক্রি",
        "todays_profit" to "আজকের নীট লাভ",
        "todays_expenses" to "আজকের মোট খরচ",
        "total_due_balance" to "মোট বাকি খাতা",
        "quick_actions" to "দ্রুত কার্যক্রম",
        "new_sale_action" to "নতুন বিক্রি (POS)",
        "add_product_action" to "পণ্য যোগ",
        "add_customer_action" to "গ্রাহক যোগ",
        "add_expense_action" to "খরচ যোগ",
        "scan_barcode" to "বারকোড স্ক্যান",
        "low_stock_alert" to "স্বল্প স্টক সতর্কতা",
        "items_need_reorder" to "টি পণ্যের স্টক শেষ হওয়ার পথে",
        "recent_sales" to "সাম্প্রতিক বিক্রি",
        "view_all" to "সব দেখুন",
        "no_recent_sales" to "এখনো কোনো বিক্রি রেকর্ড করা হয়নি।",

        // POS
        "select_customer" to "গ্রাহক নির্বাচন (ঐচ্ছিক)",
        "walk_in_customer" to "খুচরা ক্রেতা (Walk-in)",
        "search_products" to "নাম বা বারকোড দিয়ে পণ্য খুঁজুন...",
        "cart" to "বর্তমান কার্ট",
        "cart_empty" to "কার্ট খালি। ক্যাটালগ থেকে পণ্য যোগ করুন।",
        "subtotal" to "উপমোট",
        "discount" to "ছাড়",
        "vat_tax" to "ভ্যাট / ট্যাক্স",
        "grand_total" to "সর্বমোট",
        "paid_amount" to "পরিশোধ",
        "due_amount" to "বাকি",
        "payment_method" to "মূল্য পরিশোধের মাধ্যম",
        "complete_sale" to "বিক্রি সম্পন্ন ও রসিদ",
        "sale_success" to "বিক্রি সফলভাবে সম্পন্ন হয়েছে!",
        "insufficient_stock" to "পর্যাপ্ত স্টক নেই:",
        "discount_type" to "ছাড় (৳)",

        // Customers
        "customer_directory" to "গ্রাহক ও বাকি খাতা",
        "search_customers" to "গ্রাহকের নাম বা মোবাইল নম্বর খুঁজুন...",
        "total_due" to "বর্তমান বাকি",
        "total_spent" to "মোট কেনাকাটা",
        "collect_due" to "বাকি জমা গ্রহণ",
        "send_reminder" to "বাকি তাগাদা মেসেজ (SMS)",
        "payment_history" to "জমা হিস্ট্রি",
        "no_customers" to "কোনো গ্রাহক পাওয়া যায়নি। নতুন গ্রাহক যোগ করুন!",

        // Inventory
        "products_catalog" to "পণ্য ও স্টক তালিকা",
        "search_inventory" to "পণ্যের নাম, কোড বা বারকোড খুঁজুন...",
        "stock" to "মজুদ আছে",
        "buy_price" to "ক্রয়মূল্য",
        "sell_price" to "বিক্রয়মূল্য",
        "margin" to "লাভ/পিস",
        "low_stock" to "স্বল্প স্টক",
        "all_categories" to "সকল ক্যাটাগরি",
        "add_product" to "নতুন পণ্য যোগ",
        "edit_product" to "পণ্য পরিবর্তন",

        // Expenses
        "expense_tracker" to "দোকানের খরচের খাতা",
        "total_expense" to "মোট খরচ",
        "category" to "খাত",
        "record_expense" to "নতুন খরচ লিখুন",

        // Reports
        "business_overview" to "হিসাব বিবরণী ও লাভ-ক্ষতি",
        "period_today" to "আজকের",
        "period_week" to "এই সপ্তাহের",
        "period_month" to "এই মাসের",
        "period_all" to "সর্বমোট",
        "gross_sales" to "মোট বিক্রি",
        "cost_of_goods" to "পণ্যের ক্রয়মূল্য (COGS)",
        "gross_profit" to "মোট লাভ",
        "operating_expenses" to "দোকান পরিচালনা খরচ",
        "net_profit_loss" to "প্রকৃত লাভ / লোকসান",
        "top_selling_products" to "সর্বাধিক বিক্রিত পণ্যসমূহ",

        // Invoicing & Printing
        "invoice" to "চালান / মেমো",
        "invoice_no" to "চালান নং #",
        "print_thermal" to "থার্মাল প্রিন্টার (৫৮/৮০ মিমি)",
        "share_invoice" to "মেমো শেয়ার / PDF",
        "print_success" to "রসিদ প্রিন্টারে পাঠানো হয়েছে!",

        // Auth & Security
        "switch_staff" to "বর্তমান স্টাফ",
        "pin_lock" to "পিন লক সিকিউরিটি",
        "enter_pin" to "৪ সংখ্যার সিকিউরিটি পিন দিন",
        "fingerprint_unlock" to "আঙুলের ছাপ বা বায়োমেট্রিক দিয়ে আনলক",
        "admin" to "অ্যাডমিন (মালিক)",
        "manager" to "ম্যানেজার",
        "cashier" to "ক্যাশিয়ার",

        // Settings
        "business_profile" to "প্রতিষ্ঠানের তথ্য",
        "store_name" to "দোকানের নাম",
        "owner_name" to "মালিকের নাম",
        "phone_number" to "মোবাইল নম্বর",
        "address" to "ঠিকানা",
        "language_setting" to "ভাষা (Language)",
        "dark_mode" to "ডার্ক মোড",
        "backup_restore" to "ডাটা ব্যাকআপ ও রিস্টোর",
        "cloud_sync" to "ক্লাউড সিঙ্ক ও ফায়ারবেস",
        "save_changes" to "সংরক্ষণ করুন",

        // Loyalty Program
        "loyalty_program" to "লয়্যালটি ও রিওয়ার্ডস",
        "loyalty_points" to "লয়্যালটি পয়েন্ট",
        "points" to "পয়েন্ট",
        "redeem_points" to "পয়েন্ট ও অফার রিডিম",
        "rewards_catalog" to "রিওয়ার্ড ক্যাটালগ",
        "points_ledger" to "পয়েন্ট হিস্ট্রি ও লেজার",
        "points_earned" to "অর্জিত পয়েন্ট",
        "points_redeemed" to "ব্যয়িত পয়েন্ট",
        "add_reward" to "নতুন রিওয়ার্ড তৈরি",
        "adjust_points" to "পয়েন্ট বোনাস / সমন্বয়",
        "tier" to "মেম্বারশিপ লেভেল",
        "earn_rule" to "প্রতি ১০০ টাকার কেনাকাটায় ১ পয়েন্ট অর্জন",
        "available_points" to "বর্তমান ব্যালেন্স",
        "reward_applied" to "রিওয়ার্ড যুক্ত হয়েছে",
        "special_offer" to "স্পেশাল অফার / গিফট"
    )

    fun get(key: String, lang: String = "bn"): String {
        val map = if (lang == "en") en else bn
        return map[key] ?: en[key] ?: key
    }
}
