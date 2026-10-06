package com.example.data.model

enum class TransactionType(val displayName: String) {
    INCOME("Income"),
    EXPENSE("Expense"),
    SALARY("Salary"),
    SALE("Sale"),
    PURCHASE("Purchase"),
    TRANSFER("Transfer"),
    REFUND("Refund"),
    DEBT_RECEIVED("Debt Received"),
    DEBT_PAID("Debt Paid")
}

enum class AccountabilityReason(val displayName: String) {
    NECESSARY("Necessary"),
    PLANNED("Planned"),
    BUSINESS("Business"),
    EMERGENCY("Emergency"),
    IMPULSE("Impulse"),
    PERSONAL("Personal"),
    OTHER("Other")
}

enum class DebtType {
    I_OWE,       // Money I owe to someone
    OWED_TO_ME   // Money someone owes to me
}

enum class DebtStatus {
    PENDING,
    PARTIALLY_PAID,
    SETTLED
}

enum class RecurringFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}
