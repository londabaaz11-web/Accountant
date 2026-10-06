package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.RecurringFrequency
import com.example.data.model.TransactionType
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.theme.ExpenseRed
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("settings_screen"),
        contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 96.dp)
    ) {
        item {
            Text(
                text = "Settings & Customization",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 1. Currency Section
        item {
            SectionHeader(title = "Currency & Locale")
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCurrencyDialog = true }
                    .testTag("currency_setting_row")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Active Currency", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${uiState.currencyCode} (${uiState.currencySymbol})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text("Change", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. Wallets & Accounts Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionHeader(title = "Payment Accounts & Wallets")
                TextButton(onClick = { showAddAccountDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Account")
                }
            }
        }

        items(uiState.accounts, key = { it.id }) { acc ->
            val bal = uiState.accountBalances[acc.id] ?: acc.initialBalance
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.padding(9.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(acc.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(acc.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            Formatters.formatCurrency(bal, uiState.currencySymbol),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.accounts.size > 1) {
                            IconButton(onClick = { viewModel.deleteAccount(acc) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // 3. Categories Management
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionHeader(title = "Custom Categories")
                TextButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Category")
                }
            }
            Text(
                text = "${uiState.categories.size} categories configured (Income & Expense).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 4. Recurring Transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionHeader(title = "Recurring Transactions")
                TextButton(onClick = { showAddRecurringDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Recurring")
                }
            }
            if (uiState.recurringList.isEmpty()) {
                Text(
                    text = "No recurring bills, subscriptions, or salaries configured.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(uiState.recurringList, key = { it.id }) { rec ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(rec.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("${rec.frequency.displayName} • ${rec.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(Formatters.formatCurrency(rec.amount, uiState.currencySymbol), fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.deleteRecurring(rec) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // 5. Security & Privacy
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(title = "Security & Offline Privacy")
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("App PIN Lock", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(if (uiState.isPinLocked) "Protected with 4-digit PIN" else "Disabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = uiState.isPinLocked,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    showPinDialog = true
                                } else {
                                    viewModel.disablePin()
                                    Toast.makeText(context, "PIN Lock Disabled", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("pin_lock_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "100% Offline Guarantee: No cloud servers, no account registration, no telemetry.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Currency Picker Dialog
    if (showCurrencyDialog) {
        val supportedCurrencies = listOf(
            "PKR" to "Rs. ",
            "USD" to "$",
            "EUR" to "€",
            "GBP" to "£",
            "AED" to "AED ",
            "SAR" to "SAR "
        )

        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    supportedCurrencies.forEach { (code, symbol) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrency(code, symbol)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$code ($symbol)", style = MaterialTheme.typography.bodyLarge)
                            if (uiState.currencyCode == code) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("Bank") }
        var initialBalStr by remember { mutableStateOf("0") }
        val accountTypes = listOf("Cash", "Bank", "Debit card", "Credit card", "Mobile wallet", "Business account", "Custom account")
        var typeDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = { Text("Add New Account") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Account Name (e.g. Meezan Bank, Cash, Nayapay)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = type,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            accountTypes.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = {
                                        type = t
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = initialBalStr,
                        onValueChange = { initialBalStr = it },
                        label = { Text("Initial Balance (${uiState.currencySymbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val initBal = initialBalStr.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) {
                            viewModel.addAccount(name.trim(), type, initBal, "#006C4C")
                            showAddAccountDialog = false
                        }
                    }
                ) { Text("Add Account") }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catType by remember { mutableStateOf("EXPENSE") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Custom Category") },
            text = {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.FilterChip(
                            selected = catType == "EXPENSE",
                            onClick = { catType = "EXPENSE" },
                            label = { Text("Expense Category") }
                        )
                        androidx.compose.material3.FilterChip(
                            selected = catType == "INCOME",
                            onClick = { catType = "INCOME" },
                            label = { Text("Income Category") }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            viewModel.addCategory(catName.trim(), catType)
                            showAddCategoryDialog = false
                        }
                    }
                ) { Text("Save Category") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Recurring Dialog
    if (showAddRecurringDialog) {
        var title by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }
        var frequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }
        var type by remember { mutableStateOf(TransactionType.EXPENSE) }
        var selectedCategory by remember { mutableStateOf("Bills") }
        var selectedAccountId by remember { mutableStateOf(uiState.accounts.firstOrNull()?.id ?: 1L) }

        AlertDialog(
            onDismissRequest = { showAddRecurringDialog = false },
            title = { Text("Add Recurring Item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title (e.g. Netflix, Apartment Rent, Internet)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount (${uiState.currencySymbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RecurringFrequency.values().forEach { freq ->
                            androidx.compose.material3.FilterChip(
                                selected = frequency == freq,
                                onClick = { frequency = freq },
                                label = { Text(freq.displayName) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull()
                        if (title.isNotBlank() && amt != null && amt > 0) {
                            viewModel.saveRecurring(title.trim(), amt, type, selectedCategory, selectedAccountId, frequency)
                            showAddRecurringDialog = false
                        }
                    }
                ) { Text("Create Recurring") }
            },
            dismissButton = {
                TextButton(onClick = { showAddRecurringDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Set PIN Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit Security PIN") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("Enter 4 digits") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4) confirmPinInput = it },
                        label = { Text("Confirm PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length != 4) {
                            error = "PIN must be exactly 4 digits"
                            return@Button
                        }
                        if (pinInput != confirmPinInput) {
                            error = "PINs do not match"
                            return@Button
                        }
                        viewModel.setPin(pinInput)
                        showPinDialog = false
                        Toast.makeText(context, "PIN Lock Enabled", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Set PIN") }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
