package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DailyPrayerTimes
import com.example.ui.NoorScreen
import com.example.ui.NoorViewModel
import com.example.ui.components.PrayerCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun HomeScreen(
    viewModel: NoorViewModel,
    modifier: Modifier = Modifier
) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val isGpsLoading by viewModel.isGpsLoading.collectAsState()
    val isAudioPlaying by viewModel.soundManager.isPlaying.collectAsState()
    val playingTitle by viewModel.soundManager.currentPlayingTitle.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        if (granted) {
            viewModel.fetchGpsLocation()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Dates & Location
            item {
                HeaderCard(
                    prayerTimes = prayerTimes,
                    isGpsLoading = isGpsLoading,
                    onGpsClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }

            // Audio Playing Banner
            item {
                AnimatedVisibility(
                    visible = isAudioPlaying,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    AudioPlayingCard(
                        playingTitle = playingTitle ?: "صوت الأذان",
                        onStop = { viewModel.stopAudio() }
                    )
                }
            }

            // Next Prayer Countdown Card
            item {
                prayerTimes?.let { daily ->
                    NextPrayerCountdownCard(
                        daily = daily,
                        onOpenQibla = { viewModel.setScreen(NoorScreen.QIBLA) }
                    )
                }
            }

            // Daily Prayer Tracker Card
            item {
                val prayerLogs by viewModel.todayPrayerLogs.collectAsState()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prayer_tracker_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سجل أداء الصلوات اليومية",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            val completedCount = prayerLogs.count { it.value }
                            Text(
                                text = "$completedCount من 5",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (completedCount == 5) EmeraldMedium else GoldAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                com.example.model.PrayerType.FAJR,
                                com.example.model.PrayerType.DHUHR,
                                com.example.model.PrayerType.ASR,
                                com.example.model.PrayerType.MAGHRIB,
                                com.example.model.PrayerType.ISHA
                            ).forEach { p ->
                                val isDone = prayerLogs[p.id] == true
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.togglePrayerTrack(p) }
                                        .background(
                                            if (isDone) EmeraldSoft.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = p.arabicName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isDone) EmeraldMedium else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isDone) "✓" else "○",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDone) EmeraldMedium else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Prayer Times Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مواقيت صلوات اليوم",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = prayerTimes?.cityName ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // List of prayer cards
            prayerTimes?.let { daily ->
                items(daily.prayers, key = { it.type.id }) { prayerItem ->
                    PrayerCard(
                        item = prayerItem,
                        onToggleAdhan = { enabled ->
                            viewModel.toggleAdhan(prayerItem.type, enabled)
                        }
                    )
                }
            }

            // Spiritual banner
            item {
                SpiritualFeatureCard(
                    onOpenTasbih = { viewModel.setScreen(NoorScreen.AZKAR) }
                )
            }
        }
    }
}

@Composable
private fun HeaderCard(
    prayerTimes: DailyPrayerTimes?,
    isGpsLoading: Boolean,
    onGpsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_header_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = prayerTimes?.hijriDateText ?: "جاري الحساب...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = prayerTimes?.gregorianDateText ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .clickable { onGpsClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("gps_location_button")
                ) {
                    if (isGpsLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "تحديد موقعي عبر GPS",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = prayerTimes?.cityName ?: "الموقع",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun NextPrayerCountdownCard(
    daily: DailyPrayerTimes,
    onOpenQibla: () -> Unit
) {
    val nextPrayer = daily.nextPrayer ?: return
    val remainingMillis = daily.remainingMillisToNext

    val hours = (remainingMillis / 3600000).toInt()
    val minutes = ((remainingMillis % 3600000) / 60000).toInt()
    val seconds = ((remainingMillis % 60000) / 1000).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("next_prayer_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            EmeraldDeep,
                            EmeraldMedium
                        )
                    )
                )
        ) {
            // Subtle Islamic pattern overlay if present
            Image(
                painter = painterResource(id = R.drawable.bg_islamic_pattern),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.12f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top row: Next prayer name & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الصلاة القادمة",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldLight.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "صلاة ${nextPrayer.type.arabicName}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldPrimary.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = nextPrayer.formattedTime,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Countdown timer boxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CountdownBox(value = String.format("%02d", hours), label = "ساعة")
                    Text(
                        text = ":",
                        color = GoldLight,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    CountdownBox(value = String.format("%02d", minutes), label = "دقيقة")
                    Text(
                        text = ":",
                        color = GoldLight,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    CountdownBox(value = String.format("%02d", seconds), label = "ثانية")
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Qibla quick indicator
                Surface(
                    onClick = onOpenQibla,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.testTag("quick_qibla_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "اتجاه القبلة",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "القبلة: ${daily.qiblaAngle.toInt()}° (${daily.distanceToMakkahKm.toInt()} كم إلى مكة)",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownBox(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = GoldLight.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun AudioPlayingCard(
    playingTitle: String,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audio_playing_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDeep)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "يتم تشغيل الأذان الآن",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldLight
                    )
                    Text(
                        text = playingTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("stop_adhan_button")
            ) {
                Icon(
                    imageVector = Icons.Default.StopCircle,
                    contentDescription = "إيقاف",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "إيقاف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SpiritualFeatureCard(
    onOpenTasbih: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onOpenTasbih() }
            .testTag("spiritual_tasbih_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmeraldSoft.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = EmeraldMedium,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "أذكار ما بعد الصلاة والمسبحة",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "استغفار، تسبيح، تحميد وتكبير لتمام الأجر",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "فتح ❯",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
