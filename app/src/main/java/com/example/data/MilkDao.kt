package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkDao {

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: AppSettingsEntity)

    @Query("SELECT * FROM milk_entries WHERE monthKey = :monthKey ORDER BY dateIso ASC")
    fun observeEntriesForMonth(monthKey: String): Flow<List<MilkEntryEntity>>

    @Query("SELECT * FROM milk_entries ORDER BY dateIso DESC")
    fun observeAllEntries(): Flow<List<MilkEntryEntity>>

    @Query("SELECT * FROM milk_entries ORDER BY dateIso ASC")
    suspend fun getAllEntriesOnce(): List<MilkEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: MilkEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntries(entries: List<MilkEntryEntity>)

    @Query("DELETE FROM milk_entries WHERE dateIso = :dateIso")
    suspend fun deleteEntry(dateIso: String)

    @Query("SELECT * FROM month_records ORDER BY monthKey DESC")
    fun observeAllMonthRecords(): Flow<List<MonthRecordEntity>>

    @Query("SELECT * FROM month_records WHERE monthKey = :monthKey LIMIT 1")
    suspend fun getMonthRecordOnce(monthKey: String): MonthRecordEntity?

    @Query("SELECT * FROM month_records ORDER BY monthKey DESC")
    suspend fun getAllMonthRecordsOnce(): List<MonthRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMonthRecord(record: MonthRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMonthRecords(records: List<MonthRecordEntity>)
}
