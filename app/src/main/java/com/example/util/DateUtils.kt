package com.example.util

import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {

    private val englishMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val hindiMonths = listOf(
        "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून",
        "जुलाई", "अगस्त", "सितंबर", "अक्टूबर", "नवंबर", "दिसंबर"
    )

    private val englishDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    private val hindiDays = listOf("रवि", "सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि")

    fun todayIso(): String {
        val cal = Calendar.getInstance()
        return formatIso(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun currentMonthKey(): String {
        val cal = Calendar.getInstance()
        return formatMonthKey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun formatMonthKey(year: Int, monthOneBased: Int): String {
        return String.format(Locale.US, "%04d-%02d", year, monthOneBased)
    }

    fun formatIso(year: Int, monthOneBased: Int, day: Int): String {
        return String.format(Locale.US, "%04d-%02d-%02d", year, monthOneBased, day)
    }

    fun parseMonthKey(monthKey: String): Pair<Int, Int> {
        val parts = monthKey.split("-")
        if (parts.size == 2) {
            val y = parts[0].toIntOrNull()
            val m = parts[1].toIntOrNull()
            if (y != null && m != null && m in 1..12) {
                return y to m
            }
        }
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
    }

    fun parseDateIso(dateIso: String): Triple<Int, Int, Int>? {
        val parts = dateIso.split("-")
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        if (m !in 1..12 || d !in 1..31) return null
        return Triple(y, m, d)
    }

    fun shiftMonth(monthKey: String, deltaMonths: Int): String {
        val (year, month) = parseMonthKey(monthKey)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, deltaMonths)
        }
        return formatMonthKey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun daysInMonth(monthKey: String): Int {
        val (year, month) = parseMonthKey(monthKey)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /** Returns 0 for Sunday .. 6 for Saturday */
    fun firstDayOfWeekOffset(monthKey: String): Int {
        val (year, month) = parseMonthKey(monthKey)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    }

    fun monthLabelEnglish(monthKey: String): String {
        val (year, month) = parseMonthKey(monthKey)
        return "${englishMonths[month - 1]} $year"
    }

    fun monthLabelHindi(monthKey: String): String {
        val (year, month) = parseMonthKey(monthKey)
        return "${hindiMonths[month - 1]} $year"
    }

    fun monthLabelBilingual(monthKey: String, languageMode: String = "BILINGUAL"): String {
        return when (languageMode) {
            "ENGLISH" -> monthLabelEnglish(monthKey)
            "HINDI" -> monthLabelHindi(monthKey)
            else -> "${monthLabelEnglish(monthKey)} • ${hindiMonths[parseMonthKey(monthKey).second - 1]}"
        }
    }

    fun weekdayHeaders(): List<Pair<String, String>> {
        return englishDays.zip(hindiDays)
    }

    fun formatReadableDate(dateIso: String): String {
        val parsed = parseDateIso(dateIso) ?: return dateIso
        val (year, month, day) = parsed
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
        }
        val dowIndex = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        val shortMonth = englishMonths[month - 1].take(3)
        return "$day $shortMonth $year • ${englishDays[dowIndex]} / ${hindiDays[dowIndex]}"
    }

    fun formatShortDay(dateIso: String): String {
        val parsed = parseDateIso(dateIso) ?: return dateIso
        val (_, month, day) = parsed
        val shortMonth = englishMonths[month - 1].take(3)
        return "$day $shortMonth"
    }

    fun formatLiters(value: Double): String {
        val df = DecimalFormat("0.##")
        return df.format(value)
    }

    fun formatRupees(amount: Double): String {
        val rounded = kotlin.math.round(amount * 100.0) / 100.0
        val df = if (rounded % 1.0 == 0.0) {
            DecimalFormat("#,##0")
        } else {
            DecimalFormat("#,##0.##")
        }
        return "₹${df.format(rounded)}"
    }

    fun backupFileName(): String {
        val cal = Calendar.getInstance()
        val stamp = String.format(
            Locale.US,
            "%04d%02d%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        return "doodh_hisab_backup_$stamp.json"
    }
}
