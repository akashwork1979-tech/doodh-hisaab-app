package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MilkSessionType
import com.example.ui.CalendarDayUi
import com.example.ui.MilkHisabUiState
import com.example.ui.MonthSummaryUi
import com.example.ui.components.GoogleAppsQuickBar
import com.example.ui.theme.LocalDairyColors
import com.example.util.DateUtils
import com.example.util.GoogleQuickApp

@Composable
fun HisabHomeScreen(
    uiState: MilkHisabUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    onDayClick: (String) -> Unit,
    onEditRateClick: () -> Unit,
    onTogglePaidClick: () -> Unit,
    onSaveCalendarReminder: (MonthSummaryUi) -> Unit,
    onQuickAppClick: (GoogleQuickApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = uiState.currentMonthSummary
    val isViewingCurrentMonth = uiState.currentMonthKey == DateUtils.currentMonthKey()
    val lang = uiState.settings.languageMode

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Bilingual Header + Rate Quick Chip
        item {
            TopHeaderRow(
                vendorName = uiState.settings.dairyVendorName,
                ratePerLiter = summary.ratePerLiter,
                languageMode = lang,
                showTodayButton = !isViewingCurrentMonth,
                onJumpToToday = onJumpToToday,
                onEditRateClick = onEditRateClick
            )
        }

        // 2. Hero Monthly Bill Summary Card with Morning Illustration Backdrop
        item {
            MonthlyHeroSummaryCard(
                summary = summary,
                languageMode = lang,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onTogglePaidClick = onTogglePaidClick,
                onEditRateClick = onEditRateClick,
                onSaveCalendarReminder = { onSaveCalendarReminder(summary) }
            )
        }

        // 3. Interactive Monthly Calendar Grid
        item {
            MonthlyCalendarCard(
                calendarDays = uiState.calendarDays,
                languageMode = lang,
                onDayClick = onDayClick
            )
        }

        // 4. Google Apps Quick Launch Bar
        item {
            GoogleAppsQuickBar(
                languageMode = lang,
                onAppClick = onQuickAppClick
            )
        }
    }
}

@Composable
private fun TopHeaderRow(
    vendorName: String,
    ratePerLiter: Double,
    languageMode: String,
    showTodayButton: Boolean,
    onJumpToToday: () -> Unit,
    onEditRateClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (languageMode) {
                    "ENGLISH" -> "Doodh Hisab"
                    "HINDI" -> "दूध का हिसाब"
                    else -> "Doodh Hisab • दूध का हिसाब"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = vendorName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(visible = showTodayButton) {
                Surface(
                    onClick = onJumpToToday,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.testTag("jump_today_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Today,
                            contentDescription = "Jump to today",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (languageMode == "HINDI") "आज" else "Today",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Surface(
                onClick = onEditRateClick,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)),
                modifier = Modifier.testTag("top_rate_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${DateUtils.formatRupees(ratePerLiter)}/L",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.cd_edit_rate),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyHeroSummaryCard(
    summary: MonthSummaryUi,
    languageMode: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTogglePaidClick: () -> Unit,
    onEditRateClick: () -> Unit,
    onSaveCalendarReminder: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val dairyColors = LocalDairyColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = primaryColor,
        tonalElevation = 4.dp,
        shadowElevation = 3.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Editorial dairy morning illustration blended subtly on the right
            Image(
                painter = painterResource(id = R.drawable.img_dairy_morning_banner),
                contentDescription = stringResource(R.string.cd_dairy_banner),
                contentScale = ContentScale.Crop,
                alpha = 0.26f,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.96f),
                                primaryColor.copy(alpha = 0.85f),
                                primaryColor.copy(alpha = 0.62f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Month selector + Paid status pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onPreviousMonth,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("prev_month_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = stringResource(R.string.cd_previous_month),
                                tint = onPrimaryColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = DateUtils.monthLabelBilingual(summary.monthKey, languageMode),
                                style = MaterialTheme.typography.titleMedium,
                                color = onPrimaryColor
                            )
                        }
                        IconButton(
                            onClick = onNextMonth,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("next_month_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.cd_next_month),
                                tint = onPrimaryColor
                            )
                        }
                    }

                    // Paid / Unpaid toggle badge
                    Surface(
                        onClick = onTogglePaidClick,
                        shape = RoundedCornerShape(50),
                        color = if (summary.isPaid) dairyColors.deliveredContainer else dairyColors.morningContainer,
                        modifier = Modifier.testTag("home_paid_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (summary.isPaid) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                                contentDescription = "Toggle paid status",
                                tint = if (summary.isPaid) dairyColors.deliveredAccent else dairyColors.morningAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = when {
                                    summary.isPaid && languageMode == "HINDI" -> "भुगतान हो गया"
                                    summary.isPaid && languageMode == "ENGLISH" -> "Paid"
                                    summary.isPaid -> "Paid • जमा"
                                    languageMode == "HINDI" -> "बाकी है"
                                    languageMode == "ENGLISH" -> "Unpaid"
                                    else -> "Unpaid • बाकी"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.isPaid) dairyColors.deliveredAccent else dairyColors.morningAccent
                            )
                        }
                    }
                }

                // Big Total Amount Display
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = when (languageMode) {
                            "ENGLISH" -> "Total Monthly Bill"
                            "HINDI" -> "मासिक कुल बिल"
                            else -> "Total Monthly Bill • कुल मासिक बिल"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = onPrimaryColor.copy(alpha = 0.82f)
                    )
                    Text(
                        text = DateUtils.formatRupees(summary.totalAmount),
                        style = MaterialTheme.typography.displaySmall,
                        color = onPrimaryColor,
                        modifier = Modifier.testTag("monthly_total_amount")
                    )
                }

                // 3 Metric Tiles: Days, Liters, Rate/L
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroMetricPill(
                        label = if (languageMode == "HINDI") "दिन" else "Days / दिन",
                        value = "${summary.deliveredDays}",
                        subValue = if (summary.absentDays > 0) "${summary.absentDays} absent" else null,
                        onPrimaryColor = onPrimaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    HeroMetricPill(
                        label = if (languageMode == "HINDI") "लीटर" else "Liters / ली.",
                        value = "${DateUtils.formatLiters(summary.totalLiters)} L",
                        subValue = null,
                        onPrimaryColor = onPrimaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    HeroMetricPill(
                        label = if (languageMode == "HINDI") "दर (₹/L)" else "Rate / दर",
                        value = DateUtils.formatRupees(summary.ratePerLiter),
                        subValue = "tap to edit",
                        onPrimaryColor = onPrimaryColor,
                        onClick = onEditRateClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Bill Reminder Action Button
                FilledTonalButton(
                    onClick = onSaveCalendarReminder,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = onPrimaryColor.copy(alpha = 0.16f),
                        contentColor = onPrimaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("home_calendar_reminder_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (languageMode) {
                            "ENGLISH" -> "Save Bill Reminder in Google Calendar"
                            "HINDI" -> "गूगल कैलेंडर में बिल रिमाइंडर सेव करें"
                            else -> "Bill Reminder • कैलेंडर रिमाइंडर सेव करें"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricPill(
    label: String,
    value: String,
    subValue: String?,
    onPrimaryColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick)
            else Modifier
        ),
        shape = RoundedCornerShape(14.dp),
        color = onPrimaryColor.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, onPrimaryColor.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = onPrimaryColor.copy(alpha = 0.80f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = onPrimaryColor,
                maxLines = 1
            )
            if (subValue != null) {
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = onPrimaryColor.copy(alpha = 0.72f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MonthlyCalendarCard(
    calendarDays: List<CalendarDayUi>,
    languageMode: String,
    onDayClick: (String) -> Unit
) {
    val dairyColors = LocalDairyColors.current
    val weekdayHeaders = DateUtils.weekdayHeaders()
    val rows = calendarDays.chunked(7)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when (languageMode) {
                            "ENGLISH" -> "Tap any date to log milk"
                            "HINDI" -> "दूध दर्ज करने के लिए तारीख चुनें"
                            else -> "Tap date to log • तारीख चुनें"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Weekday Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weekdayHeaders.forEachIndexed { idx, (en, hi) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (languageMode == "HINDI") hi else en,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (idx == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (languageMode == "BILINGUAL") {
                            Text(
                                text = hi,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
            }

            // Calendar Grid Rows
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rows.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        week.forEach { cell ->
                            CalendarDayTile(
                                cell = cell,
                                onDayClick = onDayClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Compact Legend Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = dairyColors.deliveredAccent, label = "Delivered / आया")
                LegendItem(color = dairyColors.absentAccent, label = "0L Absent")
                LegendItem(color = dairyColors.morningAccent, label = "AM सुबह")
                LegendItem(color = dairyColors.eveningAccent, label = "PM शाम")
            }
        }
    }
}

@Composable
private fun CalendarDayTile(
    cell: CalendarDayUi,
    onDayClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dairyColors = LocalDairyColors.current
    val dayNumber = cell.dayOfMonth
    val dateIso = cell.dateIso

    if (dayNumber == null || dateIso == null) {
        Box(modifier = modifier.height(58.dp))
        return
    }

    val entry = cell.entry
    val session = entry?.let { MilkSessionType.fromString(it.sessionTag) }
    val isDelivered = entry != null && entry.totalLiters > 0.0 && session != MilkSessionType.ABSENT
    val isAbsent = entry != null && (entry.totalLiters <= 0.0 || session == MilkSessionType.ABSENT)

    val tileBackground = when {
        isDelivered -> dairyColors.deliveredContainer.copy(alpha = 0.45f)
        isAbsent -> dairyColors.absentContainer.copy(alpha = 0.50f)
        cell.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val borderModifier = if (cell.isToday) {
        Modifier.border(
            width = 1.8.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(12.dp)
        )
    } else {
        Modifier.border(
            width = 0.6.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        )
    }

    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tileBackground)
            .then(borderModifier)
            .clickable { onDayClick(dateIso) }
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("calendar_day_$dateIso"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.SemiBold,
                color = if (cell.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            // Morning / Evening indicator dots
            if (isDelivered && entry != null) {
                if (entry.morningLiters > 0.0) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dairyColors.morningAccent)
                    )
                }
                if (entry.eveningLiters > 0.0) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dairyColors.eveningAccent)
                    )
                }
            }
        }

        // Badge
        when {
            isDelivered && entry != null -> {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = dairyColors.deliveredAccent
                ) {
                    Text(
                        text = "${DateUtils.formatLiters(entry.totalLiters)}L",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            isAbsent -> {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = dairyColors.absentAccent
                ) {
                    Text(
                        text = "0L",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
            else -> {
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
