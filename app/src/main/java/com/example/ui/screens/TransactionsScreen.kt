package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.MainUiState
import com.example.ui.components.TransactionItemCard

@Composable
fun TransactionsScreen(
    uiState: MainUiState,
    onAddTransaction: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedAccountFilter by remember { mutableStateOf<Long?>(null) }
    var sortNewestFirst by remember { mutableStateOf(true) }

    val accountNames = remember(uiState.accounts) {
        uiState.accounts.associate { it.id to it.name }
    }

    // Filter and Sort logic
    val filteredTransactions = remember(
        uiState.transactions,
        searchQuery,
        selectedTypeFilter,
        selectedCategoryFilter,
        selectedAccountFilter,
        sortNewestFirst
    ) {
        var list = uiState.transactions

        // Search text
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.note.lowercase().contains(q) ||
                        it.tags.lowercase().contains(q) ||
                        (it.productName?.lowercase()?.contains(q) == true) ||
                        (it.customerOrSupplierName?.lowercase()?.contains(q) == true)
            }
        }

        // Type filter
        if (selectedTypeFilter != null) {
            list = list.filter { it.transactionType == selectedTypeFilter }
        }

        // Category filter
        if (selectedCategoryFilter != null) {
            list = list.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }

        // Account filter
        if (selectedAccountFilter != null) {
            list = list.filter { it.accountId == selectedAccountFilter || it.toAccountId == selectedAccountFilter }
        }

        // Sorting
        if (sortNewestFirst) {
            list.sortedByDescending { it.dateTimestamp }
        } else {
            list.sortedBy { it.dateTimestamp }
        }
    }

    Box(modifier = Modifier.fillMaxSize().testTag("transactions_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Transactions History",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search title, category, tags, notes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_transactions_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter & Sort Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sort toggle chip
                        FilterChip(
                            selected = false,
                            onClick = { sortNewestFirst = !sortNewestFirst },
                            leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text(if (sortNewestFirst) "Newest First" else "Oldest First") }
                        )

                        // All Types Chip
                        FilterChip(
                            selected = selectedTypeFilter == null,
                            onClick = { selectedTypeFilter = null },
                            label = { Text("All Types") }
                        )

                        TransactionType.values().forEach { tType ->
                            FilterChip(
                                selected = selectedTypeFilter == tType,
                                onClick = {
                                    selectedTypeFilter = if (selectedTypeFilter == tType) null else tType
                                },
                                label = { Text(tType.displayName) }
                            )
                        }
                    }

                    // Secondary Account Filter Chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedAccountFilter == null,
                            onClick = { selectedAccountFilter = null },
                            label = { Text("All Accounts") }
                        )
                        uiState.accounts.forEach { acc ->
                            FilterChip(
                                selected = selectedAccountFilter == acc.id,
                                onClick = {
                                    selectedAccountFilter = if (selectedAccountFilter == acc.id) null else acc.id
                                },
                                label = { Text(acc.name) }
                            )
                        }
                    }

                    // Category Filter Chips (single/deduplicated)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All Categories") }
                        )
                        uiState.categories.map { it.name }.distinct().forEach { catName ->
                            FilterChip(
                                selected = selectedCategoryFilter == catName,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == catName) null else catName
                                },
                                label = { Text(catName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Showing ${filteredTransactions.size} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            Text(
                                text = "No transactions found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing filters or search keywords.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { txn ->
                    val accName = accountNames[txn.accountId] ?: "Account"
                    val toAccName = txn.toAccountId?.let { accountNames[it] }

                    TransactionItemCard(
                        transaction = txn,
                        accountName = accName,
                        toAccountName = toAccName,
                        currencySymbol = uiState.currencySymbol,
                        onClick = { onEditTransaction(txn) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Floating Action Button to quickly add transaction
        FloatingActionButton(
            onClick = onAddTransaction,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp)
                .testTag("transactions_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }
}
