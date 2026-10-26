package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AdhanVoice
import com.example.audio.AudioSources
import com.example.data.CitiesData
import com.example.model.CalculationMethod
import com.example.model.CityLocation
import com.example.model.Madhhab
import com.example.model.PrayerType
import com.example.ui.NoorViewModel
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun SettingsScreen(
    viewModel: NoorViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isGpsLoading by viewModel.isGpsLoading.collectAsState()
    val isAudioPlaying by viewModel.soundManager.isPlaying.collectAsState()
    val playingTitle by viewModel.soundManager.currentPlayingTitle.collectAsState()

    var showCityDialog by remember { mutableStateOf(false) }

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
                .padding(horizontal = 16.dp)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Title
            item {
                Text(
                    text = "الإعدادات والتخصيص",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            // Cloud Sync & Google Account Section
            item {
                val currentUser by viewModel.currentUser.collectAsState()
                val context = androidx.compose.ui.platform.LocalContext.current
                val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
                val credentialManager = androidx.compose.runtime.remember { androidx.credentials.CredentialManager.create(context) }
                var authError by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

                SectionCard(title = "الحساب والمزامنة السحابية (Firebase)") {
                    if (currentUser != null) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSoft.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = EmeraldMedium,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = currentUser?.displayName ?: "مستخدم نور",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = currentUser?.email ?: "تم تسجيل الدخول",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        com.example.ui.auth.signOut(
                                            context = context,
                                            credentialManager = credentialManager,
                                            onSignOutComplete = {},
                                            scope = coroutineScope
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("خروج", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "✓ الإعدادات وسجل الصلوات متزامنة سحابياً مع حسابك في Firebase.",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldMedium
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "قم بتسجيل الدخول لحفظ إعداداتك ومواقيتك وسجل صلواتك عبر السحابة واستعادتها على أي جهاز:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            com.example.ui.auth.GoogleSignInButton(
                                onAuthSuccess = { authError = null },
                                onAuthError = { authError = it }
                            )
                            if (authError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = authError ?: "",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // 1. Location Settings Section
            item {
                SectionCard(title = "الموقع الجغرافي والمدينة") {
                    // Auto GPS toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تحديد الموقع تلقائياً (GPS)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (settings.useAutoLocation) "مفعّل: ${settings.selectedCity.nameArabic}" else "معطل: استخدام مدينة محددة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isGpsLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Switch(
                                checked = settings.useAutoLocation,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    } else {
                                        viewModel.updateSettings(settings.copy(useAutoLocation = false))
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldMedium
                                ),
                                modifier = Modifier.testTag("auto_location_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Choose City button
                    Surface(
                        onClick = { showCityDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("choose_city_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "المدينة المختارة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${settings.selectedCity.nameArabic} - ${settings.selectedCity.countryArabic}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Text(
                                text = "تغيير ❯",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 2. Calculation Method & Madhhab
            item {
                SectionCard(title = "طريقة الحساب والمذهب") {
                    Text(
                        text = "طريقة الحساب الفلكي المعتمدة:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    CalculationMethod.values().forEach { method ->
                        val isSelected = (settings.method == method)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setMethod(method) }
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setMethod(method) },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldMedium)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = method.arabicName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = method.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Madhhab selection
                    Text(
                        text = "مذهب حساب صلاة العصر:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Madhhab.values().forEach { madhhab ->
                        val isSelected = (settings.madhhab == madhhab)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setMadhhab(madhhab) }
                                .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setMadhhab(madhhab) },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = madhhab.arabicName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 3. Manual Minutes Offsets
            item {
                SectionCard(title = "التعديل اليدوي للمواقيت (بالدقائق)") {
                    Text(
                        text = "يمكنك تقديم أو تأخير أي وقت بمقدار دقائق لمطابقة تقويم مسجدك المحلي:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    PrayerType.values().forEach { prayer ->
                        val currentOffset = settings.offsets[prayer] ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = prayer.arabicName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.setOffset(prayer, currentOffset - 1) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "إنقاص دقيقة",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = if (currentOffset > 0) "+$currentOffset د" else "$currentOffset د",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentOffset != 0) GoldAccent else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .width(54.dp)
                                        .padding(horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                IconButton(
                                    onClick = { viewModel.setOffset(prayer, currentOffset + 1) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "زيادة دقيقة",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Adhan & Audio Settings
            item {
                SectionCard(title = "إعدادات الأذان والتنبيهات") {
                    // Volume Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "مستوى صوت الأذان",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(settings.volume * 100).toInt()}%",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldMedium
                            )
                        }
                        Slider(
                            value = settings.volume,
                            onValueChange = { viewModel.setVolume(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldMedium,
                                activeTrackColor = EmeraldMedium
                            ),
                            modifier = Modifier.testTag("volume_slider")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Choose Main Adhan Voice
                    Text(
                        text = "صوت أذان الصلوات (الظهر، العصر، المغرب، العشاء):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val generalVoices = AudioSources.voices.filter { !it.isFajrOnly }
                    generalVoices.forEach { voice ->
                        val isSelected = (settings.selectedVoiceId == voice.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setSelectedVoice(voice.id) }
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setSelectedVoice(voice.id) },
                                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldMedium)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = voice.titleArabic,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = voice.muadhinArabic,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Preview test button
                            Button(
                                onClick = {
                                    if (isAudioPlaying && playingTitle == voice.titleArabic) {
                                        viewModel.stopAudio()
                                    } else {
                                        viewModel.testAudioVoice(voice)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAudioPlaying && playingTitle == voice.titleArabic) Color.Red else EmeraldMedium
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("preview_voice_${voice.id}")
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying && playingTitle == voice.titleArabic) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "تجربة الصوت",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAudioPlaying && playingTitle == voice.titleArabic) "إيقاف" else "تجربة",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Separate Fajr Adhan
                    Text(
                        text = "صوت أذان الفجر (الصلاة خير من النوم):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val fajrVoices = AudioSources.voices.filter { it.isFajrOnly || it.id == "takbeerat" || it.id == "chime" }
                    fajrVoices.forEach { voice ->
                        val isSelected = (settings.fajrVoiceId == voice.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setFajrVoice(voice.id) }
                                .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setFajrVoice(voice.id) },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = voice.titleArabic,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = voice.muadhinArabic,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (isAudioPlaying && playingTitle == voice.titleArabic) {
                                        viewModel.stopAudio()
                                    } else {
                                        viewModel.testAudioVoice(voice)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAudioPlaying && playingTitle == voice.titleArabic) Color.Red else GoldAccent
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying && playingTitle == voice.titleArabic) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "تجربة",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAudioPlaying && playingTitle == voice.titleArabic) "إيقاف" else "تجربة",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 5. Appearance (Dark / Light)
            item {
                SectionCard(title = "المظهر والواجهة") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (settings.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "الوضع الليلي الداكن",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (settings.isDarkTheme) "الوضع الداكن مفعّل" else "الوضع الفاتح مفعّل",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = settings.isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldMedium
                            ),
                            modifier = Modifier.testTag("dark_theme_switch")
                        )
                    }
                }
            }
        }
    }

    // City Selection Modal Dialog
    if (showCityDialog) {
        CitySelectionDialog(
            currentCity = settings.selectedCity,
            onCitySelected = { city ->
                viewModel.setCity(city)
                showCityDialog = false
            },
            onDismiss = { showCityDialog = false }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun CitySelectionDialog(
    currentCity: CityLocation,
    onCitySelected: (CityLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            CitiesData.cities
        } else {
            val q = searchQuery.trim().lowercase()
            CitiesData.cities.filter {
                it.nameArabic.contains(q) ||
                        it.countryArabic.contains(q) ||
                        it.nameEnglish.lowercase().contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اختر مدينتك",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن مدينة أو دولة...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_city_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(filteredCities) { city ->
                        val isSelected = (city.nameArabic == currentCity.nameArabic)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onCitySelected(city) }
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = city.nameArabic,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = city.countryArabic,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}
