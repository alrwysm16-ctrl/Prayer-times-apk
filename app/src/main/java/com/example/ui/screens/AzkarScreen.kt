package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NoorViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

data class ZikrItem(
    val text: String,
    val countTarget: Int,
    val reward: String
)

@Composable
fun AzkarScreen(
    viewModel: NoorViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("المسبحة الإلكترونية", "أذكار بعد الصلاة")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("azkar_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldMedium,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = EmeraldMedium
                )
            },
            modifier = Modifier.widthIn(max = 600.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = (selectedTab == index),
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            TasbihSection(viewModel)
        } else {
            PostPrayerAzkarSection(viewModel)
        }
    }
}

@Composable
private fun TasbihSection(viewModel: NoorViewModel) {
    val tasbihState by viewModel.tasbihState.collectAsState()
    val progress = (tasbihState.count.toFloat() / tasbihState.target.toFloat()).coerceIn(0f, 1f)

    val phrases = listOf("سبحان الله", "الحمد لله", "الله أكبر", "لا إله إلا الله وحده لا شريك له")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Phrase Selection chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            phrases.take(3).forEach { phrase ->
                val isSelected = (tasbihState.currentPhrase == phrase)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) EmeraldMedium
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { viewModel.selectTasbihPhrase(phrase) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = phrase,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Center Big Tasbih Bead / Click Counter
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = tasbihState.currentPhrase,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Massive Tactile Counter Button with Circular Progress
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .shadow(12.dp, CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                EmeraldMedium,
                                EmeraldDeep
                            )
                        )
                    )
                    .border(4.dp, GoldPrimary, CircleShape)
                    .clickable { viewModel.incrementTasbih() }
                    .testTag("tasbih_counter_button"),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(228.dp),
                    color = GoldLight,
                    strokeWidth = 6.dp,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${tasbihState.count}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 64.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "من ${tasbihState.target}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "المس للتسبيح",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Bottom Controls: Reset and Total Cycles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
                .padding(bottom = 80.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مجموع الدورات: ${tasbihState.totalCycles}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { viewModel.resetTasbih() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("reset_tasbih_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "إعادة ضبط",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("إعادة ضبط")
            }
        }
    }
}

@Composable
private fun PostPrayerAzkarSection(viewModel: NoorViewModel) {
    val azkarList = remember {
        listOf(
            ZikrItem(
                text = "أَسْتَغْفِرُ اللَّهَ (ثَلاثاً)، اللَّهُمَّ أَنْتَ السَّلامُ وَمِنْكَ السَّلامُ، تَبَارَكْتَ يَا ذَا الجَلالِ وَالإِكْرَامِ.",
                countTarget = 1,
                reward = "سنة المصطفى ﷺ بعد كل فريضة"
            ),
            ZikrItem(
                text = "لا إِلَهَ إِلا اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لا مَانِعَ لِمَا أَعْطَيْتَ، وَلا مُعْطِيَ لِمَا مَنَعْتَ، وَلا يَنْفَعُ ذَا الجَدِّ مِنْكَ الجَدُّ.",
                countTarget = 1,
                reward = "رواه البخاري ومسلم"
            ),
            ZikrItem(
                text = "سُبْحَانَ اللَّهِ (33 مرة)",
                countTarget = 33,
                reward = "تغفر الخطايا وإن كانت مثل زبد البحر"
            ),
            ZikrItem(
                text = "الحَمْدُ لِلَّهِ (33 مرة)",
                countTarget = 33,
                reward = "تملأ الميزان"
            ),
            ZikrItem(
                text = "اللَّهُ أَكْبَرُ (33 مرة)",
                countTarget = 33,
                reward = "تمام التسبيح والتكبير"
            ),
            ZikrItem(
                text = "لا إِلَهَ إِلا اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ (تمام المائة).",
                countTarget = 1,
                reward = "تمام المائة بعد الصلاة"
            ),
            ZikrItem(
                text = "قراءة آية الكرسي: {اللَّهُ لاَ إِلَٰهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ...}",
                countTarget = 1,
                reward = "من قرأها دبر كل صلاة لم يمنعه من دخول الجنة إلا أن يموت"
            )
        )
    }

    val currentCounts = remember { mutableStateListOf(*Array(azkarList.size) { 0 }) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(azkarList) { index, item ->
            val count = currentCounts[index]
            val isDone = count >= item.countTarget

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isDone) {
                            currentCounts[index] = count + 1
                            viewModel.compassSensorManager.checkAndTriggerHaptic(true)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.reward,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isDone) EmeraldMedium else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isDone) "تم ✓" else "$count / ${item.countTarget}",
                                    color = if (isDone) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
