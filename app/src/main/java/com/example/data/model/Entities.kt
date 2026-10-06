package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "Cash", "Bank", "Debit card", "Credit card", "Mobile wallet", "Business account", "Custom account"
    val initialBalance: Double = 0.0,
    val isDefault: Boolean = false,
    val colorHex: String = "#006C4C"
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val iconName: String = "category",
    val isCustom: Boolean = false
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("accountId"),
        Index("dateTimestamp"),
        Index("transactionType")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val transactionType: TransactionType,
    val category: String,
    val accountId: Long,
    val toAccountId: Long? = null, // Used if transactionType == TRANSFER
    val dateTimestamp: Long, // Epoch ms
    val note: String = "",
    val tags: String = "", // Comma-separated
    val accountabilityReason: AccountabilityReason? = null,
    // Sale specifics (if SALE)
    val productName: String? = null,
    val quantity: Double? = null,
    val unitPrice: Double? = null,
    val customerOrSupplierName: String? = null,
    val isBusiness: Boolean = false,
    // Associated foreign keys if any
    val relatedDebtId: Long? = null,
    val relatedGoalId: Long? = null
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val monthYear: String // e.g. "2026-10" or "ALL" for recurring monthly
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetDateTimestamp: Long? = null,
    val notes: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val debtType: DebtType, // I_OWE or OWED_TO_ME
    val dueDateTimestamp: Long? = null,
    val status: DebtStatus = DebtStatus.PENDING,
    val notes: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_checkins")
data class DailyCheckIn(
    @PrimaryKey val dateKey: String, // e.g. "2026-10-06"
    val timestamp: Long = System.currentTimeMillis(),
    val rating: String = "Good", // "Great", "Good", "Needs Discipline", "Overspent"
    val stayedInBudget: Boolean = true,
    val plannedSpending: Boolean = true,
    val reflectionNotes: String = "",
    val improvementGoal: String = ""
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val transactionType: TransactionType,
    val category: String,
    val accountId: Long,
    val frequency: RecurringFrequency,
    val nextExecutionTimestamp: Long,
    val lastExecutedTimestamp: Long? = null,
    val isActive: Boolean = true
)
