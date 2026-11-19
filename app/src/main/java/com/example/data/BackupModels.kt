package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DoodhHisabBackup(
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val settings: BackupSettings,
    val monthRecords: List<BackupMonthRecord>,
    val entries: List<BackupMilkEntry>
)

@JsonClass(generateAdapter = true)
data class BackupSettings(
    val defaultRatePerLiter: Double,
    val defaultQuantityLiters: Double = 1.0,
    val dairyVendorName: String = "Dairy / दूधवाला",
    val languageMode: String = "BILINGUAL"
)

@JsonClass(generateAdapter = true)
data class BackupMonthRecord(
    val monthKey: String,
    val ratePerLiter: Double,
    val isPaid: Boolean,
    val paidAt: Long? = null
)

@JsonClass(generateAdapter = true)
data class BackupMilkEntry(
    val dateIso: String,
    val monthKey: String,
    val morningLiters: Double,
    val eveningLiters: Double,
    val totalLiters: Double,
    val sessionTag: String,
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
