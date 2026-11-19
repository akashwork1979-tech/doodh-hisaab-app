package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class ImportResult(
    val entriesImported: Int,
    val monthsImported: Int
)

class MilkRepository(private val dao: MilkDao) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val backupAdapter = moshi.adapter(DoodhHisabBackup::class.java).indent("  ")

    val settingsFlow: Flow<AppSettingsEntity> = dao.observeSettings().map {
        it ?: AppSettingsEntity()
    }

    val allEntriesFlow: Flow<List<MilkEntryEntity>> = dao.observeAllEntries()

    val allMonthRecordsFlow: Flow<List<MonthRecordEntity>> = dao.observeAllMonthRecords()

    suspend fun ensureInitialized() {
        if (dao.getSettingsOnce() == null) {
            dao.upsertSettings(AppSettingsEntity())
        }
    }

    suspend fun saveEntry(
        dateIso: String,
        morningLiters: Double,
        eveningLiters: Double,
        sessionType: MilkSessionType,
        note: String
    ) {
        val monthKey = dateIso.take(7)
        val safeMorning = if (sessionType == MilkSessionType.ABSENT) 0.0 else morningLiters.coerceAtLeast(0.0)
        val safeEvening = if (sessionType == MilkSessionType.ABSENT) 0.0 else eveningLiters.coerceAtLeast(0.0)
        val total = safeMorning + safeEvening
        val resolvedSession = when {
            sessionType == MilkSessionType.ABSENT || total <= 0.0 -> MilkSessionType.ABSENT
            safeMorning > 0.0 && safeEvening > 0.0 -> MilkSessionType.BOTH
            safeEvening > 0.0 -> MilkSessionType.EVENING
            else -> MilkSessionType.MORNING
        }
        dao.upsertEntry(
            MilkEntryEntity(
                dateIso = dateIso,
                monthKey = monthKey,
                morningLiters = safeMorning,
                eveningLiters = safeEvening,
                totalLiters = total,
                sessionTag = resolvedSession.name,
                note = note.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
        // Ensure a month record exists with the active rate snapshot
        if (dao.getMonthRecordOnce(monthKey) == null) {
            val rate = dao.getSettingsOnce()?.defaultRatePerLiter ?: 60.0
            dao.upsertMonthRecord(
                MonthRecordEntity(
                    monthKey = monthKey,
                    ratePerLiter = rate,
                    isPaid = false,
                    paidAt = null
                )
            )
        }
    }

    suspend fun deleteEntry(dateIso: String) {
        dao.deleteEntry(dateIso)
    }

    suspend fun updateRate(newRate: Double, applyToMonthKey: String?) {
        val validRate = newRate.coerceIn(1.0, 5000.0)
        val current = dao.getSettingsOnce() ?: AppSettingsEntity()
        dao.upsertSettings(current.copy(defaultRatePerLiter = validRate))

        if (applyToMonthKey != null) {
            val existing = dao.getMonthRecordOnce(applyToMonthKey)
            dao.upsertMonthRecord(
                MonthRecordEntity(
                    monthKey = applyToMonthKey,
                    ratePerLiter = validRate,
                    isPaid = existing?.isPaid ?: false,
                    paidAt = existing?.paidAt
                )
            )
        }
    }

    suspend fun updateMonthRateOnly(monthKey: String, newRate: Double) {
        val validRate = newRate.coerceIn(1.0, 5000.0)
        val existing = dao.getMonthRecordOnce(monthKey)
        dao.upsertMonthRecord(
            MonthRecordEntity(
                monthKey = monthKey,
                ratePerLiter = validRate,
                isPaid = existing?.isPaid ?: false,
                paidAt = existing?.paidAt
            )
        )
    }

    suspend fun updateVendorName(name: String) {
        val current = dao.getSettingsOnce() ?: AppSettingsEntity()
        dao.upsertSettings(current.copy(dairyVendorName = name.trim().ifBlank { "Dairy / दूधवाला" }))
    }

    suspend fun updateLanguageMode(mode: String) {
        val current = dao.getSettingsOnce() ?: AppSettingsEntity()
        dao.upsertSettings(current.copy(languageMode = mode))
    }

    suspend fun toggleMonthPaid(monthKey: String) {
        val existing = dao.getMonthRecordOnce(monthKey)
        val defaultRate = dao.getSettingsOnce()?.defaultRatePerLiter ?: 60.0
        val newPaid = !(existing?.isPaid ?: false)
        dao.upsertMonthRecord(
            MonthRecordEntity(
                monthKey = monthKey,
                ratePerLiter = existing?.ratePerLiter ?: defaultRate,
                isPaid = newPaid,
                paidAt = if (newPaid) System.currentTimeMillis() else null
            )
        )
    }

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val settings = dao.getSettingsOnce() ?: AppSettingsEntity()
        val monthRecords = dao.getAllMonthRecordsOnce()
        val entries = dao.getAllEntriesOnce()
        val backup = DoodhHisabBackup(
            schemaVersion = 1,
            exportedAt = System.currentTimeMillis(),
            settings = BackupSettings(
                defaultRatePerLiter = settings.defaultRatePerLiter,
                defaultQuantityLiters = settings.defaultQuantityLiters,
                dairyVendorName = settings.dairyVendorName,
                languageMode = settings.languageMode
            ),
            monthRecords = monthRecords.map {
                BackupMonthRecord(
                    monthKey = it.monthKey,
                    ratePerLiter = it.ratePerLiter,
                    isPaid = it.isPaid,
                    paidAt = it.paidAt
                )
            },
            entries = entries.map {
                BackupMilkEntry(
                    dateIso = it.dateIso,
                    monthKey = it.monthKey,
                    morningLiters = it.morningLiters,
                    eveningLiters = it.eveningLiters,
                    totalLiters = it.totalLiters,
                    sessionTag = it.sessionTag,
                    note = it.note,
                    updatedAt = it.updatedAt
                )
            }
        )
        backupAdapter.toJson(backup)
    }

    suspend fun importBackupJson(rawJson: String): Result<ImportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val parsed = backupAdapter.fromJson(rawJson)
                ?: throw IllegalArgumentException("Invalid JSON backup format")
            val safeRate = parsed.settings.defaultRatePerLiter.coerceIn(1.0, 5000.0)
            dao.upsertSettings(
                AppSettingsEntity(
                    id = 1,
                    defaultRatePerLiter = safeRate,
                    defaultQuantityLiters = parsed.settings.defaultQuantityLiters.coerceIn(0.25, 50.0),
                    dairyVendorName = parsed.settings.dairyVendorName.ifBlank { "Dairy / दूधवाला" },
                    languageMode = parsed.settings.languageMode.ifBlank { "BILINGUAL" }
                )
            )
            val validMonths = parsed.monthRecords
                .filter { it.monthKey.matches(Regex("\\d{4}-\\d{2}")) }
                .map {
                    MonthRecordEntity(
                        monthKey = it.monthKey,
                        ratePerLiter = it.ratePerLiter.coerceIn(1.0, 5000.0),
                        isPaid = it.isPaid,
                        paidAt = it.paidAt
                    )
                }
            if (validMonths.isNotEmpty()) {
                dao.upsertMonthRecords(validMonths)
            }

            val validEntries = parsed.entries
                .filter { it.dateIso.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
                .map {
                    val mKey = it.monthKey.takeIf { mk -> mk.matches(Regex("\\d{4}-\\d{2}")) }
                        ?: it.dateIso.take(7)
                    MilkEntryEntity(
                        dateIso = it.dateIso,
                        monthKey = mKey,
                        morningLiters = it.morningLiters.coerceAtLeast(0.0),
                        eveningLiters = it.eveningLiters.coerceAtLeast(0.0),
                        totalLiters = it.totalLiters.coerceAtLeast(0.0),
                        sessionTag = MilkSessionType.fromString(it.sessionTag).name,
                        note = it.note,
                        updatedAt = it.updatedAt
                    )
                }
            if (validEntries.isNotEmpty()) {
                dao.upsertEntries(validEntries)
            }
            ImportResult(
                entriesImported = validEntries.size,
                monthsImported = validMonths.size
            )
        }
    }
}
