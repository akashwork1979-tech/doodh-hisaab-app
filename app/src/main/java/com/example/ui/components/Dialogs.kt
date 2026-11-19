package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RateEditorDialog(
    currentRate: Double,
    currentMonthLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (newRate: Double, applyToCurrentMonth: Boolean) -> Unit
) {
    var rateText by remember(currentRate) {
        mutableStateOf(DateUtils.formatLiters(currentRate))
    }
    var applyToMonth by remember { mutableStateOf(true) }
    val presets = listOf(50.0, 55.0, 60.0, 65.0, 70.0, 75.0, 80.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Milk Rate • दूध की दर (₹/L)",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Auto-saved locally for instant calculation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = rateText.toDoubleOrNull() == preset,
                            onClick = { rateText = DateUtils.formatLiters(preset) },
                            label = { Text("₹${preset.toInt()}") }
                        )
                    }
                }

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { raw ->
                        if (raw.isEmpty() || raw.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            rateText = raw
                        }
                    },
                    label = { Text("Per Liter Rate (₹) / प्रति लीटर दर") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    suffix = { Text("/ L") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rate_dialog_input")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = applyToMonth,
                        onCheckedChange = { applyToMonth = it }
                    )
                    Text(
                        text = "Apply to $currentMonthLabel • इस महीने पर लागू करें",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = rateText.toDoubleOrNull()
                    if (parsed != null && parsed > 0.0) {
                        onConfirm(parsed, applyToMonth)
                    }
                },
                modifier = Modifier.testTag("rate_dialog_save")
            ) {
                Text("Save • सेव करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MilkBillCalculatorDialog(
    defaultRate: Double,
    onDismiss: () -> Unit
) {
    var litersPerDay by remember { mutableStateOf("1.5") }
    var daysText by remember { mutableStateOf("30") }
    var rateText by remember { mutableStateOf(DateUtils.formatLiters(defaultRate)) }

    val liters = litersPerDay.toDoubleOrNull() ?: 0.0
    val days = daysText.toDoubleOrNull() ?: 0.0
    val rate = rateText.toDoubleOrNull() ?: 0.0
    val totalLiters = liters * days
    val totalAmount = totalLiters * rate

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Milk Bill Calculator • दूध कैलकुलेटर",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = litersPerDay,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) litersPerDay = it },
                    label = { Text("Daily Liters / रोज़ का दूध (L)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,1}$"))) daysText = it },
                    label = { Text("Number of Days / कुल दिन") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) rateText = it },
                    label = { Text("Rate per Liter / दर (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Total: ${DateUtils.formatLiters(totalLiters)} L",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = DateUtils.formatRupees(totalAmount),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done • ठीक है")
            }
        }
    )
}
