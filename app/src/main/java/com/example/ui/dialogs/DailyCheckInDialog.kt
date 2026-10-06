package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DailyCheckInDialog(
    onDismiss: () -> Unit,
    onSave: (
        rating: String,
        stayedInBudget: Boolean,
        plannedSpending: Boolean,
        reflectionNotes: String,
        improvementGoal: String
    ) -> Unit
) {
    var rating by remember { mutableStateOf("Good") }
    var stayedInBudget by remember { mutableStateOf(true) }
    var plannedSpending by remember { mutableStateOf(true) }
    var reflectionNotes by remember { mutableStateOf("") }
    var improvementGoal by remember { mutableStateOf("") }

    val ratings = listOf("Great", "Good", "Needs Discipline", "Overspent")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Daily Money Accountability",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "1. How was your spending today?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ratings.forEach { r ->
                        FilterChip(
                            selected = rating == r,
                            onClick = { rating = r },
                            label = { Text(r, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2. Did you stay within budget?", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = stayedInBudget, onCheckedChange = { stayedInBudget = it })
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("3. Was today's spending planned?", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = plannedSpending, onCheckedChange = { plannedSpending = it })
                }

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = reflectionNotes,
                    onValueChange = { reflectionNotes = it },
                    label = { Text("What was your biggest expense or trigger?") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = improvementGoal,
                    onValueChange = { improvementGoal = it },
                    label = { Text("What can you improve tomorrow?") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(rating, stayedInBudget, plannedSpending, reflectionNotes, improvementGoal)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_checkin_button")
            ) {
                Text("Complete Check-In")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Later") }
        }
    )
}
