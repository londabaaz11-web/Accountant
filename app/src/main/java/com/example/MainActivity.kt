package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.dialogs.AddEditTransactionDialog
import com.example.ui.dialogs.DailyCheckInDialog
import com.example.ui.dialogs.PinLockScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.AccountabilityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccountabilityTheme {
                AccountabilityApp()
            }
        }
    }
}

@Composable
fun AccountabilityApp(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val showAddTxnDialog by viewModel.showAddTransactionDialog.collectAsStateWithLifecycle()
    val editingTxn by viewModel.editingTransaction.collectAsStateWithLifecycle()

    var showDailyCheckInDialog by remember { mutableStateOf(false) }

    // If app is PIN locked and not unlocked yet, show PIN lock screen
    if (uiState.isPinLocked && !uiState.isAppUnlocked) {
        PinLockScreen(
            onVerifyPin = { viewModel.verifyPin(it) }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HOME,
                    onClick = { viewModel.selectTab(ScreenTab.HOME) },
                    icon = {
                        Icon(
                            if (currentTab == ScreenTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.TRANSACTIONS,
                    onClick = { viewModel.selectTab(ScreenTab.TRANSACTIONS) },
                    icon = {
                        Icon(
                            if (currentTab == ScreenTab.TRANSACTIONS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                            contentDescription = "Transactions"
                        )
                    },
                    label = { Text("Transactions") },
                    modifier = Modifier.testTag("nav_transactions")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.BUDGET,
                    onClick = { viewModel.selectTab(ScreenTab.BUDGET) },
                    icon = {
                        Icon(
                            if (currentTab == ScreenTab.BUDGET) Icons.Filled.PieChart else Icons.Outlined.PieChart,
                            contentDescription = "Budget"
                        )
                    },
                    label = { Text("Budget") },
                    modifier = Modifier.testTag("nav_budget")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.REPORTS,
                    onClick = { viewModel.selectTab(ScreenTab.REPORTS) },
                    icon = {
                        Icon(
                            if (currentTab == ScreenTab.REPORTS) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                            contentDescription = "Reports"
                        )
                    },
                    label = { Text("Reports") },
                    modifier = Modifier.testTag("nav_reports")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.SETTINGS,
                    onClick = { viewModel.selectTab(ScreenTab.SETTINGS) },
                    icon = {
                        Icon(
                            if (currentTab == ScreenTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.HOME -> HomeScreen(
                    uiState = uiState,
                    onAddTransaction = { viewModel.openAddTransaction() },
                    onEditTransaction = { viewModel.openAddTransaction(it) },
                    onNavigateTab = { viewModel.selectTab(it) },
                    onOpenDailyCheckIn = { showDailyCheckInDialog = true }
                )
                ScreenTab.TRANSACTIONS -> TransactionsScreen(
                    uiState = uiState,
                    onAddTransaction = { viewModel.openAddTransaction() },
                    onEditTransaction = { viewModel.openAddTransaction(it) }
                )
                ScreenTab.BUDGET -> BudgetScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                ScreenTab.REPORTS -> ReportsScreen(
                    uiState = uiState
                )
                ScreenTab.SETTINGS -> SettingsScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }

    // Add / Edit Transaction Dialog
    if (showAddTxnDialog) {
        AddEditTransactionDialog(
            transactionToEdit = editingTxn,
            accounts = uiState.accounts,
            categories = uiState.categories,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { viewModel.closeAddTransaction() },
            onSave = { id, title, amount, type, category, accountId, toAccountId, timestamp, note, tags, reason, prod, qty, unitPrice, customer, isBiz ->
                viewModel.saveTransaction(
                    id = id,
                    title = title,
                    amount = amount,
                    transactionType = type,
                    category = category,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    dateTimestamp = timestamp,
                    note = note,
                    tags = tags,
                    accountabilityReason = reason,
                    productName = prod,
                    quantity = qty,
                    unitPrice = unitPrice,
                    customerOrSupplierName = customer,
                    isBusiness = isBiz
                )
            },
            onDelete = { viewModel.deleteTransaction(it) }
        )
    }

    // Daily Check-in Dialog
    if (showDailyCheckInDialog) {
        DailyCheckInDialog(
            onDismiss = { showDailyCheckInDialog = false },
            onSave = { rating, stayedInBudget, planned, reflection, improvement ->
                viewModel.saveDailyCheckIn(rating, stayedInBudget, planned, reflection, improvement)
            }
        )
    }
}
