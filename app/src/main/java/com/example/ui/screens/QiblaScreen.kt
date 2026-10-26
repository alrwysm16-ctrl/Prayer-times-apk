package com.example.ui.screens

import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.NoorViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    viewModel: NoorViewModel,
    modifier: Modifier = Modifier
) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val sensorAzimuth by viewModel.compassAzimuth.collectAsState()
    val accuracy by viewModel.compassAccuracy.collectAsState()
    val isSensorAvailable by viewModel.isSensorAvailable.collectAsState()
    val manualHeading by viewModel.manualCompassHeading.collectAsState()

    // Manage compass sensor listener lifecycle on this screen
    DisposableEffect(Unit) {
        viewModel.compassSensorManager.startListening()
        onDispose {
            viewModel.compassSensorManager.stopListening()
        }
    }

    val currentHeading = if (isSensorAvailable) sensorAzimuth else manualHeading
    val qiblaAngle = prayerTimes?.qiblaAngle?.toFloat() ?: 0f

    // Calculate relative difference between current heading and Qibla
    var diffAngle = (qiblaAngle - currentHeading + 360f) % 360f
    if (diffAngle > 180f) diffAngle -= 360f

    val isAligned = abs(diffAngle) <= 3.0f

    // Trigger haptic feedback when aligned
    LaunchedEffect(isAligned) {
        viewModel.compassSensorManager.checkAndTriggerHaptic(isAligned)
    }

    val animatedDialRotation by animateFloatAsState(
        targetValue = -currentHeading,
        animationSpec = tween(durationMillis = 80),
        label = "dialRotation"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("qibla_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Info Bar
        prayerTimes?.let { daily ->
            QiblaHeaderInfo(
                cityName = daily.cityName,
                qiblaAngle = qiblaAngle.toInt(),
                distanceKm = daily.distanceToMakkahKm.toInt(),
                isAligned = isAligned
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calibration Alert Banner if sensor accuracy is low or sensor is unavailable
        if (isSensorAvailable && accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            CalibrationBanner()
            Spacer(modifier = Modifier.height(12.dp))
        } else if (!isSensorAvailable) {
            NoSensorBanner(
                manualHeading = manualHeading,
                onHeadingChange = { viewModel.setManualHeading(it) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Compass Viewport
        RealisticCompassView(
            dialRotation = animatedDialRotation,
            qiblaAngle = qiblaAngle,
            isAligned = isAligned,
            modifier = Modifier
                .size(310.dp)
                .testTag("qibla_compass_view")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Alignment Status Badge
        AlignmentStatusBadge(
            isAligned = isAligned,
            diffAngle = diffAngle.toInt()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Guide Instructions Card
        QiblaTipsCard()

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun QiblaHeaderInfo(
    cityName: String,
    qiblaAngle: Int,
    distanceKm: Int,
    isAligned: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
            .testTag("qibla_header_info"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAligned) EmeraldMedium else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "زاوية القبلة",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isAligned) GoldLight else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$qiblaAngle°",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isAligned) Color.White else MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(if (isAligned) GoldPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "المسافة إلى مكة",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isAligned) GoldLight else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$distanceKm كم",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isAligned) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(if (isAligned) GoldPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "المدينة الحالية",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isAligned) GoldLight else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = cityName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isAligned) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun CalibrationBanner() {
    Surface(
        color = GoldAccent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
            .testTag("compass_calibration_banner")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RotateRight,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "معايرة البوصلة مطلوبة",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = GoldAccent
                )
                Text(
                    text = "حرّك الهاتف في الهواء على شكل رقم 8 لمعايرة دقة الحساسات.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun NoSensorBanner(
    manualHeading: Float,
    onHeadingChange: (Float) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حساس الاتجاه غير متاح - وضع التوجيه اليدوي",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "اسحب الشريط لتدوير البوصلة يدوياً حسب اتجاه جهازك:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = manualHeading,
                onValueChange = onHeadingChange,
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(
                    thumbColor = GoldPrimary,
                    activeTrackColor = EmeraldMedium
                )
            )
        }
    }
}

@Composable
private fun RealisticCompassView(
    dialRotation: Float,
    qiblaAngle: Float,
    isAligned: Boolean,
    modifier: Modifier = Modifier
) {
    val dialBorderColor by animateColorAsState(
        targetValue = if (isAligned) SuccessGreen else GoldPrimary.copy(alpha = 0.6f),
        label = "dialBorder"
    )

    val dialGlowColor by animateColorAsState(
        targetValue = if (isAligned) SuccessGreen.copy(alpha = 0.25f) else Color.Transparent,
        label = "glowColor"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isAligned) 14.dp else 6.dp,
                shape = CircleShape,
                ambientColor = dialBorderColor,
                spotColor = dialBorderColor
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        EmeraldDeep,
                        DarkSurface
                    )
                ),
                shape = CircleShape
            )
            .border(width = 4.dp, color = dialBorderColor, shape = CircleShape)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Dial with 360 degree markings, tick lines, and Arabic cardinal points (ش، ج، ق، غ)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(dialRotation)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // Inner circle ring
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = radius * 0.82f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw 360 degree ticks
            for (degree in 0 until 360 step 5) {
                val isMajor = (degree % 30 == 0)
                val isCardinal = (degree % 90 == 0)

                val tickLength = when {
                    isCardinal -> 14.dp.toPx()
                    isMajor -> 9.dp.toPx()
                    else -> 5.dp.toPx()
                }

                val strokeWidth = when {
                    isCardinal -> 2.5.dp.toPx()
                    isMajor -> 1.5.dp.toPx()
                    else -> 0.8.dp.toPx()
                }

                val tickColor = when {
                    degree == 0 -> Color(0xFFEF4444) // North in Red
                    isCardinal -> GoldPrimary
                    isMajor -> Color.White.copy(alpha = 0.8f)
                    else -> Color.White.copy(alpha = 0.35f)
                }

                val angleRad = Math.toRadians((degree - 90).toDouble())
                val startX = center.x + (radius - tickLength) * cos(angleRad).toFloat()
                val startY = center.y + (radius - tickLength) * sin(angleRad).toFloat()
                val endX = center.x + radius * cos(angleRad).toFloat()
                val endY = center.y + radius * sin(angleRad).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // Draw Cardinal Arabic text labels: ش (North), ق (East), ج (South), غ (West)
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 15.sp.toPx()
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val cardinals = listOf(
                Pair(0, "ش"),  // North
                Pair(90, "ق"), // East
                Pair(180, "ج"),// South
                Pair(270, "غ") // West
            )

            for ((deg, label) in cardinals) {
                paint.color = if (deg == 0) android.graphics.Color.RED else android.graphics.Color.WHITE
                val angleRad = Math.toRadians((deg - 90).toDouble())
                val labelDistance = radius * 0.70f
                val textX = center.x + labelDistance * cos(angleRad).toFloat()
                val textY = center.y + labelDistance * sin(angleRad).toFloat() + 6.dp.toPx()

                drawContext.canvas.nativeCanvas.drawText(label, textX, textY, paint)
            }
        }

        // Qibla Target Arrow & Kaaba Medallion (Rotates with dial + qibla angle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(dialRotation + qiblaAngle),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Golden Kaaba Medallion pointing toward Qibla
                Image(
                    painter = painterResource(id = R.drawable.ic_kaaba_gold),
                    contentDescription = "الكعبة المشرفة",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldLight, CircleShape)
                        .shadow(6.dp, CircleShape)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Golden pointer triangle towards Kaaba
                Canvas(modifier = Modifier.size(16.dp, 16.dp)) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path, color = GoldLight)
                }
            }
        }

        // Fixed Device Pointer at Top (12 o'clock)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 2.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(modifier = Modifier.size(18.dp, 12.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, color = if (isAligned) SuccessGreen else GoldAccent)
            }
        }

        // Center Pivot Jewel / Medallion
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldLight,
                            GoldAccent
                        )
                    )
                )
                .border(2.dp, Color.White, CircleShape)
                .shadow(4.dp, CircleShape)
        )
    }
}

@Composable
private fun AlignmentStatusBadge(
    isAligned: Boolean,
    diffAngle: Int
) {
    val bgColor = if (isAligned) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isAligned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isAligned) 4.dp else 1.dp,
        modifier = Modifier.testTag("qibla_alignment_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (isAligned) GoldLight else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isAligned) {
                    "أنت الآن باتجاه القبلة تماماً ✓"
                } else {
                    if (diffAngle > 0) "أدر الهاتف يميناً بمقدار $diffAngle°" else "أدر الهاتف يساراً بمقدار ${abs(diffAngle)}°"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = textColor
            )
        }
    }
}

@Composable
private fun QiblaTipsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "إرشادات للحصول على أدق اتجاه:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• ضع الهاتف بشكل أفقي مستوٍ تماماً على راحة يدك أو على طاولة.\n" +
                        "• ابتعد عن الأجهزة الإلكترونية والأجسام المغناطيسية أو المعدنية.\n" +
                        "• عند اهتزاز الجهاز وتوهج الدائرة باللون الأخضر تكون متجهاً للقبلة.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
