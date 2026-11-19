package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.MilkEntryEntity
import com.example.data.MilkSessionType
import com.example.ui.theme.LocalDairyColors
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DailyEntryBottomSheet(
    dateIso: String,
    existingEntry: MilkEntryEntity?,
    ratePerLiter: Double,
    defaultQuantity: Double,
    onDismiss: () -> Unit,
    onSave: (morningLiters: Double, eveningLiters: Double, sessionType: MilkSessionType, note: String) -> Unit,
    onClear: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dairyColors = LocalDairyColors.current

    val initialSession = existingEntry?.let { MilkSessionType.fromString(it.sessionTag) }
        ?: MilkSessionType.MORNING

    var selectedSession by remember(dateIso) { mutableStateOf(initialSession) }
    var morningText by remember(dateIso) {
        mutableStateOf(
            when {
                existingEntry != null -> DateUtils.formatLiters(existingEntry.morningLiters)
                else -> DateUtils.formatLiters(defaultQuantity)
            }
        )
    }
    var eveningText by remember(dateIso) {
        mutableStateOf(
            when {
                existingEntry != null -> DateUtils.formatLiters(existingEntry.eveningLiters)
                else -> "0"
            }
        )
    }
    var singleQtyText by remember(dateIso) {
        mutableStateOf(
            when {
                existingEntry != null && initialSession != MilkSessionType.ABSENT ->
                    DateUtils.formatLiters(existingEntry.totalLiters)
                existingEntry != null && initialSession == MilkSessionType.ABSENT -> "0"
                else -> DateUtils.formatLiters(defaultQuantity)
            }
        )
    }
    var noteText by remember(dateIso) { mutableStateOf(existingEntry?.note ?: "") }

    val computedMorning: Double
    val computedEvening: Double
    when (selectedSession) {
        MilkSessionType.ABSENT -> {
            computedMorning = 0.0
            computedEvening = 0.0
        }
        MilkSessionType.MORNING -> {
            computedMorning = singleQtyText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
            computedEvening = 0.0
        }
        MilkSessionType.EVENING -> {
            computedMorning = 0.0
            computedEvening = singleQtyText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        }
        MilkSessionType.BOTH -> {
            computedMorning = morningText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
            computedEvening = eveningText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        }
    }
    val totalLiters = computedMorning + computedEvening
    val estimatedBill = totalLiters * ratePerLiter

    val presets = listOf(0.5, 1.0, 1.5, 2.0, 2.5, 3.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Milk Entry • दूध दर्ज करें",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = DateUtils.formatReadableDate(dateIso),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Session Tag Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Delivery Time / समय चुनें",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SessionChip(
                        selected = selectedSession == MilkSessionType.MORNING,
                        label = "Morning / सुबह",
                        icon = Icons.Rounded.WbSunny,
                        selectedContainer = dairyColors.morningContainer,
                        selectedContent = dairyColors.morningAccent,
                        testTag = "session_morning",
                        onClick = {
                            if (selectedSession == MilkSessionType.ABSENT && (singleQtyText.toDoubleOrNull() ?: 0.0) <= 0.0) {
                                singleQtyText = DateUtils.formatLiters(defaultQuantity)
                            }
                            selectedSession = MilkSessionType.MORNING
                        }
                    )
                    SessionChip(
                        selected = selectedSession == MilkSessionType.EVENING,
                        label = "Evening / शाम",
                        icon = Icons.Rounded.NightsStay,
                        selectedContainer = dairyColors.eveningContainer,
                        selectedContent = dairyColors.eveningAccent,
                        testTag = "session_evening",
                        onClick = {
                            if (selectedSession == MilkSessionType.ABSENT && (singleQtyText.toDoubleOrNull() ?: 0.0) <= 0.0) {
                                singleQtyText = DateUtils.formatLiters(defaultQuantity)
                            }
                            selectedSession = MilkSessionType.EVENING
                        }
                    )
                    SessionChip(
                        selected = selectedSession == MilkSessionType.BOTH,
                        label = "Both / दोनों",
                        icon = Icons.Rounded.WbTwilight,
                        selectedContainer = MaterialTheme.colorScheme.primaryContainer,
                        selectedContent = MaterialTheme.colorScheme.primary,
                        testTag = "session_both",
                        onClick = {
                            if ((morningText.toDoubleOrNull() ?: 0.0) == 0.0 && (eveningText.toDoubleOrNull() ?: 0.0) == 0.0) {
                                morningText = "1"
                                eveningText = "0.5"
                            }
                            selectedSession = MilkSessionType.BOTH
                        }
                    )
                    SessionChip(
                        selected = selectedSession == MilkSessionType.ABSENT,
                        label = "Absent (0L) / नहीं",
                        icon = Icons.Rounded.Block,
                        selectedContainer = dairyColors.absentContainer,
                        selectedContent = dairyColors.absentAccent,
                        testTag = "session_absent",
                        onClick = {
                            selectedSession = MilkSessionType.ABSENT
                        }
                    )
                }
            }

            // Quantity input area
            if (selectedSession == MilkSessionType.BOTH) {
                QuantityStepperRow(
                    label = "Morning / सुबह (L)",
                    valueText = morningText,
                    onValueChange = { morningText = it },
                    testTag = "morning_qty_input"
                )
                QuantityStepperRow(
                    label = "Evening / शाम (L)",
                    valueText = eveningText,
                    onValueChange = { eveningText = it },
                    testTag = "evening_qty_input"
                )
            } else if (selectedSession != MilkSessionType.ABSENT) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Quick Select Quantity / मात्रा चुनें",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            val isSelected = singleQtyText.toDoubleOrNull() == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = { singleQtyText = DateUtils.formatLiters(preset) },
                                label = {
                                    Text(
                                        text = "${DateUtils.formatLiters(preset)} L",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = dairyColors.deliveredContainer,
                                    selectedLabelColor = dairyColors.deliveredAccent,
                                    selectedLeadingIconColor = dairyColors.deliveredAccent
                                ),
                                modifier = Modifier.testTag("preset_${DateUtils.formatLiters(preset)}")
                            )
                        }
                    }
                    QuantityStepperRow(
                        label = "Custom Quantity / कस्टम लीटर",
                        valueText = singleQtyText,
                        onValueChange = { singleQtyText = it },
                        testTag = "custom_qty_input"
                    )
                }
            }

            // Live Cost Preview
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (totalLiters > 0.0) dairyColors.deliveredContainer else dairyColors.absentContainer,
                border = BorderStroke(
                    1.dp,
                    if (totalLiters > 0.0) dairyColors.deliveredAccent.copy(alpha = 0.25f)
                    else dairyColors.absentAccent.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (totalLiters > 0.0) "Day Total • आज का हिसाब" else "Marked Absent • दूध नहीं आया",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (totalLiters > 0.0) dairyColors.deliveredAccent else dairyColors.absentAccent
                        )
                        Text(
                            text = "${DateUtils.formatLiters(totalLiters)} L × ${DateUtils.formatRupees(ratePerLiter)}/L",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = DateUtils.formatRupees(estimatedBill),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (totalLiters > 0.0) dairyColors.deliveredAccent else dairyColors.absentAccent
                    )
                }
            }

            // Optional Note
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note (optional) • टिप्पणी (वैकल्पिक)") },
                placeholder = { Text("e.g. Extra for kheer / खीर के लिए अतिरिक्त") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_note_input")
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (existingEntry != null) {
                    OutlinedButton(
                        onClick = onClear,
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("clear_entry_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Clear entry",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear")
                    }
                }
                Button(
                    onClick = {
                        onSave(computedMorning, computedEvening, selectedSession, noteText)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("save_entry_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save • सेव करें",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionChip(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedContainer: androidx.compose.ui.graphics.Color,
    selectedContent: androidx.compose.ui.graphics.Color,
    testTag: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectedContainer,
            selectedLabelColor = selectedContent,
            selectedLeadingIconColor = selectedContent
        ),
        modifier = Modifier.testTag(testTag)
    )
}

@Composable
private fun QuantityStepperRow(
    label: String,
    valueText: String,
    onValueChange: (String) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilledTonalIconButton(
            onClick = {
                val next = ((valueText.toDoubleOrNull() ?: 0.0) - 0.25).coerceAtLeast(0.0)
                onValueChange(DateUtils.formatLiters(next))
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Rounded.Remove, contentDescription = "Decrease quantity")
        }

        OutlinedTextField(
            value = valueText,
            onValueChange = { raw ->
                if (raw.isEmpty() || raw.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    onValueChange(raw)
                }
            },
            label = { Text(label) },
            suffix = { Text("L", fontWeight = FontWeight.Bold) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .weight(1f)
                .testTag(testTag)
        )

        FilledTonalIconButton(
            onClick = {
                val next = ((valueText.toDoubleOrNull() ?: 0.0) + 0.25).coerceAtMost(50.0)
                onValueChange(DateUtils.formatLiters(next))
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Increase quantity")
        }
    }
}
