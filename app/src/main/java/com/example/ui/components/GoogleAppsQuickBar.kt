package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalDairyColors
import com.example.util.GoogleQuickApp

@Composable
fun GoogleAppsQuickBar(
    languageMode: String,
    onAppClick: (GoogleQuickApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val dairyColors = LocalDairyColors.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (languageMode) {
                        "ENGLISH" -> "Google Apps & Tools"
                        "HINDI" -> "गूगल ऐप्स और टूल्स"
                        else -> "Google Apps • गूगल ऐप्स"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (languageMode == "HINDI") "तुरंत खोलें" else "Quick Launch",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(GoogleQuickApp.entries) { app ->
                    val (icon, tint, container) = appVisualSpec(app, dairyColors)
                    val label = when (languageMode) {
                        "HINDI" -> app.titleHi
                        else -> app.titleEn
                    }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onAppClick(app) }
                            .testTag("quick_app_${app.name.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(
                            modifier = Modifier
                                .width(74.dp)
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = CircleShape,
                                color = container
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = app.titleEn,
                                    tint = tint,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun appVisualSpec(
    app: GoogleQuickApp,
    dairyColors: com.example.ui.theme.DairyExtendedColors
): Triple<ImageVector, Color, Color> {
    return when (app) {
        GoogleQuickApp.CALENDAR -> Triple(
            Icons.Rounded.CalendarMonth,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
        GoogleQuickApp.KEEP -> Triple(
            Icons.Rounded.EditNote,
            dairyColors.morningAccent,
            dairyColors.morningContainer
        )
        GoogleQuickApp.DRIVE -> Triple(
            Icons.Rounded.Cloud,
            dairyColors.deliveredAccent,
            dairyColors.deliveredContainer
        )
        GoogleQuickApp.GMAIL -> Triple(
            Icons.Rounded.Email,
            dairyColors.absentAccent,
            dairyColors.absentContainer
        )
        GoogleQuickApp.MAPS -> Triple(
            Icons.Rounded.LocationOn,
            dairyColors.deliveredAccent,
            dairyColors.deliveredContainer
        )
        GoogleQuickApp.GPAY -> Triple(
            Icons.Rounded.AccountBalanceWallet,
            dairyColors.eveningAccent,
            dairyColors.eveningContainer
        )
        GoogleQuickApp.CALCULATOR -> Triple(
            Icons.Rounded.Calculate,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    }
}
