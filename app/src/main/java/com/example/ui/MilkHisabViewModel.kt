package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppSettingsEntity
import com.example.data.MilkEntryEntity
import com.example.data.MilkRepository
import com.example.data.MilkSessionType
import com.example.data.MonthRecordEntity
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarDayUi(
    val dayOfMonth: Int?,
    val dateIso: String?,
    val entry: MilkEntryEntity?,
    val isToday: Boolean
)

data class MonthSummaryUi(
    val monthKey: String,
    val monthLabel: String,
    val monthHindiLabel: String,
    val deliveredDays: Int,
    val absentDays: Int,
    val totalLiters: Double,
    val morningLiters: Double,
    val eveningLiters: Double,
    val ratePerLiter: Double,
    val totalAmount: Double,
    val isPaid: Boolean,
    val entriesCount: Int
)

data class MilkHisabUiState(
    val currentMonthKey: String = DateUtils.currentMonthKey(),
    val todayIso: String = DateUtils.todayIso(),
    val settings: AppSettingsEntity = AppSettingsEntity(),
    val calendarDays: List<CalendarDayUi> = emptyList(),
    val currentMonthSummary: MonthSummaryUi = emptySummary(DateUtils.currentMonthKey(), 60.0),
    val allMonthSummaries: List<MonthSummaryUi> = emptyList(),
    val selectedHistoryMonthKey: String = DateUtils.currentMonthKey(),
    val selectedHistoryEntries: List<MilkEntryEntity> = emptyList(),
    val totalUnpaidAmount: Double = 0.0,
    val editingDateIso: String? = null,
    val editingEntry: MilkEntryEntity? = null,
    val showRateDialog: Boolean = false,
    val showCalculatorDialog: Boolean = false,
    val snackbarMessage: String? = null
)

private fun emptySummary(monthKey: String, rate: Double): MonthSummaryUi {
    return MonthSummaryUi(
        monthKey = monthKey,
        monthLabel = DateUtils.monthLabelEnglish(monthKey),
        monthHindiLabel = DateUtils.monthLabelHindi(monthKey),
        deliveredDays = 0,
        absentDays = 0,
        totalLiters = 0.0,
        morningLiters = 0.0,
        eveningLiters = 0.0,
        ratePerLiter = rate,
        totalAmount = 0.0,
        isPaid = false,
        entriesCount = 0
    )
}

private data class UiControlState(
    val currentMonthKey: String = DateUtils.currentMonthKey(),
    val selectedHistoryMonthKey: String = DateUtils.currentMonthKey(),
    val editingDateIso: String? = null,
    val showRateDialog: Boolean = false,
    val showCalculatorDialog: Boolean = false,
    val snackbarMessage: String? = null
)

class MilkHisabViewModel(
    private val repository: MilkRepository
) : ViewModel() {

    private val controlState = MutableStateFlow(UiControlState())

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    val uiState: StateFlow<MilkHisabUiState> = combine(
        repository.settingsFlow,
        repository.allEntriesFlow,
        repository.allMonthRecordsFlow,
        controlState
    ) { settings, allEntries, allMonthRecords, ctrl ->
        val todayIso = DateUtils.todayIso()
        val recordMap = allMonthRecords.associateBy { it.monthKey }
        val entriesByMonth = allEntries.groupBy { it.monthKey }

        val knownMonths = buildSet {
            add(DateUtils.currentMonthKey())
            add(ctrl.currentMonthKey)
            addAll(entriesByMonth.keys)
            addAll(recordMap.keys)
        }.sortedDescending()

        val summaries = knownMonths.map { mKey ->
            buildMonthSummary(
                monthKey = mKey,
                entries = entriesByMonth[mKey].orEmpty(),
                record = recordMap[mKey],
                defaultRate = settings.defaultRatePerLiter
            )
        }

        val currentSummary = summaries.firstOrNull { it.monthKey == ctrl.currentMonthKey }
            ?: emptySummary(ctrl.currentMonthKey, settings.defaultRatePerLiter)

        val currentMonthEntries = entriesByMonth[ctrl.currentMonthKey].orEmpty().associateBy { it.dateIso }
        val calendarCells = buildCalendarGrid(
            monthKey = ctrl.currentMonthKey,
            entriesByDate = currentMonthEntries,
            todayIso = todayIso
        )

        val effectiveHistoryKey = if (ctrl.selectedHistoryMonthKey in knownMonths) {
            ctrl.selectedHistoryMonthKey
        } else {
            knownMonths.first()
        }
        val historyEntries = entriesByMonth[effectiveHistoryKey].orEmpty().sortedBy { it.dateIso }

        val unpaidSum = summaries
            .filter { !it.isPaid && it.totalAmount > 0.0 }
            .sumOf { it.totalAmount }

        val editingEntry = ctrl.editingDateIso?.let { iso ->
            allEntries.firstOrNull { it.dateIso == iso }
        }

        MilkHisabUiState(
            currentMonthKey = ctrl.currentMonthKey,
            todayIso = todayIso,
            settings = settings,
            calendarDays = calendarCells,
            currentMonthSummary = currentSummary,
            allMonthSummaries = summaries,
            selectedHistoryMonthKey = effectiveHistoryKey,
            selectedHistoryEntries = historyEntries,
            totalUnpaidAmount = unpaidSum,
            editingDateIso = ctrl.editingDateIso,
            editingEntry = editingEntry,
            showRateDialog = ctrl.showRateDialog,
            showCalculatorDialog = ctrl.showCalculatorDialog,
            snackbarMessage = ctrl.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MilkHisabUiState()
    )

    private fun buildMonthSummary(
        monthKey: String,
        entries: List<MilkEntryEntity>,
        record: MonthRecordEntity?,
        defaultRate: Double
    ): MonthSummaryUi {
        val rate = record?.ratePerLiter ?: defaultRate
        val deliveredDays = entries.count { it.totalLiters > 0.0 && it.sessionTag != MilkSessionType.ABSENT.name }
        val absentDays = entries.count { it.totalLiters <= 0.0 || it.sessionTag == MilkSessionType.ABSENT.name }
        val totalLiters = entries.sumOf { it.totalLiters }
        val morningLiters = entries.sumOf { it.morningLiters }
        val eveningLiters = entries.sumOf { it.eveningLiters }
        return MonthSummaryUi(
            monthKey = monthKey,
            monthLabel = DateUtils.monthLabelEnglish(monthKey),
            monthHindiLabel = DateUtils.monthLabelHindi(monthKey),
            deliveredDays = deliveredDays,
            absentDays = absentDays,
            totalLiters = totalLiters,
            morningLiters = morningLiters,
            eveningLiters = eveningLiters,
            ratePerLiter = rate,
            totalAmount = totalLiters * rate,
            isPaid = record?.isPaid ?: false,
            entriesCount = entries.size
        )
    }

    private fun buildCalendarGrid(
        monthKey: String,
        entriesByDate: Map<String, MilkEntryEntity>,
        todayIso: String
    ): List<CalendarDayUi> {
        val (year, month) = DateUtils.parseMonthKey(monthKey)
        val offset = DateUtils.firstDayOfWeekOffset(monthKey)
        val totalDays = DateUtils.daysInMonth(monthKey)
        val cells = mutableListOf<CalendarDayUi>()
        repeat(offset) {
            cells.add(CalendarDayUi(dayOfMonth = null, dateIso = null, entry = null, isToday = false))
        }
        for (day in 1..totalDays) {
            val iso = DateUtils.formatIso(year, month, day)
            cells.add(
                CalendarDayUi(
                    dayOfMonth = day,
                    dateIso = iso,
                    entry = entriesByDate[iso],
                    isToday = iso == todayIso
                )
            )
        }
        while (cells.size % 7 != 0) {
            cells.add(CalendarDayUi(dayOfMonth = null, dateIso = null, entry = null, isToday = false))
        }
        return cells
    }

    fun previousMonth() {
        controlState.value = controlState.value.copy(
            currentMonthKey = DateUtils.shiftMonth(controlState.value.currentMonthKey, -1)
        )
    }

    fun nextMonth() {
        controlState.value = controlState.value.copy(
            currentMonthKey = DateUtils.shiftMonth(controlState.value.currentMonthKey, 1)
        )
    }

    fun jumpToMonth(monthKey: String) {
        controlState.value = controlState.value.copy(currentMonthKey = monthKey)
    }

    fun jumpToToday() {
        val todayMonth = DateUtils.currentMonthKey()
        controlState.value = controlState.value.copy(currentMonthKey = todayMonth)
    }

    fun openDateEntry(dateIso: String) {
        controlState.value = controlState.value.copy(editingDateIso = dateIso)
    }

    fun dismissDateEntry() {
        controlState.value = controlState.value.copy(editingDateIso = null)
    }

    fun saveDailyEntry(
        dateIso: String,
        morningLiters: Double,
        eveningLiters: Double,
        sessionType: MilkSessionType,
        note: String
    ) {
        viewModelScope.launch {
            repository.saveEntry(dateIso, morningLiters, eveningLiters, sessionType, note)
            val total = if (sessionType == MilkSessionType.ABSENT) 0.0 else morningLiters + eveningLiters
            val msg = if (total <= 0.0) {
                "Marked Absent (0L) • अनुपस्थित दर्ज"
            } else {
                "Saved ${DateUtils.formatLiters(total)}L • सेव हो गया"
            }
            controlState.value = controlState.value.copy(
                editingDateIso = null,
                snackbarMessage = msg
            )
        }
    }

    fun clearDailyEntry(dateIso: String) {
        viewModelScope.launch {
            repository.deleteEntry(dateIso)
            controlState.value = controlState.value.copy(
                editingDateIso = null,
                snackbarMessage = "Entry removed • प्रविष्टि हटाई गई"
            )
        }
    }

    fun openRateDialog() {
        controlState.value = controlState.value.copy(showRateDialog = true)
    }

    fun dismissRateDialog() {
        controlState.value = controlState.value.copy(showRateDialog = false)
    }

    fun updateRate(newRate: Double, applyToCurrentMonth: Boolean) {
        viewModelScope.launch {
            val monthKey = if (applyToCurrentMonth) controlState.value.currentMonthKey else null
            repository.updateRate(newRate, monthKey)
            controlState.value = controlState.value.copy(
                showRateDialog = false,
                snackbarMessage = "Rate saved: ${DateUtils.formatRupees(newRate)}/L • दर सेव हुई"
            )
        }
    }

    fun updateVendorName(name: String) {
        viewModelScope.launch {
            repository.updateVendorName(name)
            showSnackbar("Dairy name updated • नाम सेव हुआ")
        }
    }

    fun updateLanguageMode(mode: String) {
        viewModelScope.launch {
            repository.updateLanguageMode(mode)
        }
    }

    fun toggleMonthPaid(monthKey: String) {
        viewModelScope.launch {
            repository.toggleMonthPaid(monthKey)
            showSnackbar("Payment status updated • भुगतान स्थिति अपडेट हुई")
        }
    }

    fun selectHistoryMonth(monthKey: String) {
        controlState.value = controlState.value.copy(selectedHistoryMonthKey = monthKey)
    }

    fun openCalculatorDialog() {
        controlState.value = controlState.value.copy(showCalculatorDialog = true)
    }

    fun dismissCalculatorDialog() {
        controlState.value = controlState.value.copy(showCalculatorDialog = false)
    }

    suspend fun generateBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun importBackupJson(rawJson: String) {
        viewModelScope.launch {
            repository.importBackupJson(rawJson)
                .onSuccess { res ->
                    showSnackbar("Restored ${res.entriesImported} entries & ${res.monthsImported} months • बैकअप रिस्टोर हुआ")
                }
                .onFailure {
                    showSnackbar("Could not read backup JSON • बैकअप फ़ाइल अमान्य है")
                }
        }
    }

    fun showSnackbar(message: String) {
        controlState.value = controlState.value.copy(snackbarMessage = message)
    }

    fun clearSnackbar() {
        controlState.value = controlState.value.copy(snackbarMessage = null)
    }

    companion object {
        fun factory(repository: MilkRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MilkHisabViewModel(repository) as T
                }
            }
        }
    }
}
