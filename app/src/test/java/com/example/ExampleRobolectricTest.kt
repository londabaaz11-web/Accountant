package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Account
import com.example.data.model.AccountabilityReason
import com.example.data.model.Budget
import com.example.data.model.Debt
import com.example.data.model.SavingsGoal
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.util.AccountabilityAnalyzer
import com.example.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Accountability", appName)
    }

    @Test
    fun testCurrencyFormatting() {
        assertEquals("Rs. 15,000", Formatters.formatCurrency(15000.0, "Rs. "))
        assertEquals("-Rs. 1,200", Formatters.formatCurrency(-1200.0, "Rs. "))
    }

    @Test
    fun testAccountabilityAnalyzerDetectsImpulse() {
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                title = "Impulse Gadget",
                amount = 4500.0,
                transactionType = TransactionType.EXPENSE,
                category = "Shopping",
                accountId = 1L,
                dateTimestamp = System.currentTimeMillis(),
                accountabilityReason = AccountabilityReason.IMPULSE
            )
        )
        val insights = AccountabilityAnalyzer.analyze(transactions, emptyList(), "Rs. ")
        assertTrue(insights.any { it.isWarning && it.title.contains("Impulse") })
    }
}
