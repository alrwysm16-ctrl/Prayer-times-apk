package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary

@Composable
fun PrayerCard(
    item: PrayerTimeItem,
    onToggleAdhan: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isNext = item.isNext
    val isCurrent = item.isCurrent

    val cardBgColor by animateColorAsState(
        targetValue = when {
            isNext -> MaterialTheme.colorScheme.primaryContainer
            isCurrent -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "cardBgColor"
    )

    val borderColor = when {
        isNext -> GoldPrimary
        isCurrent -> EmeraldSoft
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    }

    val icon = getPrayerIcon(item.type)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prayer_card_${item.type.id}")
            .border(
                width = if (isNext || isCurrent) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right side in RTL: Icon + Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNext) GoldPrimary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.type.arabicName,
                        tint = if (isNext) GoldAccent else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.type.arabicName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldPrimary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "القادمة",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldSoft.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "الحالية",
                                    color = EmeraldMedium,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (item.offsetMinutes != 0) {
                        Text(
                            text = if (item.offsetMinutes > 0) "+${item.offsetMinutes} د" else "${item.offsetMinutes} د",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Left side in RTL: Time + Adhan notification toggle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.formattedTime,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 18.sp
                    ),
                    color = if (isNext) GoldAccent else MaterialTheme.colorScheme.onSurface
                )

                if (item.type != PrayerType.SUNRISE) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { onToggleAdhan(!item.adhanEnabled) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("adhan_toggle_${item.type.id}")
                    ) {
                        Icon(
                            imageVector = if (item.adhanEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = if (item.adhanEnabled) "إيقاف تنبيه الصلاة" else "تفعيل تنبيه الصلاة",
                            tint = if (item.adhanEnabled) EmeraldSoft else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getPrayerIcon(type: PrayerType): ImageVector {
    return when (type) {
        PrayerType.FAJR -> Icons.Default.Brightness2
        PrayerType.SUNRISE -> Icons.Default.Brightness5
        PrayerType.DHUHR -> Icons.Default.WbSunny
        PrayerType.ASR -> Icons.Default.Brightness7
        PrayerType.MAGHRIB -> Icons.Default.Brightness6
        PrayerType.ISHA -> Icons.Default.Brightness2
    }
}
