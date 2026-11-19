package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MilkSessionType {
    MORNING,
    EVENING,
    BOTH,
    ABSENT;

    companion object {
        fun fromString(raw: String?): MilkSessionType {
            return entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: MORNING
        }
    }
}

@Entity(
    tableName = "milk_entries",
    indices = [Index(value = ["monthKey"])]
)
data class MilkEntryEntity(
    @PrimaryKey
    val dateIso: String,
    val monthKey: String,
    val morningLiters: Double,
    val eveningLiters: Double,
    val totalLiters: Double,
    val sessionTag: String,
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "month_records")
data class MonthRecordEntity(
    @PrimaryKey
    val monthKey: String,
    val ratePerLiter: Double,
    val isPaid: Boolean = false,
    val paidAt: Long? = null
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val defaultRatePerLiter: Double = 60.0,
    val defaultQuantityLiters: Double = 1.0,
    val dairyVendorName: String = "Dairy / दूधवाला",
    val languageMode: String = "BILINGUAL"
)
