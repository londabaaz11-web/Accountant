package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? =
        value?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromAccountabilityReason(value: AccountabilityReason?): String? = value?.name

    @TypeConverter
    fun toAccountabilityReason(value: String?): AccountabilityReason? =
        value?.let { runCatching { AccountabilityReason.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromDebtType(value: DebtType?): String? = value?.name

    @TypeConverter
    fun toDebtType(value: String?): DebtType? =
        value?.let { runCatching { DebtType.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromDebtStatus(value: DebtStatus?): String? = value?.name

    @TypeConverter
    fun toDebtStatus(value: String?): DebtStatus? =
        value?.let { runCatching { DebtStatus.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromRecurringFrequency(value: RecurringFrequency?): String? = value?.name

    @TypeConverter
    fun toRecurringFrequency(value: String?): RecurringFrequency? =
        value?.let { runCatching { RecurringFrequency.valueOf(it) }.getOrNull() }
}

@Database(
    entities = [
        Account::class,
        Category::class,
        TransactionEntity::class,
        Budget::class,
        SavingsGoal::class,
        Debt::class,
        DailyCheckIn::class,
        RecurringTransaction::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun debtDao(): DebtDao
    abstract fun dailyCheckInDao(): DailyCheckInDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "accountability_master.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default starter data
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                prepopulateDefaults(database)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateDefaults(db: AppDatabase) {
            // Default accounts
            val cashId = db.accountDao().insertAccount(
                Account(name = "Cash", type = "Cash", initialBalance = 15000.0, isDefault = true, colorHex = "#10B981")
            )
            val bankId = db.accountDao().insertAccount(
                Account(name = "Bank Account", type = "Bank", initialBalance = 45000.0, colorHex = "#3B82F6")
            )
            db.accountDao().insertAccount(
                Account(name = "Mobile Wallet", type = "Mobile wallet", initialBalance = 5000.0, colorHex = "#F59E0B")
            )
            db.accountDao().insertAccount(
                Account(name = "Business Account", type = "Business account", initialBalance = 25000.0, colorHex = "#8B5CF6")
            )

            // Default categories
            val incomeCats = listOf(
                "Salary", "Business", "Freelance", "Sales", "Investment", "Other"
            ).map { Category(name = it, type = "INCOME") }

            val expenseCats = listOf(
                "Food", "Transport", "Fuel", "Bills", "Rent", "Shopping",
                "Entertainment", "Health", "Education", "Family", "Business",
                "Mobile/Internet", "Other"
            ).map { Category(name = it, type = "EXPENSE") }

            db.categoryDao().insertCategories(incomeCats + expenseCats)

            // Starter budgets
            db.budgetDao().insertBudget(Budget(category = "Food", monthlyLimit = 15000.0, monthYear = "ALL"))
            db.budgetDao().insertBudget(Budget(category = "Transport", monthlyLimit = 10000.0, monthYear = "ALL"))
            db.budgetDao().insertBudget(Budget(category = "Bills", monthlyLimit = 20000.0, monthYear = "ALL"))
            db.budgetDao().insertBudget(Budget(category = "Shopping", monthlyLimit = 5000.0, monthYear = "ALL"))

            // Sample starter transactions so user immediately sees rich, interactive data on first install
            val now = System.currentTimeMillis()
            val oneDayMs = 24L * 60 * 60 * 1000

            db.transactionDao().insertTransaction(
                TransactionEntity(
                    title = "Monthly Salary",
                    amount = 60000.0,
                    transactionType = TransactionType.SALARY,
                    category = "Salary",
                    accountId = bankId,
                    dateTimestamp = now - 2 * oneDayMs,
                    note = "Full-time salary credited"
                )
            )
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    title = "Grocery & Food",
                    amount = 1200.0,
                    transactionType = TransactionType.EXPENSE,
                    category = "Food",
                    accountId = cashId,
                    dateTimestamp = now - oneDayMs,
                    note = "Weekly pantry essentials",
                    accountabilityReason = AccountabilityReason.NECESSARY
                )
            )
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    title = "Fuel refill",
                    amount = 2000.0,
                    transactionType = TransactionType.EXPENSE,
                    category = "Fuel",
                    accountId = cashId,
                    dateTimestamp = now - 12 * 3600 * 1000,
                    note = "Bike fuel refill",
                    accountabilityReason = AccountabilityReason.PLANNED
                )
            )
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    title = "Design Consultation Sale",
                    amount = 5000.0,
                    transactionType = TransactionType.SALE,
                    category = "Sales",
                    accountId = bankId,
                    dateTimestamp = now - 4 * 3600 * 1000,
                    note = "Freelance UI work completed",
                    productName = "Landing Page Design",
                    quantity = 1.0,
                    unitPrice = 5000.0,
                    customerOrSupplierName = "Client Acme",
                    isBusiness = true
                )
            )

            // Starter savings goal
            db.savingsGoalDao().insertGoal(
                SavingsGoal(
                    title = "Emergency Fund",
                    targetAmount = 100000.0,
                    savedAmount = 35000.0,
                    notes = "3 months living expense buffer"
                )
            )
            db.savingsGoalDao().insertGoal(
                SavingsGoal(
                    title = "New Laptop",
                    targetAmount = 150000.0,
                    savedAmount = 65000.0,
                    notes = "Target for work upgrade"
                )
            )

            // Starter debts
            db.debtDao().insertDebt(
                Debt(
                    personName = "Ali Raza",
                    amount = 8000.0,
                    debtType = DebtType.OWED_TO_ME,
                    notes = "Lent for vehicle repair"
                )
            )
            db.debtDao().insertDebt(
                Debt(
                    personName = "Shopkeeper Store",
                    amount = 2500.0,
                    debtType = DebtType.I_OWE,
                    notes = "Monthly store tab"
                )
            )
        }
    }
}
