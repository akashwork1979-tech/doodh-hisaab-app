package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import java.util.Calendar
import java.util.Locale

enum class GoogleQuickApp(
    val titleEn: String,
    val titleHi: String,
    val packageName: String?,
    val fallbackUrl: String?
) {
    CALENDAR("Calendar", "कैलेंडर", "com.google.android.calendar", "https://calendar.google.com"),
    KEEP("Keep", "कीप नोट्स", "com.google.android.keep", "https://keep.google.com"),
    DRIVE("Drive", "ड्राइव", "com.google.android.apps.docs", "https://drive.google.com"),
    GMAIL("Gmail", "जीमेल", "com.google.android.gm", "https://mail.google.com"),
    MAPS("Maps", "मैप्स", "com.google.android.apps.maps", "https://maps.google.com"),
    GPAY("GPay", "जी-पे", "com.google.android.apps.nbu.paisa.user", "https://pay.google.com"),
    CALCULATOR("Calculator", "कैलकुलेटर", null, null)
}

object ExternalAppLauncher {

    fun openBillReminderInGoogleCalendar(
        context: Context,
        monthLabel: String,
        vendorName: String,
        deliveredDays: Int,
        totalLiters: Double,
        ratePerLiter: Double,
        totalAmount: Double,
        isPaid: Boolean
    ): Boolean {
        val statusText = if (isPaid) "Paid (भुगतान हो गया)" else "Unpaid (बाकी है)"
        val title = "Milk Bill ($monthLabel) - ${DateUtils.formatRupees(totalAmount)}"
        val details = buildString {
            appendLine("Doodh Hisab • दूध का हिसाब")
            appendLine("Vendor: $vendorName")
            appendLine("Month: $monthLabel")
            appendLine("Delivered Days: $deliveredDays")
            appendLine("Total Milk: ${DateUtils.formatLiters(totalLiters)} L")
            appendLine("Rate: ${DateUtils.formatRupees(ratePerLiter)}/L")
            appendLine("Total Amount: ${DateUtils.formatRupees(totalAmount)}")
            append("Status: $statusText")
        }

        // Schedule reminder for tomorrow morning 9:00 AM - 9:30 AM local format
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startStr = String.format(
            Locale.US,
            "%04d%02d%02dT090000",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val endStr = String.format(
            Locale.US,
            "%04d%02d%02dT093000",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )

        val url = "https://calendar.google.com/calendar/render?action=TEMPLATE" +
            "&text=${encode(title)}" +
            "&details=${encode(details)}" +
            "&dates=$startStr/$endStr"

        return openUrl(context, url)
    }

    /**
     * Launches the requested Google/utility app with graceful web fallback.
     * For CALCULATOR, attempts system calculator first and returns false if unavailable
     * so the UI can open the built-in offline milk calculator dialog.
     */
    fun launchQuickApp(context: Context, app: GoogleQuickApp): Boolean {
        if (app == GoogleQuickApp.CALCULATOR) {
            return try {
                val calcIntent = Intent.makeMainSelectorActivity(
                    Intent.ACTION_MAIN,
                    Intent.CATEGORY_APP_CALCULATOR
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(calcIntent)
                true
            } catch (_: ActivityNotFoundException) {
                false
            } catch (_: Exception) {
                false
            }
        }

        val pkg = app.packageName
        if (pkg != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                return try {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } catch (_: Exception) {
                    app.fallbackUrl?.let { openUrl(context, it) } ?: false
                }
            }
        }

        return app.fallbackUrl?.let { openUrl(context, it) } ?: false
    }

    private fun openUrl(context: Context, url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, "UTF-8")
    }
}
