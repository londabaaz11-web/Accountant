package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.DailyCheckIn
import com.example.data.model.Debt
import com.example.data.model.RecurringTransaction
import com.example.data.model.SavingsGoal
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class FinancialSnapshot(
    val totalBalance: Double,
    val todayIncome: Double,
    val todayExpense: Double,
    val monthIncome: Double,
    val monthExpense: Double,
    val totalSavings: Double,
    val totalMonthlyBudget: Double,
    val remainingMonthlyBudget: Double,
    // Business specific
    val businessSales: Double,
    val businessPurchases: Double,
    val businessGrossProfit: Double,
    val businessProfitMargin: Double,
    // Debts
    val totalOwedToMe: Double,
    val totalIOwe: Double
)

class FinanceRepository(private val db: AppDatabase) {

    // Accounts
    val allAccounts: Flow<List<Account>> = db.accountDao().getAllAccounts()
    suspend fun getAccountById(id: Long) = db.accountDao().getAccountById(id)
    suspend fun insertAccount(account: Account) = db.accountDao().insertAccount(account)
    suspend fun updateAccount(account: Account) = db.accountDao().updateAccount(account)
    suspend fun deleteAccount(account: Account) = db.accountDao().deleteAccount(account)

    // Categories
    val allCategories: Flow<List<Category>> = db.categoryDao().getAllCategories()
    fun getCategoriesByType(type: String) = db.categoryDao().getCategoriesByType(type)
    suspend fun insertCategory(category: Category) = db.categoryDao().insertCategory(category)
    suspend fun deleteCategory(category: Category) = db.categoryDao().deleteCategory(category)

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val recentTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getRecentTransactions(20)
    suspend fun insertTransaction(transaction: TransactionEntity) = db.transactionDao().insertTransaction(transaction)
    suspend fun updateTransaction(transaction: TransactionEntity) = db.transactionDao().updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = db.transactionDao().deleteTransaction(transaction)
    suspend fun deleteTransactionById(id: Long) = db.transactionDao().deleteTransactionById(id)
    suspend fun getAllTransactionsSync() = db.transactionDao().getAllTransactionsSync()

    // Budgets
    val allBudgets: Flow<List<Budget>> = db.budgetDao().getAllBudgets()
    suspend fun insertBudget(budget: Budget) = db.budgetDao().insertBudget(budget)
    suspend fun updateBudget(budget: Budget) = db.budgetDao().updateBudget(budget)
    suspend fun deleteBudget(budget: Budget) = db.budgetDao().deleteBudget(budget)

    // Savings Goals
    val allGoals: Flow<List<SavingsGoal>> = db.savingsGoalDao().getAllGoals()
    suspend fun insertGoal(goal: SavingsGoal) = db.savingsGoalDao().insertGoal(goal)
    suspend fun updateGoal(goal: SavingsGoal) = db.savingsGoalDao().updateGoal(goal)
    suspend fun deleteGoal(goal: SavingsGoal) = db.savingsGoalDao().deleteGoal(goal)

    // Debts
    val allDebts: Flow<List<Debt>> = db.debtDao().getAllDebts()
    suspend fun insertDebt(debt: Debt) = db.debtDao().insertDebt(debt)
    suspend fun updateDebt(debt: Debt) = db.debtDao().updateDebt(debt)
    suspend fun deleteDebt(debt: Debt) = db.debtDao().deleteDebt(debt)

    // Daily Check-ins
    val allCheckIns: Flow<List<DailyCheckIn>> = db.dailyCheckInDao().getAllCheckIns()
    suspend fun getCheckInForDate(dateKey: String) = db.dailyCheckInDao().getCheckInForDate(dateKey)
    suspend fun insertCheckIn(checkIn: DailyCheckIn) = db.dailyCheckInDao().insertCheckIn(checkIn)

    // Recurring Transactions
    val allRecurring: Flow<List<RecurringTransaction>> = db.recurringTransactionDao().getAllRecurring()
    suspend fun insertRecurring(recurring: RecurringTransaction) = db.recurringTransactionDao().insertRecurring(recurring)
    suspend fun updateRecurring(recurring: RecurringTransaction) = db.recurringTransactionDao().updateRecurring(recurring)
    suspend fun deleteRecurring(recurring: RecurringTransaction) = db.recurringTransactionDao().deleteRecurring(recurring)

    /**
     * Compute real balances, cash flow, business metrics, and budgets
     */
    fun computeSnapshot(
        accounts: List<Account>,
        transactions: List<TransactionEntity>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        debts: List<Debt>
    ): FinancialSnapshot {
        // Base account starting balances
        val accountBalances = accounts.associate { it.id to it.initialBalance }.toMutableMap()

        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)
        val currentDay = cal.get(Calendar.DAY_OF_YEAR)

        var todayIncome = 0.0
        var todayExpense = 0.0
        var monthIncome = 0.0
        var monthExpense = 0.0

        var businessSales = 0.0
        var businessPurchases = 0.0

        val txnCal = Calendar.getInstance()

        for (txn in transactions) {
            txnCal.timeInMillis = txn.dateTimestamp
            val isToday = txnCal.get(Calendar.YEAR) == currentYear &&
                    txnCal.get(Calendar.DAY_OF_YEAR) == currentDay
            val isThisMonth = txnCal.get(Calendar.YEAR) == currentYear &&
                    txnCal.get(Calendar.MONTH) == currentMonth

            val amt = txn.amount

            when (txn.transactionType) {
                TransactionType.INCOME,
                TransactionType.SALARY,
                TransactionType.REFUND -> {
                    accountBalances[txn.accountId] = (accountBalances[txn.accountId] ?: 0.0) + amt
                    if (isToday) todayIncome += amt
                    if (isThisMonth) monthIncome += amt
                }
                TransactionType.EXPENSE,
                TransactionType.PURCHASE -> {
                    accountBalances[txn.accountId] = (accountBalances[txn.accountId] ?: 0.0) - amt
                    if (isToday) todayExpense += amt
                    if (isThisMonth) monthExpense += amt

                    if (txn.transactionType == TransactionType.PURCHASE || txn.isBusiness) {
                        businessPurchases += amt
                    }
                }
                TransactionType.SALE -> {
                    accountBalances[txn.accountId] = (accountBalances[txn.accountId] ?: 0.0) + amt
                    if (isToday) todayIncome += amt
                    if (isThisMonth) monthIncome += amt
                    businessSales += amt
                }
                TransactionType.TRANSFER -> {
                    // Internal transfer: does NOT count towards income or expense
                    accountBalances[txn.accountId] = (accountBalances[txn.accountId] ?: 0.0) - amt
                    if (txn.toAccountId != null) {
                        accountBalances[txn.toAccountId] = (accountBalances[txn.toAccountId] ?: 0.0) + amt
                    }
                }
                TransactionType.DEBT_RECEIVED -> {
                    // Received loan/debt: adds cash to account
                    accountBalances[txn.accountId] = (accountBalances[txn.accountId] ?: 0.0) + amt
                }
                TransactionType.DEBT_PAID -> {
                    // Paid back debt: reduces cash from account
                    accountBalances[txn.accountId] = (accountBalances[txn.accountId] ?: 0.0) - amt
                }
            }
        }

        val totalBalance = accountBalances.values.sum()
        val totalSavings = goals.sumOf { it.savedAmount }
        val totalMonthlyBudget = budgets.sumOf { it.monthlyLimit }
        val remainingMonthlyBudget = (totalMonthlyBudget - monthExpense).coerceAtLeast(0.0)

        val grossProfit = businessSales - businessPurchases
        val profitMargin = if (businessSales > 0) (grossProfit / businessSales) * 100.0 else 0.0

        val totalOwedToMe = debts.filter { it.debtType == com.example.data.model.DebtType.OWED_TO_ME }
            .sumOf { (it.amount - it.paidAmount).coerceAtLeast(0.0) }
        val totalIOwe = debts.filter { it.debtType == com.example.data.model.DebtType.I_OWE }
            .sumOf { (it.amount - it.paidAmount).coerceAtLeast(0.0) }

        return FinancialSnapshot(
            totalBalance = totalBalance,
            todayIncome = todayIncome,
            todayExpense = todayExpense,
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            totalSavings = totalSavings,
            totalMonthlyBudget = totalMonthlyBudget,
            remainingMonthlyBudget = remainingMonthlyBudget,
            businessSales = businessSales,
            businessPurchases = businessPurchases,
            businessGrossProfit = grossProfit,
            businessProfitMargin = profitMargin,
            totalOwedToMe = totalOwedToMe,
            totalIOwe = totalIOwe
        )
    }

    /**
     * Compute individual account current balances
     */
    fun computeAccountBalances(
        accounts: List<Account>,
        transactions: List<TransactionEntity>
    ): Map<Long, Double> {
        val map = accounts.associate { it.id to it.initialBalance }.toMutableMap()
        for (txn in transactions) {
            val amt = txn.amount
            when (txn.transactionType) {
                TransactionType.INCOME,
                TransactionType.SALARY,
                TransactionType.REFUND,
                TransactionType.SALE,
                TransactionType.DEBT_RECEIVED -> {
                    map[txn.accountId] = (map[txn.accountId] ?: 0.0) + amt
                }
                TransactionType.EXPENSE,
                TransactionType.PURCHASE,
                TransactionType.DEBT_PAID -> {
                    map[txn.accountId] = (map[txn.accountId] ?: 0.0) - amt
                }
                TransactionType.TRANSFER -> {
                    map[txn.accountId] = (map[txn.accountId] ?: 0.0) - amt
                    if (txn.toAccountId != null) {
                        map[txn.toAccountId] = (map[txn.toAccountId] ?: 0.0) + amt
                    }
                }
            }
        }
        return map
    }
}
