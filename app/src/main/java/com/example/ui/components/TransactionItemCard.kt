package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountabilityReason
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningOrange
import com.example.util.Formatters

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    accountName: String,
    toAccountName: String? = null,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = when (transaction.transactionType) {
        TransactionType.INCOME,
        TransactionType.SALARY,
        TransactionType.SALE,
        TransactionType.REFUND,
        TransactionType.DEBT_RECEIVED -> true
        TransactionType.EXPENSE,
        TransactionType.PURCHASE,
        TransactionType.DEBT_PAID -> false
        TransactionType.TRANSFER -> null
    }

    val icon: ImageVector = when (transaction.transactionType) {
        TransactionType.INCOME -> Icons.Default.ArrowDownward
        TransactionType.SALARY -> Icons.Default.Work
        TransactionType.SALE -> Icons.Default.Receipt
        TransactionType.REFUND -> Icons.Default.ArrowDownward
        TransactionType.EXPENSE -> Icons.Default.ArrowUpward
        TransactionType.PURCHASE -> Icons.Default.ShoppingCart
        TransactionType.TRANSFER -> Icons.Default.ArrowForward
        TransactionType.DEBT_RECEIVED -> Icons.Default.AccountBalance
        TransactionType.DEBT_PAID -> Icons.Default.AccountBalance
    }

    val iconBgColor = when (isPositive) {
        true -> IncomeGreen.copy(alpha = 0.15f)
        false -> ExpenseRed.copy(alpha = 0.15f)
        null -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    }

    val iconTint = when (isPositive) {
        true -> IncomeGreen
        false -> ExpenseRed
        null -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("transaction_item_${transaction.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = iconBgColor,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.transactionType.displayName,
                    tint = iconTint,
                    modifier = Modifier
                        .padding(11.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title, Category, Date, Reason
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (transaction.transactionType == TransactionType.TRANSFER && toAccountName != null) {
                            "$accountName → $toAccountName"
                        } else {
                            accountName
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Accountability badge / tags
                if (transaction.accountabilityReason != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    ReasonBadge(reason = transaction.accountabilityReason)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount & Date
            Column(horizontalAlignment = Alignment.End) {
                val amountPrefix = when (isPositive) {
                    true -> "+"
                    false -> "-"
                    null -> "⇄ "
                }

                val amountColor = when (isPositive) {
                    true -> IncomeGreen
                    false -> ExpenseRed
                    null -> MaterialTheme.colorScheme.onSurface
                }

                Text(
                    text = "$amountPrefix${Formatters.formatCurrency(transaction.amount, currencySymbol)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = Formatters.formatDate(transaction.dateTimestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun ReasonBadge(reason: AccountabilityReason) {
    val (badgeBg, badgeText) = when (reason) {
        AccountabilityReason.NECESSARY -> Color(0xFF10B981).copy(alpha = 0.15f) to Color(0xFF047857)
        AccountabilityReason.PLANNED -> Color(0xFF3B82F6).copy(alpha = 0.15f) to Color(0xFF1D4ED8)
        AccountabilityReason.BUSINESS -> Color(0xFF8B5CF6).copy(alpha = 0.15f) to Color(0xFF6D28D9)
        AccountabilityReason.EMERGENCY -> Color(0xFFEF4444).copy(alpha = 0.15f) to Color(0xFFB91C1C)
        AccountabilityReason.IMPULSE -> Color(0xFFF59E0B).copy(alpha = 0.2f) to Color(0xFFB45309)
        AccountabilityReason.PERSONAL -> Color(0xFFEC4899).copy(alpha = 0.15f) to Color(0xFFBE185D)
        AccountabilityReason.OTHER -> Color.Gray.copy(alpha = 0.15f) to Color.DarkGray
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = badgeBg,
        modifier = Modifier.padding(top = 2.dp)
    ) {
        Text(
            text = "Why: ${reason.displayName}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = badgeText,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
