package com.example.ui.loyalty

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.LoyaltyReward
import com.example.data.model.LoyaltyTier
import com.example.data.model.LoyaltyTransaction
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberSecondaryLight
import com.example.ui.theme.GreenPrimaryLight
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltyScreen(
    viewModel: MainViewModel,
    lang: String
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val rewards by viewModel.loyaltyRewards.collectAsStateWithLifecycle()
    val transactions by viewModel.loyaltyTransactions.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currency = settings?.currencySymbol ?: "৳"

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        if (lang == "bn") "রিওয়ার্ড ক্যাটালগ" else "Rewards Catalog",
        if (lang == "bn") "সদস্য ও বোনাস" else "Members & Points",
        if (lang == "bn") "পয়েন্ট লেজার" else "Points Ledger"
    )

    var showAddRewardDialog by remember { mutableStateOf(false) }
    var editingReward by remember { mutableStateOf<LoyaltyReward?>(null) }
    var customerForAdjustment by remember { mutableStateOf<Customer?>(null) }
    var customerForHistory by remember { mutableStateOf<Customer?>(null) }

    // Summary calculations
    val totalPointsInCirculation = remember(customers) {
        customers.sumOf { it.loyaltyPoints }
    }
    val totalPointsRedeemed = remember(transactions) {
        transactions.filter { it.points < 0 }.sumOf { -it.points }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.get("loyalty_program", lang),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (selectedTabIndex == 0) {
                        FilledTonalButton(
                            onClick = {
                                editingReward = null
                                showAddRewardDialog = true
                            },
                            modifier = Modifier.padding(end = 8.dp).testTag("add_reward_top_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (lang == "bn") "নতুন রিওয়ার্ড" else "New Reward", fontSize = 12.sp)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = {
                        editingReward = null
                        showAddRewardDialog = true
                    },
                    modifier = Modifier.testTag("add_reward_fab"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reward")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Overview KPI Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (lang == "bn") "মোট সক্রিয় পয়েন্ট ব্যালেন্স" else "Total Active Points Issued",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$totalPointsInCirculation",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Strings.get("points", lang),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (lang == "bn") "ব্যয়িত পয়েন্ট" else "Points Redeemed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$totalPointsRedeemed pts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmberSecondaryLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tier Policy & Earn Rate explanation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (lang == "bn") "প্রতি ৳১০০ কেনাকাটায় ১ পয়েন্ট (টায়ার বোনাস সহ)" else "Spend ৳100 = 1 pt (with Tier bonus)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "${customers.size} ${if (lang == "bn") "সদস্য" else "Members"}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Tabs Selector
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("loyalty_tab_$index")
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> RewardsCatalogTab(
                    rewards = rewards,
                    currency = currency,
                    lang = lang,
                    onEdit = {
                        editingReward = it
                        showAddRewardDialog = true
                    },
                    onToggleActive = { reward, isActive ->
                        viewModel.saveLoyaltyReward(reward.copy(isActive = isActive))
                    },
                    onDelete = {
                        viewModel.deleteLoyaltyReward(it)
                    }
                )
                1 -> CustomerMembersTab(
                    customers = customers,
                    currency = currency,
                    lang = lang,
                    onAdjustPoints = { customerForAdjustment = it },
                    onViewHistory = { customerForHistory = it }
                )
                2 -> PointsLedgerTab(
                    transactions = transactions,
                    lang = lang
                )
            }
        }
    }

    // Dialog: Add / Edit Reward
    if (showAddRewardDialog) {
        AddEditRewardDialog(
            reward = editingReward,
            currency = currency,
            lang = lang,
            onDismiss = { showAddRewardDialog = false },
            onSave = { rewardToSave ->
                viewModel.saveLoyaltyReward(rewardToSave)
                showAddRewardDialog = false
            }
        )
    }

    // Dialog: Adjust Points / Bonus
    if (customerForAdjustment != null) {
        val targetCustomer = customerForAdjustment!!
        AdjustPointsDialog(
            customer = targetCustomer,
            lang = lang,
            onDismiss = { customerForAdjustment = null },
            onConfirm = { delta, reason ->
                viewModel.adjustCustomerPoints(targetCustomer.id, targetCustomer.name, delta, reason)
                customerForAdjustment = null
            }
        )
    }

    // Dialog: Customer Loyalty History
    if (customerForHistory != null) {
        val targetCustomer = customerForHistory!!
        val customerTransactions = remember(transactions, targetCustomer.id) {
            transactions.filter { it.customerId == targetCustomer.id }
        }
        CustomerHistoryDialog(
            customer = targetCustomer,
            transactions = customerTransactions,
            lang = lang,
            onDismiss = { customerForHistory = null }
        )
    }
}

@Composable
private fun RewardsCatalogTab(
    rewards: List<LoyaltyReward>,
    currency: String,
    lang: String,
    onEdit: (LoyaltyReward) -> Unit,
    onToggleActive: (LoyaltyReward, Boolean) -> Unit,
    onDelete: (LoyaltyReward) -> Unit
) {
    if (rewards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CardGiftcard,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (lang == "bn") "কোনো রিওয়ার্ড বা অফার তৈরি করা হয়নি" else "No rewards created yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (lang == "bn") "গ্রাহকদের কেনাকাটায় ছাড় দিতে 'নতুন রিওয়ার্ড' বাটনে চাপুন।" else "Tap 'New Reward' to create point discounts for shoppers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rewards, key = { it.id }) { reward ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reward_card_${reward.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (reward.isActive) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                // Reward Type Icon
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when (reward.rewardType) {
                                                "DISCOUNT_PERCENT" -> Color(0xFFE8F5E9)
                                                "DISCOUNT_FIXED" -> Color(0xFFFFF8E1)
                                                else -> Color(0xFFF3E5F5)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (reward.rewardType) {
                                            "DISCOUNT_PERCENT" -> Icons.Default.Percent
                                            "DISCOUNT_FIXED" -> Icons.Default.MonetizationOn
                                            else -> Icons.Default.CardGiftcard
                                        },
                                        contentDescription = null,
                                        tint = when (reward.rewardType) {
                                            "DISCOUNT_PERCENT" -> GreenPrimaryLight
                                            "DISCOUNT_FIXED" -> Color(0xFFF57C00)
                                            else -> Color(0xFF7B1FA2)
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = if (lang == "bn" && reward.banglaTitle.isNotBlank()) reward.banglaTitle else reward.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    val benefitText = when (reward.rewardType) {
                                        "DISCOUNT_PERCENT" -> "${reward.discountValue.toInt()}% ${if (lang == "bn") "মূল্যে ছাড়" else "Off Cart"}"
                                        "DISCOUNT_FIXED" -> "$currency ${reward.discountValue.toInt()} ${if (lang == "bn") "নগদ ছাড়" else "Flat Cash Off"}"
                                        else -> if (lang == "bn") "স্পেশাল গিফট / উপহার" else "Special Gift / Offer"
                                    }
                                    Text(
                                        text = benefitText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Points Required Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFFF8E1),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${reward.pointsRequired} pts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }

                        if (reward.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = reward.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (reward.minPurchaseAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (lang == "bn") "সর্বনিম্ন অর্ডার:" else "Min Order:"} $currency ${reward.minPurchaseAmount.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(6.dp))

                        // Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = reward.isActive,
                                    onCheckedChange = { onToggleActive(reward, it) },
                                    modifier = Modifier.testTag("toggle_reward_${reward.id}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (reward.isActive) (if (lang == "bn") "সক্রিয়" else "Active") else (if (lang == "bn") "নিষ্ক্রিয়" else "Inactive"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (reward.isActive) GreenPrimaryLight else MaterialTheme.colorScheme.error
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { onEdit(reward) },
                                    modifier = Modifier.testTag("edit_reward_${reward.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = { onDelete(reward) },
                                    modifier = Modifier.testTag("delete_reward_${reward.id}")
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerMembersTab(
    customers: List<Customer>,
    currency: String,
    lang: String,
    onAdjustPoints: (Customer) -> Unit,
    onViewHistory: (Customer) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCustomers = remember(customers, searchQuery) {
        customers.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
        }.sortedByDescending { it.loyaltyPoints }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(Strings.get("search_customers", lang)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_loyalty_members")
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredCustomers, key = { it.id }) { customer ->
                val tier = LoyaltyTier.fromPoints(customer.loyaltyPoints)
                val tierColor = when (tier) {
                    LoyaltyTier.PLATINUM -> Color(0xFF3F51B5)
                    LoyaltyTier.GOLD -> Color(0xFFFFA000)
                    LoyaltyTier.SILVER -> Color(0xFF78909C)
                    LoyaltyTier.BRONZE -> Color(0xFF8D6E63)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loyalty_customer_${customer.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = customer.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Tier Badge
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = tierColor.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, tierColor)
                                    ) {
                                        Text(
                                            text = if (lang == "bn") tier.titleBn else tier.titleEn,
                                            color = tierColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (customer.phone.isNotBlank()) {
                                    Text(
                                        text = customer.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Points Counter
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${customer.loyaltyPoints}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = if (lang == "bn") "সর্বমোট অর্জিত: ${customer.totalPointsEarned} pt" else "Lifetime: ${customer.totalPointsEarned} pt",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onViewHistory(customer) },
                                modifier = Modifier.weight(1f).testTag("view_history_${customer.id}"),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == "bn") "পয়েন্ট হিস্ট্রি" else "History", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onAdjustPoints(customer) },
                                modifier = Modifier.weight(1f).testTag("adjust_points_${customer.id}"),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ControlPoint, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == "bn") "বোনাস / সমন্বয়" else "Bonus / Adjust", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PointsLedgerTab(
    transactions: List<LoyaltyTransaction>,
    lang: String
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (lang == "bn") "কোনো পয়েন্ট লেনদেন হয়নি" else "No point transactions yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (lang == "bn") "বিক্রির সময় বা বোনাস পয়েন্ট যুক্ত হলে এখানে অডিট খাতা দেখা যাবে।" else "Points earned and redeemed in sales will show here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(transactions, key = { it.id }) { tx ->
                val isPositive = tx.points >= 0
                val badgeColor = when (tx.type) {
                    "EARNED_PURCHASE" -> GreenPrimaryLight
                    "REDEEMED_DISCOUNT", "REDEEMED_OFFER" -> Color(0xFFE65100)
                    "MANUAL_BONUS" -> Color(0xFF1976D2)
                    else -> MaterialTheme.colorScheme.error
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(badgeColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tx.customerName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = (if (isPositive) "+" else "") + "${tx.points} pts",
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                text = tx.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dateFormat.format(Date(tx.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                if (tx.invoiceNumber.isNotBlank()) {
                                    Text(
                                        text = tx.invoiceNumber,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditRewardDialog(
    reward: LoyaltyReward?,
    currency: String,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (LoyaltyReward) -> Unit
) {
    var title by remember { mutableStateOf(reward?.title ?: "") }
    var banglaTitle by remember { mutableStateOf(reward?.banglaTitle ?: "") }
    var description by remember { mutableStateOf(reward?.description ?: "") }
    var pointsRequired by remember { mutableStateOf(reward?.pointsRequired?.toString() ?: "50") }
    var rewardType by remember { mutableStateOf(reward?.rewardType ?: "DISCOUNT_FIXED") }
    var discountValue by remember { mutableStateOf(reward?.discountValue?.toInt()?.toString() ?: "50") }
    var minPurchaseAmount by remember { mutableStateOf(reward?.minPurchaseAmount?.toInt()?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (reward == null) Strings.get("add_reward", lang) else (if (lang == "bn") "রিওয়ার্ড পরিবর্তন" else "Edit Reward"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (lang == "bn") "রিওয়ার্ডের নাম (English)" else "Reward Title (EN)") },
                        modifier = Modifier.fillMaxWidth().testTag("reward_title_input"),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = banglaTitle,
                        onValueChange = { banglaTitle = it },
                        label = { Text(if (lang == "bn") "রিওয়ার্ডের নাম (বাংলা)" else "Reward Title (Bangla)") },
                        modifier = Modifier.fillMaxWidth().testTag("reward_bangla_title_input"),
                        singleLine = true
                    )
                }
                item {
                    // Reward Type Choice
                    Text(if (lang == "bn") "রিওয়ার্ডের ধরন" else "Reward Type", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val types = listOf(
                            "DISCOUNT_FIXED" to if (lang == "bn") "নগদ ছাড় (৳)" else "Flat ($currency)",
                            "DISCOUNT_PERCENT" to if (lang == "bn") "শতাংশ ছাড় (%)" else "Percent (%)",
                            "SPECIAL_OFFER_GIFT" to if (lang == "bn") "গিফট/অফার" else "Gift/Offer"
                        )
                        for ((typeKey, typeLabel) in types) {
                            FilterChip(
                                selected = rewardType == typeKey,
                                onClick = { rewardType = typeKey },
                                label = { Text(typeLabel, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                if (rewardType != "SPECIAL_OFFER_GIFT") {
                    item {
                        OutlinedTextField(
                            value = discountValue,
                            onValueChange = { discountValue = it },
                            label = {
                                Text(
                                    if (rewardType == "DISCOUNT_PERCENT") (if (lang == "bn") "ছাড়ের শতকরা হার (%)" else "Discount Percentage (%)")
                                    else (if (lang == "bn") "ছাড়ের পরিমাণ ($currency)" else "Discount Value ($currency)")
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("reward_discount_val_input"),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = pointsRequired,
                        onValueChange = { pointsRequired = it },
                        label = { Text(if (lang == "bn") "প্রয়োজনীয় পয়েন্ট (Points Required)" else "Points Required") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("reward_points_input"),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = minPurchaseAmount,
                        onValueChange = { minPurchaseAmount = it },
                        label = { Text(if (lang == "bn") "ন্যূনতম কেনাকাটার সীমা ($currency)" else "Min Purchase Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(if (lang == "bn") "বিবরণ (ঐচ্ছিক)" else "Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newReward = (reward ?: LoyaltyReward(title = title)).copy(
                            title = title,
                            banglaTitle = banglaTitle,
                            description = description,
                            pointsRequired = pointsRequired.toIntOrNull() ?: 50,
                            rewardType = rewardType,
                            discountValue = discountValue.toDoubleOrNull() ?: 0.0,
                            minPurchaseAmount = minPurchaseAmount.toDoubleOrNull() ?: 0.0,
                            isActive = reward?.isActive ?: true
                        )
                        onSave(newReward)
                    }
                },
                modifier = Modifier.testTag("save_reward_btn")
            ) {
                Text(if (lang == "bn") "সংরক্ষণ করুন" else "Save Reward")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AdjustPointsDialog(
    customer: Customer,
    lang: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var isAddition by remember { mutableStateOf(true) }
    var pointsInput by remember { mutableStateOf("50") }
    var reasonInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (lang == "bn") "${customer.name} - পয়েন্ট সমন্বয়" else "Adjust Points - ${customer.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${if (lang == "bn") "বর্তমান পয়েন্ট ব্যালেন্স:" else "Current Balance:"} ${customer.loyaltyPoints} pts",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isAddition,
                        onClick = { isAddition = true },
                        label = { Text(if (lang == "bn") "+ বোনাস পয়েন্ট দিন" else "+ Add Bonus Points") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isAddition,
                        onClick = { isAddition = false },
                        label = { Text(if (lang == "bn") "- পয়েন্ট কর্তন" else "- Deduct Points") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = pointsInput,
                    onValueChange = { pointsInput = it },
                    label = { Text(if (lang == "bn") "পয়েন্টের পরিমাণ" else "Points Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("adjust_points_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    placeholder = {
                        Text(if (lang == "bn") "কারণ (যেমন: জন্মদিন গিফট, রেফারেল বোনাস)" else "Reason (e.g. Birthday gift, referral)")
                    },
                    label = { Text(if (lang == "bn") "কারণ বা নোট" else "Reason / Note") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pts = pointsInput.toIntOrNull() ?: 0
                    if (pts > 0) {
                        val finalDelta = if (isAddition) pts else -pts
                        onConfirm(finalDelta, reasonInput)
                    }
                },
                modifier = Modifier.testTag("confirm_adjust_points_btn")
            ) {
                Text(if (lang == "bn") "নিশ্চিত করুন" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CustomerHistoryDialog(
    customer: Customer,
    transactions: List<LoyaltyTransaction>,
    lang: String,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${Strings.get("points_ledger", lang)} (${customer.loyaltyPoints} pts available)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == "bn") "এই গ্রাহকের কোনো পয়েন্ট লেনদেন পাওয়া যায়নি।" else "No points history for this customer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { tx ->
                        val isPositive = tx.points >= 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = dateFormat.format(Date(tx.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = (if (isPositive) "+" else "") + "${tx.points} pts",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) GreenPrimaryLight else Color(0xFFE65100),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
