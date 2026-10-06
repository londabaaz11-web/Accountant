package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Account
import com.example.data.model.AccountabilityReason
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.DailyCheckIn
import com.example.data.model.Debt
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.RecurringFrequency
import com.example.data.model.RecurringTransaction
import com.example.data.model.SavingsGoal
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.preferences.AppPreferences
import com.example.data.repository.FinanceRepository
import com.example.data.repository.FinancialSnapshot
import com.example.util.AccountabilityAnalyzer
import com.example.util.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MainUiState(
    val snapshot: FinancialSnapshot = FinancialSnapshot(
        totalBalance = 0.0,
        todayIncome = 0.0,
        todayExpense = 0.0,
        monthIncome = 0.0,
        monthExpense = 0.0,
        totalSavings = 0.0,
        totalMonthlyBudget = 0.0,
        remainingMonthlyBudget = 0.0,
        businessSales = 0.0,
        businessPurchases = 0.0,
        businessGrossProfit = 0.0,
        businessProfitMargin = 0.0,
        totalOwedToMe = 0.0,
        totalIOwe = 0.0
    ),
    val accounts: List<Account> = emptyList(),
    val accountBalances: Map<Long, Double> = emptyMap(),
    val categories: List<Category> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val checkIns: List<DailyCheckIn> = emptyList(),
    val recurringList: List<RecurringTransaction> = emptyList(),
    val insights: List<AccountabilityAnalyzer.Insight> = emptyList(),
    val currencySymbol: String = "Rs. ",
    val currencyCode: String = "PKR",
    val isPinLocked: Boolean = false,
    val isAppUnlocked: Boolean = false
)

enum class ScreenTab {
    HOME,
    TRANSACTIONS,
    BUDGET,
    REPORTS,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = FinanceRepository(database)
    val prefs = AppPreferences(application)

    private val _currencySymbol = MutableStateFlow(prefs.currencySymbol)
    val currencySymbol = _currencySymbol.asStateFlow()

    private val _currencyCode = MutableStateFlow(prefs.currencyCode)
    val currencyCode = _currencyCode.asStateFlow()

    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab = _currentTab.asStateFlow()

    private val _isAppUnlocked = MutableStateFlow(!prefs.isPinLockEnabled)
    val isAppUnlocked = _isAppUnlocked.asStateFlow()

    // Transaction form dialog visibility
    private val _showAddTransactionDialog = MutableStateFlow(false)
    val showAddTransactionDialog = _showAddTransactionDialog.asStateFlow()

    // Transaction to edit (null for new)
    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction = _editingTransaction.asStateFlow()

    val uiState: StateFlow<MainUiState> = combine(
        repository.allAccounts,
        repository.allTransactions,
        repository.allCategories,
        repository.allBudgets,
        repository.allGoals,
        repository.allDebts,
        repository.allCheckIns,
        repository.allRecurring,
        _currencySymbol,
        _isAppUnlocked
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val accounts = args[0] as List<Account>
        @Suppress("UNCHECKED_CAST")
        val transactions = args[1] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val categories = args[2] as List<Category>
        @Suppress("UNCHECKED_CAST")
        val budgets = args[3] as List<Budget>
        @Suppress("UNCHECKED_CAST")
        val goals = args[4] as List<SavingsGoal>
        @Suppress("UNCHECKED_CAST")
        val debts = args[5] as List<Debt>
        @Suppress("UNCHECKED_CAST")
        val checkIns = args[6] as List<DailyCheckIn>
        @Suppress("UNCHECKED_CAST")
        val recurring = args[7] as List<RecurringTransaction>
        val curSymbol = args[8] as String
        val unlocked = args[9] as Boolean

        val snapshot = repository.computeSnapshot(accounts, transactions, budgets, goals, debts)
        val accountBalances = repository.computeAccountBalances(accounts, transactions)
        val insights = AccountabilityAnalyzer.analyze(transactions, budgets, curSymbol)

        MainUiState(
            snapshot = snapshot,
            accounts = accounts,
            accountBalances = accountBalances,
            categories = categories,
            transactions = transactions,
            recentTransactions = transactions.take(15),
            budgets = budgets,
            savingsGoals = goals,
            debts = debts,
            checkIns = checkIns,
            recurringList = recurring,
            insights = insights,
            currencySymbol = curSymbol,
            currencyCode = prefs.currencyCode,
            isPinLocked = prefs.isPinLockEnabled,
            isAppUnlocked = unlocked
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun selectTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun openAddTransaction(transactionToEdit: TransactionEntity? = null) {
        _editingTransaction.value = transactionToEdit
        _showAddTransactionDialog.value = true
    }

    fun closeAddTransaction() {
        _showAddTransactionDialog.value = false
        _editingTransaction.value = null
    }

    fun saveTransaction(
        id: Long = 0,
        title: String,
        amount: Double,
        transactionType: TransactionType,
        category: String,
        accountId: Long,
        toAccountId: Long? = null,
        dateTimestamp: Long,
        note: String = "",
        tags: String = "",
        accountabilityReason: AccountabilityReason? = null,
        productName: String? = null,
        quantity: Double? = null,
        unitPrice: Double? = null,
        customerOrSupplierName: String? = null,
        isBusiness: Boolean = false
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = id,
                title = title,
                amount = amount,
                transactionType = transactionType,
                category = category,
                accountId = accountId,
                toAccountId = toAccountId,
                dateTimestamp = dateTimestamp,
                note = note,
                tags = tags,
                accountabilityReason = accountabilityReason,
                productName = productName,
                quantity = quantity,
                unitPrice = unitPrice,
                customerOrSupplierName = customerOrSupplierName,
                isBusiness = isBusiness
            )
            if (id == 0L) {
                repository.insertTransaction(entity)
            } else {
                repository.updateTransaction(entity)
            }
            closeAddTransaction()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Account Actions
    fun addAccount(name: String, type: String, initialBalance: Double, colorHex: String) {
        viewModelScope.launch {
            repository.insertAccount(
                Account(name = name, type = type, initialBalance = initialBalance, colorHex = colorHex)
            )
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    // Category Actions
    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(name = name, type = type, isCustom = true)
            )
        }
    }

    // Budget Actions
    fun saveBudget(id: Long = 0, category: String, monthlyLimit: Double) {
        viewModelScope.launch {
            val b = Budget(id = id, category = category, monthlyLimit = monthlyLimit, monthYear = "ALL")
            if (id == 0L) repository.insertBudget(b) else repository.updateBudget(b)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Savings Goals
    fun saveSavingsGoal(id: Long = 0, title: String, targetAmount: Double, savedAmount: Double, notes: String) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                id = id,
                title = title,
                targetAmount = targetAmount,
                savedAmount = savedAmount,
                notes = notes,
                isCompleted = savedAmount >= targetAmount
            )
            if (id == 0L) repository.insertGoal(goal) else repository.updateGoal(goal)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun addSavingsContribution(goal: SavingsGoal, addition: Double) {
        viewModelScope.launch {
            val updated = goal.copy(
                savedAmount = goal.savedAmount + addition,
                isCompleted = (goal.savedAmount + addition) >= goal.targetAmount
            )
            repository.updateGoal(updated)
        }
    }

    // Debts
    fun saveDebt(id: Long = 0, personName: String, amount: Double, paidAmount: Double, debtType: DebtType, notes: String) {
        viewModelScope.launch {
            val d = Debt(
                id = id,
                personName = personName,
                amount = amount,
                paidAmount = paidAmount,
                debtType = debtType,
                status = if (paidAmount >= amount) DebtStatus.SETTLED else if (paidAmount > 0) DebtStatus.PARTIALLY_PAID else DebtStatus.PENDING,
                notes = notes
            )
            if (id == 0L) repository.insertDebt(d) else repository.updateDebt(d)
        }
    }

    fun recordDebtPayment(debt: Debt, paymentAmount: Double, accountId: Long) {
        viewModelScope.launch {
            val newPaid = debt.paidAmount + paymentAmount
            val updated = debt.copy(
                paidAmount = newPaid,
                status = if (newPaid >= debt.amount) DebtStatus.SETTLED else DebtStatus.PARTIALLY_PAID
            )
            repository.updateDebt(updated)

            // Also record corresponding transaction
            val type = if (debt.debtType == DebtType.OWED_TO_ME) TransactionType.DEBT_RECEIVED else TransactionType.DEBT_PAID
            val title = if (debt.debtType == DebtType.OWED_TO_ME) "Debt Collected from ${debt.personName}" else "Debt Paid to ${debt.personName}"
            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = paymentAmount,
                    transactionType = type,
                    category = "Debt",
                    accountId = accountId,
                    dateTimestamp = System.currentTimeMillis(),
                    note = "Payment against debt of ${Formatters.formatCurrency(debt.amount, _currencySymbol.value)}"
                )
            )
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    // Daily Check-in
    fun saveDailyCheckIn(
        rating: String,
        stayedInBudget: Boolean,
        plannedSpending: Boolean,
        reflectionNotes: String,
        improvementGoal: String
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val dateKey = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
            repository.insertCheckIn(
                DailyCheckIn(
                    dateKey = dateKey,
                    rating = rating,
                    stayedInBudget = stayedInBudget,
                    plannedSpending = plannedSpending,
                    reflectionNotes = reflectionNotes,
                    improvementGoal = improvementGoal
                )
            )
        }
    }

    // Recurring
    fun saveRecurring(
        title: String,
        amount: Double,
        transactionType: TransactionType,
        category: String,
        accountId: Long,
        frequency: RecurringFrequency
    ) {
        viewModelScope.launch {
            repository.insertRecurring(
                RecurringTransaction(
                    title = title,
                    amount = amount,
                    transactionType = transactionType,
                    category = category,
                    accountId = accountId,
                    frequency = frequency,
                    nextExecutionTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteRecurring(recurring: RecurringTransaction) {
        viewModelScope.launch {
            repository.deleteRecurring(recurring)
        }
    }

    // Currency Setting
    fun setCurrency(code: String, symbol: String) {
        prefs.currencyCode = code
        prefs.currencySymbol = symbol
        _currencyCode.value = code
        _currencySymbol.value = symbol
    }

    // PIN lock
    fun setPin(pin: String) {
        prefs.pinCode = pin
        prefs.isPinLockEnabled = pin.isNotEmpty()
        _isAppUnlocked.value = pin.isEmpty()
    }

    fun disablePin() {
        prefs.pinCode = ""
        prefs.isPinLockEnabled = false
        _isAppUnlocked.value = true
    }

    fun verifyPin(input: String): Boolean {
        return if (prefs.pinCode == input) {
            _isAppUnlocked.value = true
            true
        } else {
            false
        }
    }
}
