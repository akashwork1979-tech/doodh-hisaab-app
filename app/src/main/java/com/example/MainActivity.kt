package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MilkDatabase
import com.example.data.MilkRepository
import com.example.ui.MilkHisabViewModel
import com.example.ui.MonthSummaryUi
import com.example.ui.components.DailyEntryBottomSheet
import com.example.ui.components.MilkBillCalculatorDialog
import com.example.ui.components.RateEditorDialog
import com.example.ui.screens.AppsAndSettingsScreen
import com.example.ui.screens.HisabHomeScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.theme.DoodhHisabTheme
import com.example.util.DateUtils
import com.example.util.ExternalAppLauncher
import com.example.util.GoogleQuickApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class AppTab(
    val labelEn: String,
    val labelHi: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HISAB("Hisab", "हिसाब", Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth),
    HISTORY("History", "पिछला हिसाब", Icons.Rounded.History, Icons.Outlined.History),
    APPS("Apps", "ऐप्स", Icons.Rounded.Apps, Icons.Outlined.Apps)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = MilkDatabase.getInstance(applicationContext)
        val repository = MilkRepository(database.milkDao())

        setContent {
            DoodhHisabTheme {
                val vm: MilkHisabViewModel = viewModel(factory = MilkHisabViewModel.factory(repository))
                DoodhHisabApp(viewModel = vm)
            }
        }
    }
}

@Composable
private fun DoodhHisabApp(viewModel: MilkHisabViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeTab by rememberSaveable { mutableStateOf(AppTab.HISAB) }

    // Return to Home Hisab tab on back press from secondary tabs
    BackHandler(enabled = activeTab != AppTab.HISAB) {
        activeTab = AppTab.HISAB
    }

    // SAF Export JSON Backup launcher (zero storage permissions needed)
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = viewModel.generateBackupJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(json.toByteArray(Charsets.UTF_8))
                        } ?: throw IllegalStateException("Cannot open output stream")
                    }
                }.onSuccess {
                    viewModel.showSnackbar("Backup JSON saved • बैकअप सेव हो गया")
                }.onFailure {
                    viewModel.showSnackbar("Export failed • बैकअप सेव नहीं हो सका")
                }
            }
        }
    }

    // SAF Import JSON Backup launcher (zero storage permissions needed)
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                            it.readText()
                        } ?: throw IllegalStateException("Cannot read file")
                    }
                }.onSuccess { rawJson ->
                    viewModel.importBackupJson(rawJson)
                }.onFailure {
                    viewModel.showSnackbar("Could not open JSON file • फ़ाइल नहीं खुली")
                }
            }
        }
    }

    // Snackbar observer
    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val handleQuickApp: (GoogleQuickApp) -> Unit = { app ->
        val launched = ExternalAppLauncher.launchQuickApp(context, app)
        if (!launched) {
            if (app == GoogleQuickApp.CALCULATOR) {
                viewModel.openCalculatorDialog()
            } else {
                viewModel.showSnackbar("Could not open ${app.titleEn} on this device")
            }
        }
    }

    val handleBillReminder: (MonthSummaryUi) -> Unit = { summary ->
        val opened = ExternalAppLauncher.openBillReminderInGoogleCalendar(
            context = context,
            monthLabel = summary.monthLabel,
            vendorName = uiState.settings.dairyVendorName,
            deliveredDays = summary.deliveredDays,
            totalLiters = summary.totalLiters,
            ratePerLiter = summary.ratePerLiter,
            totalAmount = summary.totalAmount,
            isPaid = summary.isPaid
        )
        if (!opened) {
            viewModel.showSnackbar("No browser or Calendar app found on device")
        }
    }

    val lang = uiState.settings.languageMode

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (activeTab == AppTab.HISAB) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openDateEntry(uiState.todayIso) },
                        icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                        text = {
                            Text(
                                text = when (lang) {
                                    "ENGLISH" -> "Log Today"
                                    "HINDI" -> "आज दर्ज करें"
                                    else -> "Log Today • आज"
                                }
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_log_today")
                    )
                }
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.labelEn
                                    )
                                },
                                label = {
                                    Text(
                                        text = when (lang) {
                                            "ENGLISH" -> tab.labelEn
                                            "HINDI" -> tab.labelHi
                                            else -> "${tab.labelEn} • ${tab.labelHi}"
                                        },
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .statusBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.labelEn
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (lang == "HINDI") tab.labelHi else tab.labelEn,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxSize()

                    when (activeTab) {
                        AppTab.HISAB -> {
                            HisabHomeScreen(
                                uiState = uiState,
                                onPreviousMonth = viewModel::previousMonth,
                                onNextMonth = viewModel::nextMonth,
                                onJumpToToday = viewModel::jumpToToday,
                                onDayClick = viewModel::openDateEntry,
                                onEditRateClick = viewModel::openRateDialog,
                                onTogglePaidClick = { viewModel.toggleMonthPaid(uiState.currentMonthKey) },
                                onSaveCalendarReminder = handleBillReminder,
                                onQuickAppClick = handleQuickApp,
                                modifier = contentModifier
                            )
                        }

                        AppTab.HISTORY -> {
                            HistoryScreen(
                                uiState = uiState,
                                onSelectMonth = viewModel::selectHistoryMonth,
                                onTogglePaid = viewModel::toggleMonthPaid,
                                onOpenMonthInCalendar = { monthKey ->
                                    viewModel.jumpToMonth(monthKey)
                                    activeTab = AppTab.HISAB
                                },
                                onEditEntry = viewModel::openDateEntry,
                                onSaveCalendarReminder = handleBillReminder,
                                onExportJsonClick = {
                                    exportBackupLauncher.launch(DateUtils.backupFileName())
                                },
                                onImportJsonClick = {
                                    importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                },
                                modifier = contentModifier
                            )
                        }

                        AppTab.APPS -> {
                            AppsAndSettingsScreen(
                                uiState = uiState,
                                onEditRateClick = viewModel::openRateDialog,
                                onQuickSelectRate = { rate ->
                                    viewModel.updateRate(rate, applyToCurrentMonth = true)
                                },
                                onUpdateVendorName = viewModel::updateVendorName,
                                onUpdateLanguageMode = viewModel::updateLanguageMode,
                                onSaveCalendarReminder = handleBillReminder,
                                onQuickAppClick = handleQuickApp,
                                onExportJsonClick = {
                                    exportBackupLauncher.launch(DateUtils.backupFileName())
                                },
                                onImportJsonClick = {
                                    importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                },
                                modifier = contentModifier
                            )
                        }
                    }
                }
            }
        }
    }

    // Daily Entry Modal Bottom Sheet
    val editingDate = uiState.editingDateIso
    if (editingDate != null) {
        val editingMonthKey = editingDate.take(7)
        val rateForEditingMonth = uiState.allMonthSummaries
            .firstOrNull { it.monthKey == editingMonthKey }
            ?.ratePerLiter ?: uiState.settings.defaultRatePerLiter

        DailyEntryBottomSheet(
            dateIso = editingDate,
            existingEntry = uiState.editingEntry,
            ratePerLiter = rateForEditingMonth,
            defaultQuantity = uiState.settings.defaultQuantityLiters,
            onDismiss = viewModel::dismissDateEntry,
            onSave = { morning, evening, session, note ->
                viewModel.saveDailyEntry(editingDate, morning, evening, session, note)
            },
            onClear = {
                viewModel.clearDailyEntry(editingDate)
            }
        )
    }

    // Per-Liter Rate Editor Dialog
    if (uiState.showRateDialog) {
        RateEditorDialog(
            currentRate = uiState.currentMonthSummary.ratePerLiter,
            currentMonthLabel = uiState.currentMonthSummary.monthLabel,
            onDismiss = viewModel::dismissRateDialog,
            onConfirm = { newRate, applyToCurrent ->
                viewModel.updateRate(newRate, applyToCurrent)
            }
        )
    }

    // Built-in Offline Milk Calculator Dialog (fallback when device lacks a Calculator app)
    if (uiState.showCalculatorDialog) {
        MilkBillCalculatorDialog(
            defaultRate = uiState.currentMonthSummary.ratePerLiter,
            onDismiss = viewModel::dismissCalculatorDialog
        )
    }
}
