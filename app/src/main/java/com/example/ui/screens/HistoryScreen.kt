package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.MilkEntryEntity
import com.example.data.MilkSessionType
import com.example.ui.MilkHisabUiState
import com.example.ui.MonthSummaryUi
import com.example.ui.theme.LocalDairyColors
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: MilkHisabUiState,
    onSelectMonth: (String) -> Unit,
    onTogglePaid: (String) -> Unit,
    onOpenMonthInCalendar: (String) -> Unit,
    onEditEntry: (String) -> Unit,
    onSaveCalendarReminder: (MonthSummaryUi) -> Unit,
    onExportJsonClick: () -> Unit,
    onImportJsonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dairyColors = LocalDairyColors.current
    val lang = uiState.settings.languageMode
    val selectedSummary = uiState.allMonthSummaries.firstOrNull {
        it.monthKey == uiState.selectedHistoryMonthKey
    } ?: uiState.currentMonthSummary

    var dropdownExpanded by remember { mutableStateOf(false) }
    val maxLitersInMonth = remember(uiState.selectedHistoryEntries) {
        (uiState.selectedHistoryEntries.maxOfOrNull { it.totalLiters } ?: 1.0).coerceAtLeast(1.0)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = when (lang) {
                        "ENGLISH" -> "Monthly History & Ledger"
                        "HINDI" -> "पिछला हिसाब और मासिक चार्ट"
                        else -> "History • पिछला हिसाब"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Permanent offline month-wise records • हर महीने का पक्का रिकॉर्ड",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Unpaid Balance & Tracked Months KPI Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = if (uiState.totalUnpaidAmount > 0.0) dairyColors.morningContainer else dairyColors.deliveredContainer
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (lang == "HINDI") "कुल बाकी राशि" else "Total Unpaid / बाकी",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.totalUnpaidAmount > 0.0) dairyColors.morningAccent else dairyColors.deliveredAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = DateUtils.formatRupees(uiState.totalUnpaidAmount),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (uiState.totalUnpaidAmount > 0.0) dairyColors.morningAccent else dairyColors.deliveredAccent
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (lang == "HINDI") "दर्ज महीने" else "Tracked Months",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.allMonthSummaries.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // JSON Backup & Restore Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "JSON Backup & Restore • डेटा बैकअप",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Export a JSON file anytime so your milk ledger is never lost, or restore an existing backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onExportJsonClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = onImportJsonClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("import_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Month Dropdown Selector
        item {
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = DateUtils.monthLabelBilingual(selectedSummary.monthKey, lang),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Month • महीना चुनें") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("history_month_dropdown")
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    uiState.allMonthSummaries.forEach { monthItem ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(DateUtils.monthLabelBilingual(monthItem.monthKey, lang))
                                    Text(
                                        text = DateUtils.formatRupees(monthItem.totalAmount),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            onClick = {
                                onSelectMonth(monthItem.monthKey)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Selected Month Bill Summary Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedSummary.monthLabel,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selectedSummary.monthHindiLabel} • ${selectedSummary.monthKey}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            onClick = { onTogglePaid(selectedSummary.monthKey) },
                            shape = RoundedCornerShape(50),
                            color = if (selectedSummary.isPaid) dairyColors.deliveredContainer else dairyColors.absentContainer,
                            modifier = Modifier.testTag("history_paid_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedSummary.isPaid) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                                    contentDescription = "Toggle paid status",
                                    tint = if (selectedSummary.isPaid) dairyColors.deliveredAccent else dairyColors.absentAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (selectedSummary.isPaid) "Paid • जमा" else "Unpaid • बाकी",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selectedSummary.isPaid) dairyColors.deliveredAccent else dairyColors.absentAccent
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryStatColumn("Days / दिन", "${selectedSummary.deliveredDays}")
                        SummaryStatColumn("Liters / ली.", "${DateUtils.formatLiters(selectedSummary.totalLiters)} L")
                        SummaryStatColumn("Rate / दर", "${DateUtils.formatRupees(selectedSummary.ratePerLiter)}/L")
                        SummaryStatColumn(
                            label = "Bill / बिल",
                            value = DateUtils.formatRupees(selectedSummary.totalAmount),
                            highlight = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onOpenMonthInCalendar(selectedSummary.monthKey) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Calendar", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = { onSaveCalendarReminder(selectedSummary) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bill Reminder", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Complete Date-wise Chart Heading
        item {
            Text(
                text = "Date-wise Chart • तारीख़ अनुसार विवरण (${uiState.selectedHistoryEntries.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (uiState.selectedHistoryEntries.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No entries recorded for ${selectedSummary.monthLabel}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "इस महीने कोई एंट्री नहीं है। कैलेंडर में तारीख़ चुनकर दूध दर्ज करें।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.selectedHistoryEntries, key = { it.dateIso }) { entry ->
                DateWiseChartRow(
                    entry = entry,
                    ratePerLiter = selectedSummary.ratePerLiter,
                    maxLiters = maxLitersInMonth,
                    onClick = { onEditEntry(entry.dateIso) }
                )
            }
        }

        // All Months Overview List
        if (uiState.allMonthSummaries.size > 1) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "All Months • सभी महीनों का सार",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(uiState.allMonthSummaries, key = { it.monthKey }) { mSummary ->
                MonthOverviewTile(
                    summary = mSummary,
                    isSelected = mSummary.monthKey == selectedSummary.monthKey,
                    onSelect = { onSelectMonth(mSummary.monthKey) },
                    onTogglePaid = { onTogglePaid(mSummary.monthKey) }
                )
            }
        }
    }
}

@Composable
private fun SummaryStatColumn(label: String, value: String, highlight: Boolean = false) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DateWiseChartRow(
    entry: MilkEntryEntity,
    ratePerLiter: Double,
    maxLiters: Double,
    onClick: () -> Unit
) {
    val dairyColors = LocalDairyColors.current
    val session = MilkSessionType.fromString(entry.sessionTag)
    val isAbsent = session == MilkSessionType.ABSENT || entry.totalLiters <= 0.0
    val barFraction = if (isAbsent) 0f else (entry.totalLiters / maxLiters).toFloat().coerceIn(0.08f, 1f)
    val dayAmount = entry.totalLiters * ratePerLiter

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("history_entry_${entry.dateIso}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = DateUtils.formatShortDay(entry.dateIso),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (session) {
                            MilkSessionType.MORNING -> dairyColors.morningContainer
                            MilkSessionType.EVENING -> dairyColors.eveningContainer
                            MilkSessionType.BOTH -> MaterialTheme.colorScheme.primaryContainer
                            MilkSessionType.ABSENT -> dairyColors.absentContainer
                        }
                    ) {
                        Text(
                            text = when (session) {
                                MilkSessionType.MORNING -> "Morning / सुबह"
                                MilkSessionType.EVENING -> "Evening / शाम"
                                MilkSessionType.BOTH -> "AM ${DateUtils.formatLiters(entry.morningLiters)} + PM ${DateUtils.formatLiters(entry.eveningLiters)}"
                                MilkSessionType.ABSENT -> "Absent / नहीं"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (session) {
                                MilkSessionType.MORNING -> dairyColors.morningAccent
                                MilkSessionType.EVENING -> dairyColors.eveningAccent
                                MilkSessionType.BOTH -> MaterialTheme.colorScheme.primary
                                MilkSessionType.ABSENT -> dairyColors.absentAccent
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${DateUtils.formatLiters(entry.totalLiters)} L",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isAbsent) dairyColors.absentAccent else dairyColors.deliveredAccent
                    )
                    Text(
                        text = DateUtils.formatRupees(dayAmount),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Proportional visual chart bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (barFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(barFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(dairyColors.deliveredAccent)
                    )
                }
            }

            if (entry.note.isNotBlank()) {
                Text(
                    text = "Note: ${entry.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MonthOverviewTile(
    summary: MonthSummaryUi,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTogglePaid: () -> Unit
) {
    val dairyColors = LocalDairyColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${summary.monthLabel} • ${summary.monthHindiLabel}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${summary.deliveredDays} days • ${DateUtils.formatLiters(summary.totalLiters)} L @ ${DateUtils.formatRupees(summary.ratePerLiter)}/L",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = DateUtils.formatRupees(summary.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    onClick = onTogglePaid,
                    shape = RoundedCornerShape(50),
                    color = if (summary.isPaid) dairyColors.deliveredContainer else dairyColors.absentContainer
                ) {
                    Text(
                        text = if (summary.isPaid) "Paid" else "Unpaid",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.isPaid) dairyColors.deliveredAccent else dairyColors.absentAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}
