package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionEntity
import com.example.ui.MainUiState
import com.example.ui.ScreenTab
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningOrange
import com.example.util.Formatters

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onAddTransaction: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onNavigateTab: (ScreenTab) -> Unit,
    onOpenDailyCheckIn: () -> Unit
) {
    val snapshot = uiState.snapshot
    val currency = uiState.currencySymbol
    val accountNames = remember(uiState.accounts) {
        uiState.accounts.associate { it.id to it.name }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Hero Balance Card
        item {
            HeroBalanceCard(
                uiState = uiState,
                onAddTransaction = onAddTransaction
            )
        }

        // Daily Check-in prompt banner
        item {
            DailyCheckInCard(onOpenCheckIn = onOpenDailyCheckIn)
        }

        // 4 Accountability Core Questions Card
        item {
            AccountabilityPillarsCard(uiState = uiState)
        }

        // Key Metrics Grid (Today & Monthly)
        item {
            MetricsGrid(snapshot = snapshot, currency = currency)
        }

        // Monthly Budget Status Preview
        item {
            MonthlyBudgetPreview(
                snapshot = snapshot,
                currency = currency,
                onViewBudgets = { onNavigateTab(ScreenTab.BUDGET) }
            )
        }

        // Business Summary if any sales/purchases recorded
        if (snapshot.businessSales > 0 || snapshot.businessPurchases > 0) {
            item {
                BusinessQuickCard(snapshot = snapshot, currency = currency)
            }
        }

        // Accountability Insights Feed
        if (uiState.insights.isNotEmpty()) {
            item {
                AccountabilityInsightsSection(insights = uiState.insights)
            }
        }

        // Recent Transactions Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = { onNavigateTab(ScreenTab.TRANSACTIONS) },
                    modifier = Modifier.testTag("view_all_transactions_button")
                ) {
                    Text("View All")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Transactions List
        if (uiState.recentTransactions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add Transaction' to record your first income or expense.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.recentTransactions, key = { it.id }) { txn ->
                val accName = accountNames[txn.accountId] ?: "Account"
                val toAccName = txn.toAccountId?.let { accountNames[it] }

                TransactionItemCard(
                    transaction = txn,
                    accountName = accName,
                    toAccountName = toAccName,
                    currencySymbol = currency,
                    onClick = { onEditTransaction(txn) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun HeroBalanceCard(
    uiState: MainUiState,
    onAddTransaction: () -> Unit
) {
    val snapshot = uiState.snapshot
    val currency = uiState.currencySymbol

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF004D36),
                        Color(0xFF006C4C),
                        Color(0xFF0D3324)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "TOTAL NET BALANCE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF89F8C6)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Formatters.formatCurrency(snapshot.totalBalance, currency),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sub-row: Savings & Active Accounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC0E0D4)
                    )
                    Text(
                        text = Formatters.formatCurrency(snapshot.totalSavings, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFDDB3)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Accounts Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC0E0D4)
                    )
                    Text(
                        text = "${uiState.accounts.size} Wallets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large + Add Transaction Button
            Button(
                onClick = onAddTransaction,
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6CDBAB),
                    contentColor = Color(0xFF003825)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("hero_add_transaction_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+ Add Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DailyCheckInCard(onOpenCheckIn: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable(onClick = onOpenCheckIn)
            .testTag("daily_checkin_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Money Accountability",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Did you stay disciplined with your spending today?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onOpenCheckIn) {
                Text("Check In", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AccountabilityPillarsCard(uiState: MainUiState) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "The 4 Money Questions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            QuestionRow(
                q = "1. How much money do I have?",
                a = Formatters.formatCurrency(uiState.snapshot.totalBalance, uiState.currencySymbol)
            )
            QuestionRow(
                q = "2. Where did my money come from?",
                a = "${Formatters.formatCurrency(uiState.snapshot.monthIncome, uiState.currencySymbol)} this month"
            )
            QuestionRow(
                q = "3. Where did my money go?",
                a = "${Formatters.formatCurrency(uiState.snapshot.monthExpense, uiState.currencySymbol)} spent this month"
            )
            val isResponsible = uiState.snapshot.monthIncome >= uiState.snapshot.monthExpense
            QuestionRow(
                q = "4. Am I managing responsibly?",
                a = if (isResponsible) "✓ Yes, Net positive cash flow" else "⚠️ Spending exceeds income this month"
            )
        }
    }
}

@Composable
fun QuestionRow(q: String, a: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = q,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = a,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MetricsGrid(snapshot: com.example.data.repository.FinancialSnapshot, currency: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        // Today's Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Today's Income",
                amount = snapshot.todayIncome,
                currency = currency,
                icon = Icons.Default.ArrowDownward,
                color = IncomeGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Today's Expenses",
                amount = snapshot.todayExpense,
                currency = currency,
                icon = Icons.Default.ArrowUpward,
                color = ExpenseRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Month's Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Monthly Income",
                amount = snapshot.monthIncome,
                currency = currency,
                icon = Icons.Default.TrendingUp,
                color = IncomeGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Monthly Expenses",
                amount = snapshot.monthExpense,
                currency = currency,
                icon = Icons.Default.TrendingDown,
                color = ExpenseRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    amount: Double,
    currency: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = Formatters.formatCurrency(amount, currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MonthlyBudgetPreview(
    snapshot: com.example.data.repository.FinancialSnapshot,
    currency: String,
    onViewBudgets: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable(onClick = onViewBudgets)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Monthly Budget Health",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manage →",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val budgetTotal = snapshot.totalMonthlyBudget
            val spent = snapshot.monthExpense
            val remaining = snapshot.remainingMonthlyBudget
            val progress = if (budgetTotal > 0) (spent / budgetTotal).toFloat().coerceIn(0f, 1f) else 0f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (progress > 0.9f) ExpenseRed else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${Formatters.formatCurrency(spent, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Remaining: ${Formatters.formatCurrency(remaining, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (remaining <= 0) ExpenseRed else IncomeGreen
                )
            }
        }
    }
}

@Composable
fun BusinessQuickCard(
    snapshot: com.example.data.repository.FinancialSnapshot,
    currency: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BusinessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Business Cash Flow",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Sales", style = MaterialTheme.typography.labelSmall)
                    Text(
                        Formatters.formatCurrency(snapshot.businessSales, currency),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text("Purchases/Costs", style = MaterialTheme.typography.labelSmall)
                    Text(
                        Formatters.formatCurrency(snapshot.businessPurchases, currency),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Gross Profit", style = MaterialTheme.typography.labelSmall)
                    Text(
                        Formatters.formatCurrency(snapshot.businessGrossProfit, currency),
                        fontWeight = FontWeight.Bold,
                        color = if (snapshot.businessGrossProfit >= 0) IncomeGreen else ExpenseRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun AccountabilityInsightsSection(insights: List<com.example.util.AccountabilityAnalyzer.Insight>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = WarningOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Accountability Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        insights.take(3).forEach { insight ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (insight.isWarning) {
                        ExpenseRed.copy(alpha = 0.08f)
                    } else if (insight.isPositive) {
                        IncomeGreen.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (insight.isWarning) ExpenseRed else if (insight.isPositive) IncomeGreen else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = insight.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
