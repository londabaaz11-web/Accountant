package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.AccountabilityReason
import com.example.data.model.Budget
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinancialSnapshot
import java.io.File
import java.io.FileWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    fun formatCurrency(amount: Double, symbol: String = "Rs. "): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
        val formattedNumber = formatter.format(amount)
        return if (amount < 0) {
            "-${symbol}${formatter.format(-amount)}"
        } else {
            "${symbol}${formattedNumber}"
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatMonthYear(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

object AccountabilityAnalyzer {
    data class Insight(
        val title: String,
        val message: String,
        val isWarning: Boolean = false,
        val isPositive: Boolean = false
    )

    fun analyze(
        transactions: List<TransactionEntity>,
        budgets: List<Budget>,
        currencySymbol: String
    ): List<Insight> {
        val insights = mutableListOf<Insight>()

        // 1. Impulse purchases check
        val impulseCount = transactions.count {
            it.accountabilityReason == AccountabilityReason.IMPULSE
        }
        val impulseTotal = transactions.filter {
            it.accountabilityReason == AccountabilityReason.IMPULSE
        }.sumOf { it.amount }

        if (impulseCount > 0) {
            insights.add(
                Insight(
                    title = "Impulse Spending Awareness",
                    message = "You recorded $impulseCount impulse purchase${if (impulseCount > 1) "s" else ""} totaling ${Formatters.formatCurrency(impulseTotal, currencySymbol)}. Awareness is the first step toward disciplined wealth building.",
                    isWarning = true
                )
            )
        } else {
            insights.add(
                Insight(
                    title = "Disciplined Spending",
                    message = "Zero impulse purchases logged recently! You are staying strictly intentional with your money.",
                    isPositive = true
                )
            )
        }

        // 2. Budget adherence check
        val expenseByCategory = transactions
            .filter { it.transactionType == TransactionType.EXPENSE || it.transactionType == TransactionType.PURCHASE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        for (budget in budgets) {
            val spent = expenseByCategory[budget.category] ?: 0.0
            if (spent > budget.monthlyLimit) {
                val over = spent - budget.monthlyLimit
                insights.add(
                    Insight(
                        title = "${budget.category} Budget Alert",
                        message = "Your ${budget.category.lowercase()} budget was ${Formatters.formatCurrency(budget.monthlyLimit, currencySymbol)}. You spent ${Formatters.formatCurrency(spent, currencySymbol)} (${Formatters.formatCurrency(over, currencySymbol)} over budget). Consider adjusting for next month.",
                        isWarning = true
                    )
                )
            } else if (spent >= budget.monthlyLimit * 0.85) {
                insights.add(
                    Insight(
                        title = "${budget.category} Near Limit",
                        message = "You have used ${(spent / budget.monthlyLimit * 100).toInt()}% of your ${budget.category} budget. Keep an eye on remaining expenses this month.",
                        isWarning = false
                    )
                )
            }
        }

        // 3. Largest expense
        val largestExpense = transactions
            .filter { it.transactionType == TransactionType.EXPENSE || it.transactionType == TransactionType.PURCHASE }
            .maxByOrNull { it.amount }

        if (largestExpense != null) {
            insights.add(
                Insight(
                    title = "Largest Expense",
                    message = "Your largest recorded expense was ${largestExpense.title} (${Formatters.formatCurrency(largestExpense.amount, currencySymbol)}) in ${largestExpense.category}.",
                    isPositive = false
                )
            )
        }

        // 4. Food spending highlight
        val foodSpent = expenseByCategory["Food"] ?: 0.0
        if (foodSpent > 0) {
            insights.add(
                Insight(
                    title = "Food & Sustenance",
                    message = "You spent ${Formatters.formatCurrency(foodSpent, currencySymbol)} on Food. Nourishing yourself well while respecting your plan is true financial wellness.",
                    isPositive = true
                )
            )
        }

        return insights
    }
}

object ExportHelper {
    /**
     * Export all transactions to CSV file and trigger Android Share sheet
     */
    fun exportToCsv(
        context: Context,
        transactions: List<TransactionEntity>,
        accountNames: Map<Long, String>
    ): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = File(exportDir, "accountability_transactions_${System.currentTimeMillis()}.csv")
            val writer = FileWriter(file)

            // Header
            writer.append("ID,Date,Time,Title,Type,Category,Amount,Account,Reason,Product,Customer/Supplier,IsBusiness,Notes\n")

            for (t in transactions) {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(t.dateTimestamp))
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(t.dateTimestamp))
                val account = accountNames[t.accountId] ?: "Account #${t.accountId}"
                val reason = t.accountabilityReason?.displayName ?: ""
                val product = (t.productName ?: "").replace(",", ";")
                val entityName = (t.customerOrSupplierName ?: "").replace(",", ";")
                val notes = t.note.replace("\n", " ").replace(",", ";")
                val title = t.title.replace(",", ";")

                writer.append("${t.id},$dateStr,$timeStr,\"$title\",${t.transactionType.name},\"${t.category}\",${t.amount},\"$account\",\"$reason\",\"$product\",\"$entityName\",${t.isBusiness},\"$notes\"\n")
            }

            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a clean financial summary PDF file locally using Android's native PdfDocument
     */
    fun generateFinancialSummaryPdf(
        context: Context,
        snapshot: FinancialSnapshot,
        transactions: List<TransactionEntity>,
        currencySymbol: String
    ): File? {
        return try {
            val pdfDoc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            paint.isAntiAlias = true

            // Header Banner
            paint.color = Color.parseColor("#006C4C") // Emerald
            canvas.drawRect(0f, 0f, 595f, 90f, paint)

            paint.color = Color.WHITE
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("ACCOUNTABILITY", 36f, 45f, paint)

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Offline Personal Finance & Wealth Report", 36f, 68f, paint)
            val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
            canvas.drawText(dateStr, 440f, 68f, paint)

            // Snapshot Metrics Box
            paint.color = Color.parseColor("#181D1A")
            paint.textSize = 14f
            paint.isFakeBoldText = true
            var yPos = 130f
            canvas.drawText("FINANCIAL SNAPSHOT", 36f, yPos, paint)

            paint.color = Color.parseColor("#707973")
            paint.strokeWidth = 1f
            canvas.drawLine(36f, yPos + 6f, 559f, yPos + 6f, paint)

            yPos += 30f
            paint.isFakeBoldText = false
            paint.textSize = 12f
            paint.color = Color.DKGRAY

            val leftCol = 36f
            val rightCol = 300f

            canvas.drawText("Total Net Balance: ${Formatters.formatCurrency(snapshot.totalBalance, currencySymbol)}", leftCol, yPos, paint)
            canvas.drawText("Total Savings: ${Formatters.formatCurrency(snapshot.totalSavings, currencySymbol)}", rightCol, yPos, paint)

            yPos += 20f
            canvas.drawText("Monthly Income: ${Formatters.formatCurrency(snapshot.monthIncome, currencySymbol)}", leftCol, yPos, paint)
            canvas.drawText("Monthly Expenses: ${Formatters.formatCurrency(snapshot.monthExpense, currencySymbol)}", rightCol, yPos, paint)

            yPos += 20f
            canvas.drawText("Monthly Budget: ${Formatters.formatCurrency(snapshot.totalMonthlyBudget, currencySymbol)}", leftCol, yPos, paint)
            canvas.drawText("Remaining Budget: ${Formatters.formatCurrency(snapshot.remainingMonthlyBudget, currencySymbol)}", rightCol, yPos, paint)

            yPos += 20f
            canvas.drawText("Business Sales: ${Formatters.formatCurrency(snapshot.businessSales, currencySymbol)}", leftCol, yPos, paint)
            canvas.drawText("Business Profit: ${Formatters.formatCurrency(snapshot.businessGrossProfit, currencySymbol)} (${String.format(Locale.US, "%.1f", snapshot.businessProfitMargin)}%)", rightCol, yPos, paint)

            // Recent Transactions Table
            yPos += 45f
            paint.color = Color.parseColor("#181D1A")
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("RECENT TRANSACTIONS", 36f, yPos, paint)
            canvas.drawLine(36f, yPos + 6f, 559f, yPos + 6f, paint)

            yPos += 25f
            paint.textSize = 10f
            paint.color = Color.GRAY
            paint.isFakeBoldText = true
            canvas.drawText("DATE", 36f, yPos, paint)
            canvas.drawText("TITLE & CATEGORY", 120f, yPos, paint)
            canvas.drawText("TYPE", 330f, yPos, paint)
            canvas.drawText("AMOUNT", 460f, yPos, paint)

            yPos += 15f
            paint.isFakeBoldText = false

            val limit = minOf(transactions.size, 18)
            for (i in 0 until limit) {
                val t = transactions[i]
                paint.color = Color.DKGRAY
                val dStr = SimpleDateFormat("MM/dd", Locale.US).format(Date(t.dateTimestamp))
                canvas.drawText(dStr, 36f, yPos, paint)
                val titleTruncated = if (t.title.length > 25) t.title.take(22) + "..." else t.title
                canvas.drawText("$titleTruncated (${t.category})", 120f, yPos, paint)
                canvas.drawText(t.transactionType.displayName, 330f, yPos, paint)

                if (t.transactionType in listOf(TransactionType.INCOME, TransactionType.SALARY, TransactionType.SALE)) {
                    paint.color = Color.parseColor("#10B981")
                } else {
                    paint.color = Color.parseColor("#EF4444")
                }
                canvas.drawText(Formatters.formatCurrency(t.amount, currencySymbol), 460f, yPos, paint)

                yPos += 18f
                if (yPos > 800f) break
            }

            // Footer
            paint.color = Color.GRAY
            paint.textSize = 9f
            canvas.drawText("Generated 100% locally by Accountability App • Track it. Understand it. Improve it.", 36f, 825f, paint)

            pdfDoc.finishPage(page)

            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val pdfFile = File(exportDir, "accountability_report_${System.currentTimeMillis()}.pdf")
            pdfDoc.writeTo(pdfFile.outputStream())
            pdfDoc.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates a full JSON backup file containing all accounts, transactions, budgets, goals, debts, check-ins, and recurring rules.
     */
    fun createFullBackupJson(
        context: Context,
        backupJsonString: String
    ): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val backupFile = File(exportDir, "accountability_backup_${System.currentTimeMillis()}.json")
            val writer = FileWriter(backupFile)
            writer.write(backupJsonString)
            writer.flush()
            writer.close()
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
