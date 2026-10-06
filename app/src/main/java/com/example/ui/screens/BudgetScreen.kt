package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Budget
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.data.model.SavingsGoal
import com.example.data.model.TransactionType
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningOrange
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    uiState: MainUiState,
    viewModel: MainViewModel
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Budgets, 1: Savings Goals, 2: Debt Tracking
    val currency = uiState.currencySymbol

    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().testTag("budget_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Text(
                text = "Budget & Planning",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
            )

            // Sub Tabs: Budgets, Savings Goals, Debts
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Budgets") }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Savings Goals") }
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    text = { Text("Debt Tracking") }
                )
            }

            // Tab Content
            when (selectedSubTab) {
                0 -> BudgetsTabContent(uiState = uiState, onAddBudget = { showAddBudgetDialog = true }, onDeleteBudget = { viewModel.deleteBudget(it) })
                1 -> SavingsGoalsTabContent(
                    uiState = uiState,
                    onAddGoal = { showAddGoalDialog = true },
                    onDeleteGoal = { viewModel.deleteSavingsGoal(it) },
                    onAddContribution = { goal, amt -> viewModel.addSavingsContribution(goal, amt) }
                )
                2 -> DebtsTabContent(
                    uiState = uiState,
                    onAddDebt = { showAddDebtDialog = true },
                    onDeleteDebt = { viewModel.deleteDebt(it) },
                    onRecordPayment = { debt, amt, accId -> viewModel.recordDebtPayment(debt, amt, accId) }
                )
            }
        }

        // Floating Action Button for current tab
        FloatingActionButton(
            onClick = {
                when (selectedSubTab) {
                    0 -> showAddBudgetDialog = true
                    1 -> showAddGoalDialog = true
                    2 -> showAddDebtDialog = true
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp)
                .testTag("budget_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Item")
        }
    }

    // Dialog: Add Budget
    if (showAddBudgetDialog) {
        var selectedCategory by remember { mutableStateOf(uiState.categories.firstOrNull { it.type == "EXPENSE" }?.name ?: "Food") }
        var limitStr by remember { mutableStateOf("") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddBudgetDialog = false },
            title = { Text("Set Monthly Budget") },
            text = {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Expense Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            uiState.categories
                                .filter { it.type == "EXPENSE" }
                                .map { it.name }
                                .distinct()
                                .forEach { catName ->
                                    DropdownMenuItem(
                                        text = { Text(catName) },
                                        onClick = {
                                            selectedCategory = catName
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = limitStr,
                        onValueChange = { limitStr = it },
                        label = { Text("Monthly Limit ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = limitStr.toDoubleOrNull()
                        if (limit != null && limit > 0) {
                            viewModel.saveBudget(category = selectedCategory, monthlyLimit = limit)
                            showAddBudgetDialog = false
                        }
                    }
                ) { Text("Save Budget") }
            },
            dismissButton = {
                TextButton(onClick = { showAddBudgetDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Add Savings Goal
    if (showAddGoalDialog) {
        var title by remember { mutableStateOf("") }
        var targetStr by remember { mutableStateOf("") }
        var savedStr by remember { mutableStateOf("0") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("New Savings Goal") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Goal Name (e.g. New Laptop, Emergency Fund)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text("Target Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = savedStr,
                        onValueChange = { savedStr = it },
                        label = { Text("Already Saved ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetStr.toDoubleOrNull()
                        val saved = savedStr.toDoubleOrNull() ?: 0.0
                        if (!title.isBlank() && target != null && target > 0) {
                            viewModel.saveSavingsGoal(
                                title = title.trim(),
                                targetAmount = target,
                                savedAmount = saved,
                                notes = notes.trim()
                            )
                            showAddGoalDialog = false
                        }
                    }
                ) { Text("Create Goal") }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Add Debt
    if (showAddDebtDialog) {
        var personName by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }
        var debtType by remember { mutableStateOf(DebtType.I_OWE) }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDebtDialog = false },
            title = { Text("Record Debt / Loan") },
            text = {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.FilterChip(
                            selected = debtType == DebtType.I_OWE,
                            onClick = { debtType = DebtType.I_OWE },
                            label = { Text("Money I Owe") }
                        )
                        androidx.compose.material3.FilterChip(
                            selected = debtType == DebtType.OWED_TO_ME,
                            onClick = { debtType = DebtType.OWED_TO_ME },
                            label = { Text("Money Owed to Me") }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text("Person / Institution Name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Total Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Reason") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull()
                        if (personName.isNotBlank() && amt != null && amt > 0) {
                            viewModel.saveDebt(
                                personName = personName.trim(),
                                amount = amt,
                                paidAmount = 0.0,
                                debtType = debtType,
                                notes = notes.trim()
                            )
                            showAddDebtDialog = false
                        }
                    }
                ) { Text("Record Debt") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDebtDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun BudgetsTabContent(
    uiState: MainUiState,
    onAddBudget: () -> Unit,
    onDeleteBudget: (Budget) -> Unit
) {
    val currency = uiState.currencySymbol
    // Calculate category spending for the current month
    val categorySpent = remember(uiState.transactions) {
        uiState.transactions
            .filter { it.transactionType == TransactionType.EXPENSE || it.transactionType == TransactionType.PURCHASE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        if (uiState.budgets.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    ) {
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Budgets Defined", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Create category spending caps (e.g. Food Rs.15,000) to receive accountability warnings.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(uiState.budgets, key = { it.id }) { budget ->
                val spent = categorySpent[budget.category] ?: 0.0
                val remaining = budget.monthlyLimit - spent
                val percentUsed = (spent / budget.monthlyLimit * 100.0).coerceAtLeast(0.0)
                val progress = (spent / budget.monthlyLimit).toFloat().coerceIn(0f, 1f)
                val isOverBudget = spent > budget.monthlyLimit
                val isNearLimit = spent >= budget.monthlyLimit * 0.85 && !isOverBudget

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = budget.category,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Limit: ${Formatters.formatCurrency(budget.monthlyLimit, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteBudget(budget) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (isOverBudget) ExpenseRed else if (isNearLimit) WarningOrange else IncomeGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: ${Formatters.formatCurrency(spent, currency)} (${percentUsed.toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isOverBudget) "Over by ${Formatters.formatCurrency(-remaining, currency)}" else "Remaining: ${Formatters.formatCurrency(remaining, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverBudget) ExpenseRed else IncomeGreen
                            )
                        }

                        // Helpful warning banner if close or over
                        if (isOverBudget) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Accountability Warning: Budget exceeded by ${Formatters.formatCurrency(-remaining, currency)}!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (isNearLimit) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Caution: 85%+ used this month. Spend mindfully.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarningOrange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsGoalsTabContent(
    uiState: MainUiState,
    onAddGoal: () -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit,
    onAddContribution: (SavingsGoal, Double) -> Unit
) {
    val currency = uiState.currencySymbol
    var goalToContribute by remember { mutableStateOf<SavingsGoal?>(null) }
    var contributionStr by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        if (uiState.savingsGoals.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Savings Goals Set", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Set targets like 'Emergency Fund' or 'New Laptop' to monitor progress.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(uiState.savingsGoals, key = { it.id }) { goal ->
                val progress = (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                val remaining = (goal.targetAmount - goal.savedAmount).coerceAtLeast(0.0)
                val percent = (goal.savedAmount / goal.targetAmount * 100.0).coerceAtMost(100.0)

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (goal.notes.isNotBlank()) {
                                    Text(goal.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { onDeleteGoal(goal) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Saved: ${Formatters.formatCurrency(goal.savedAmount, currency)} (${percent.toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Target: ${Formatters.formatCurrency(goal.targetAmount, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Remaining: ${Formatters.formatCurrency(remaining, currency)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = {
                                goalToContribute = goal
                                contributionStr = ""
                            }) {
                                Text("+ Add Saved Amount")
                            }
                        }
                    }
                }
            }
        }
    }

    if (goalToContribute != null) {
        AlertDialog(
            onDismissRequest = { goalToContribute = null },
            title = { Text("Contribute to ${goalToContribute?.title}") },
            text = {
                OutlinedTextField(
                    value = contributionStr,
                    onValueChange = { contributionStr = it },
                    label = { Text("Amount ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = contributionStr.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            goalToContribute?.let { onAddContribution(it, amt) }
                            goalToContribute = null
                        }
                    }
                ) { Text("Save Contribution") }
            },
            dismissButton = {
                TextButton(onClick = { goalToContribute = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DebtsTabContent(
    uiState: MainUiState,
    onAddDebt: () -> Unit,
    onDeleteDebt: (Debt) -> Unit,
    onRecordPayment: (Debt, Double, Long) -> Unit
) {
    val currency = uiState.currencySymbol
    var debtToPay by remember { mutableStateOf<Debt?>(null) }
    var payAmountStr by remember { mutableStateOf("") }
    var selectedPayAccountId by remember { mutableStateOf(uiState.accounts.firstOrNull()?.id ?: 1L) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        // Debt Summary Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Money I Owe", style = MaterialTheme.typography.bodySmall)
                        Text(
                            Formatters.formatCurrency(uiState.snapshot.totalIOwe, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Owed to Me", style = MaterialTheme.typography.bodySmall)
                        Text(
                            Formatters.formatCurrency(uiState.snapshot.totalOwedToMe, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }
            }
        }

        if (uiState.debts.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    ) {
                        Text("No Debts or Loans Recorded", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Track money you owe to creditors or money owed to you.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(uiState.debts, key = { it.id }) { debt ->
                val remaining = (debt.amount - debt.paidAmount).coerceAtLeast(0.0)
                val isIOwe = debt.debtType == DebtType.I_OWE

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isIOwe) "I Owe: " else "Owed to Me: ",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIOwe) ExpenseRed else IncomeGreen
                                    )
                                    Text(
                                        text = debt.personName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (debt.notes.isNotBlank()) {
                                    Text(debt.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { onDeleteDebt(debt) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total: ${Formatters.formatCurrency(debt.amount, currency)}", style = MaterialTheme.typography.bodySmall)
                            Text("Paid: ${Formatters.formatCurrency(debt.paidAmount, currency)}", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Remaining: ${Formatters.formatCurrency(remaining, currency)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isIOwe) ExpenseRed else IncomeGreen
                        )

                        if (remaining > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = {
                                    debtToPay = debt
                                    payAmountStr = remaining.toString()
                                }) {
                                    Text(if (isIOwe) "Record Payment" else "Record Collection")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (debtToPay != null) {
        AlertDialog(
            onDismissRequest = { debtToPay = null },
            title = { Text(if (debtToPay?.debtType == DebtType.I_OWE) "Pay Debt to ${debtToPay?.personName}" else "Collect Debt from ${debtToPay?.personName}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = payAmountStr,
                        onValueChange = { payAmountStr = it },
                        label = { Text("Payment Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = payAmountStr.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            debtToPay?.let { onRecordPayment(it, amt, selectedPayAccountId) }
                            debtToPay = null
                        }
                    }
                ) { Text("Confirm & Log Transaction") }
            },
            dismissButton = {
                TextButton(onClick = { debtToPay = null }) { Text("Cancel") }
            }
        )
    }
}
