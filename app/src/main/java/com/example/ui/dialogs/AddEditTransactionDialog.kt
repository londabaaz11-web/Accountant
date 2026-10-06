package com.example.ui.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.AccountabilityReason
import com.example.data.model.Category
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.util.Formatters
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionDialog(
    transactionToEdit: TransactionEntity? = null,
    accounts: List<Account>,
    categories: List<Category>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        amount: Double,
        transactionType: TransactionType,
        category: String,
        accountId: Long,
        toAccountId: Long?,
        dateTimestamp: Long,
        note: String,
        tags: String,
        accountabilityReason: AccountabilityReason?,
        productName: String?,
        quantity: Double?,
        unitPrice: Double?,
        customerOrSupplierName: String?,
        isBusiness: Boolean
    ) -> Unit,
    onDelete: ((TransactionEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(transactionToEdit?.title ?: "") }
    var amountStr by remember { mutableStateOf(transactionToEdit?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var selectedType by remember { mutableStateOf(transactionToEdit?.transactionType ?: TransactionType.EXPENSE) }

    val defaultAccountId = accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id ?: 1L
    var selectedAccountId by remember { mutableLongStateOf(transactionToEdit?.accountId ?: defaultAccountId) }
    var selectedToAccountId by remember { mutableStateOf<Long?>(transactionToEdit?.toAccountId ?: accounts.getOrNull(1)?.id) }

    // Categories filtered by type
    val isIncomeType = selectedType in listOf(TransactionType.INCOME, TransactionType.SALARY, TransactionType.SALE)
    val defaultCategory = if (isIncomeType) {
        categories.firstOrNull { it.type == "INCOME" }?.name ?: "Salary"
    } else {
        categories.firstOrNull { it.type == "EXPENSE" }?.name ?: "Food"
    }
    var selectedCategory by remember { mutableStateOf(transactionToEdit?.category ?: defaultCategory) }

    var dateTimestamp by remember { mutableLongStateOf(transactionToEdit?.dateTimestamp ?: System.currentTimeMillis()) }
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }
    var tags by remember { mutableStateOf(transactionToEdit?.tags ?: "") }

    // Accountability question for expense
    var accountabilityReason by remember { mutableStateOf(transactionToEdit?.accountabilityReason ?: AccountabilityReason.NECESSARY) }

    // Sales & Purchases specifics
    var productName by remember { mutableStateOf(transactionToEdit?.productName ?: "") }
    var quantityStr by remember { mutableStateOf(transactionToEdit?.quantity?.toString() ?: "1") }
    var unitPriceStr by remember { mutableStateOf(transactionToEdit?.unitPrice?.toString() ?: "") }
    var customerOrSupplierName by remember { mutableStateOf(transactionToEdit?.customerOrSupplierName ?: "") }
    var isBusiness by remember { mutableStateOf(transactionToEdit?.isBusiness ?: (selectedType == TransactionType.SALE || selectedType == TransactionType.PURCHASE)) }

    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var toAccountDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (transactionToEdit == null) "Record Transaction" else "Edit Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (transactionToEdit != null && onDelete != null) {
                        IconButton(
                            onClick = {
                                onDelete(transactionToEdit)
                                onDismiss()
                            },
                            modifier = Modifier.testTag("delete_transaction_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ExpenseRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transaction Type Selector (chips row)
                Text(
                    text = "Transaction Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                if (type == TransactionType.SALE || type == TransactionType.PURCHASE) {
                                    isBusiness = true
                                }
                                if (type == TransactionType.SALARY) {
                                    selectedCategory = "Salary"
                                }
                            },
                            label = { Text(type.displayName) },
                            modifier = Modifier.testTag("type_chip_${type.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        errorMessage = null
                    },
                    label = { Text("Amount ($currencySymbol)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Title / Description") },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                TransactionType.EXPENSE -> "e.g. Grocery, Lunch, Petrol"
                                TransactionType.INCOME -> "e.g. Project freelance payment"
                                TransactionType.SALARY -> "e.g. October monthly salary"
                                TransactionType.SALE -> "e.g. Product sale to customer"
                                TransactionType.PURCHASE -> "e.g. Inventory raw material"
                                TransactionType.TRANSFER -> "e.g. ATM withdrawal / transfer"
                                else -> "Title"
                            }
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category dropdown (unless transfer)
                if (selectedType != TransactionType.TRANSFER) {
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("category_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategory = cat.name
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Account Selection
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                ) {
                    val currentAccountName = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: "Select Account"
                    OutlinedTextField(
                        value = currentAccountName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (selectedType == TransactionType.TRANSFER) "From Account" else "Account / Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("account_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${acc.type})") },
                                onClick = {
                                    selectedAccountId = acc.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // If Transfer, choose To Account
                if (selectedType == TransactionType.TRANSFER) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = toAccountDropdownExpanded,
                        onExpandedChange = { toAccountDropdownExpanded = !toAccountDropdownExpanded }
                    ) {
                        val currentToAccountName = accounts.firstOrNull { it.id == selectedToAccountId }?.name ?: "Select Destination Account"
                        OutlinedTextField(
                            value = currentToAccountName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toAccountDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("to_account_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = toAccountDropdownExpanded,
                            onDismissRequest = { toAccountDropdownExpanded = false }
                        ) {
                            accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${acc.type})") },
                                    onClick = {
                                        selectedToAccountId = acc.id
                                        toAccountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time Picker Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateTimestamp }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        timeInMillis = dateTimestamp
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                    }
                                    dateTimestamp = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Formatters.formatDate(dateTimestamp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateTimestamp }
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    val newCal = Calendar.getInstance().apply {
                                        timeInMillis = dateTimestamp
                                        set(Calendar.HOUR_OF_DAY, h)
                                        set(Calendar.MINUTE, min)
                                    }
                                    dateTimestamp = newCal.timeInMillis
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(Formatters.formatTime(dateTimestamp))
                    }
                }

                // CRITICAL ACCOUNTABILITY QUESTION: "Why did you spend this?" (For Expense / Purchase)
                if (selectedType == TransactionType.EXPENSE || selectedType == TransactionType.PURCHASE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Accountability Check:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "\"Why did you spend this?\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AccountabilityReason.values().forEach { reason ->
                                    FilterChip(
                                        selected = accountabilityReason == reason,
                                        onClick = { accountabilityReason = reason },
                                        label = { Text(reason.displayName) },
                                        modifier = Modifier.testTag("reason_chip_${reason.name}")
                                    )
                                }
                            }
                        }
                    }
                }

                // Sale/Purchase Specific Fields
                if (selectedType == TransactionType.SALE || selectedType == TransactionType.PURCHASE) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (selectedType == TransactionType.SALE) "Sale Information" else "Purchase Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product / Service / Item") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitPriceStr,
                            onValueChange = {
                                unitPriceStr = it
                                val q = quantityStr.toDoubleOrNull() ?: 1.0
                                val u = it.toDoubleOrNull() ?: 0.0
                                if (u > 0) {
                                    amountStr = (q * u).toString()
                                }
                            },
                            label = { Text("Unit Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerOrSupplierName,
                        onValueChange = { customerOrSupplierName = it },
                        label = { Text(if (selectedType == TransactionType.SALE) "Customer Name" else "Supplier Name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Business toggle
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Separate as Business Transaction",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isBusiness,
                        onCheckedChange = { isBusiness = it },
                        modifier = Modifier.testTag("business_switch")
                    )
                }

                // Note & Tags
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes (optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated, e.g. urgent, project, trip)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_button")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amountStr.toDoubleOrNull()
                            if (parsedAmount == null || parsedAmount <= 0.0) {
                                errorMessage = "Please enter a valid amount greater than 0"
                                return@Button
                            }
                            if (title.isBlank()) {
                                title = if (selectedType == TransactionType.TRANSFER) "Account Transfer" else selectedCategory
                            }

                            onSave(
                                transactionToEdit?.id ?: 0L,
                                title.trim(),
                                parsedAmount,
                                selectedType,
                                selectedCategory,
                                selectedAccountId,
                                if (selectedType == TransactionType.TRANSFER) selectedToAccountId else null,
                                dateTimestamp,
                                note.trim(),
                                tags.trim(),
                                if (selectedType in listOf(TransactionType.EXPENSE, TransactionType.PURCHASE)) accountabilityReason else null,
                                productName.ifBlank { null },
                                quantityStr.toDoubleOrNull(),
                                unitPriceStr.toDoubleOrNull(),
                                customerOrSupplierName.ifBlank { null },
                                isBusiness
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_transaction_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Transaction")
                    }
                }
            }
        }
    }
}
