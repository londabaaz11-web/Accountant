package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.DailyCheckIn
import com.example.data.model.Debt
import com.example.data.model.RecurringTransaction
import com.example.data.model.SavingsGoal
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: String): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<Category>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateTimestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY dateTimestamp DESC")
    fun getTransactionsInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR toAccountId = :accountId ORDER BY dateTimestamp DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY category ASC")
    fun getAllBudgets(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, targetAmount DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY status ASC, dueDateTimestamp ASC")
    fun getAllDebts(): Flow<List<Debt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt): Long

    @Update
    suspend fun updateDebt(debt: Debt)

    @Delete
    suspend fun deleteDebt(debt: Debt)
}

@Dao
interface DailyCheckInDao {
    @Query("SELECT * FROM daily_checkins ORDER BY dateKey DESC")
    fun getAllCheckIns(): Flow<List<DailyCheckIn>>

    @Query("SELECT * FROM daily_checkins WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getCheckInForDate(dateKey: String): DailyCheckIn?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: DailyCheckIn)
}

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1")
    fun getActiveRecurring(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions")
    fun getAllRecurring(): Flow<List<RecurringTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(recurring: RecurringTransaction): Long

    @Update
    suspend fun updateRecurring(recurring: RecurringTransaction)

    @Delete
    suspend fun deleteRecurring(recurring: RecurringTransaction)
}
