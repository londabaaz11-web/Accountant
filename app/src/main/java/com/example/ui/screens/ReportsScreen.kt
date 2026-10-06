package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountabilityReason
import com.example.data.model.TransactionType
import com.example.ui.MainUiState
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningOrange
import com.example.util.ExportHelper
import com.example.util.Formatters
import java.util.Calendar

@Composable
fun ReportsScreen(uiState: MainUiState) {
    val context = LocalContext.current
    var selectedTimeframe by remember { mutableIntStateOf(2) } // 0: Today, 1: Weekly, 2: Monthly, 3: Yearly, 4: All Time
    val currency = uiState.currencySymbol

    // Filter transactions based on timeframe
    val timeframeTransactions = remember(uiState.transactions, selectedTimeframe) {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        when (selectedTimeframe) {
            0 -> { // Today
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                uiState.transactions.filter { it.dateTimestamp >= todayStart }
            }
            1 -> { // This week
                val weekStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
                uiState.transactions.filter { it.dateTimestamp >= weekStart }
            }
            2 -> { // This month
                val monthStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
                uiState.transactions.filter { it.dateTimestamp >= monthStart }
            }
            3 -> { // This year
                val yearStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
                uiState.transactions.filter { it.dateTimestamp >= yearStart }
            }
            else -> uiState.transactions
        }
    }

    // Aggregations
    val incomeTotal = timeframeTransactions
        .filter { it.transactionType in listOf(TransactionType.INCOME, TransactionType.SALARY, TransactionType.SALE, TransactionType.REFUND) }
        .sumOf { it.amount }

    val expenseTotal = timeframeTransactions
        .filter { it.transactionType in listOf(TransactionType.EXPENSE, TransactionType.PURCHASE) }
        .sumOf { it.amount }

    val netCashFlow = incomeTotal - expenseTotal

    // Spending by Category
    val categorySpending = remember(timeframeTransactions) {
        timeframeTransactions
            .filter { it.transactionType in listOf(TransactionType.EXPENSE, TransactionType.PURCHASE) }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Spending by Accountability Reason
    val reasonSpending = remember(timeframeTransactions) {
        timeframeTransactions
            .filter { it.accountabilityReason != null }
            .groupBy { it.accountabilityReason!! }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Business Sales & Purchases
    val salesTotal = timeframeTransactions
        .filter { it.transactionType == TransactionType.SALE }
        .sumOf { it.amount }
    val purchaseTotal = timeframeTransactions
        .filter { it.transactionType == TransactionType.PURCHASE || (it.isBusiness && it.transactionType == TransactionType.EXPENSE) }
        .sumOf { it.amount }
    val grossProfit = salesTotal - purchaseTotal
    val profitMargin = if (salesTotal > 0) (grossProfit / salesTotal * 100) else 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("reports_screen"),
        contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 96.dp)
    ) {
        item {
            Text(
                text = "Reports & Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Timeframe Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Today", "Week", "Month", "Year", "All").forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedTimeframe == index,
                        onClick = { selectedTimeframe = index },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Cash Flow Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Income", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(Formatters.formatCurrency(incomeTotal, currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(Formatters.formatCurrency(expenseTotal, currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net Savings / Cash Flow:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                Formatters.formatCurrency(netCashFlow, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netCashFlow >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // Spending by Category Visual Breakdown
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (categorySpending.isEmpty()) {
                Text(
                    text = "No expenses recorded in this timeframe.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        categorySpending.forEach { (cat, amt) ->
                            val pct = if (expenseTotal > 0) (amt / expenseTotal).toFloat() else 0f
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${Formatters.formatCurrency(amt, currency)} (${(pct * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Spending by Accountability Reason
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Accountability Reason Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"Why did you spend?\" reflection summary",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (reasonSpending.isEmpty()) {
                Text(
                    text = "No reasons logged yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val reasonTotal = reasonSpending.sumOf { it.second }
                        reasonSpending.forEach { (reason, amt) ->
                            val pct = if (reasonTotal > 0) (amt / reasonTotal).toFloat() else 0f
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(reason.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${Formatters.formatCurrency(amt, currency)} (${(pct * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = if (reason == AccountabilityReason.IMPULSE) WarningOrange else MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Business P&L Section
        if (salesTotal > 0 || purchaseTotal > 0) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Business Profit & Loss",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Sales / Revenue:", style = MaterialTheme.typography.bodyMedium)
                            Text(Formatters.formatCurrency(salesTotal, currency), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Purchases / Costs:", style = MaterialTheme.typography.bodyMedium)
                            Text(Formatters.formatCurrency(purchaseTotal, currency), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gross Profit (Margin):", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${Formatters.formatCurrency(grossProfit, currency)} (${String.format("%.1f", profitMargin)}%)",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (grossProfit >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // Export Actions Section (CSV & PDF)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Export Financial Records",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Generate 100% offline CSV spreadsheets and PDF summary reports without cloud servers.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val accMap = uiState.accounts.associate { it.id to it.name }
                        val file = ExportHelper.exportToCsv(context, uiState.transactions, accMap)
                        if (file != null) {
                            ExportHelper.shareFile(context, file, "text/csv", "Share Accountability CSV")
                        } else {
                            Toast.makeText(context, "Export error", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("export_csv_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV")
                }

                Button(
                    onClick = {
                        val file = ExportHelper.generateFinancialSummaryPdf(
                            context,
                            uiState.snapshot,
                            uiState.transactions,
                            currency
                        )
                        if (file != null) {
                            ExportHelper.shareFile(context, file, "application/pdf", "Share Accountability PDF Report")
                        } else {
                            Toast.makeText(context, "PDF generation error", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("export_pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF")
                }
            }
        }
    }
}
