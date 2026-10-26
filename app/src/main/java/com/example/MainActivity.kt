package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NoorScreen
import com.example.ui.NoorViewModel
import com.example.ui.screens.AzkarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NoorAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NoorViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            NoorAppTheme(darkTheme = settings.isDarkTheme) {
                // Enforce RTL Layout Direction for authentic Arabic experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    NoorMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

data class NavigationTabItem(
    val screen: NoorScreen,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun NoorMainApp(viewModel: NoorViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back button to return to Home screen when on other screens
    if (currentScreen != NoorScreen.HOME) {
        BackHandler {
            viewModel.setScreen(NoorScreen.HOME)
        }
    }

    val navItems = listOf(
        NavigationTabItem(NoorScreen.HOME, Icons.Default.Home, "nav_home"),
        NavigationTabItem(NoorScreen.QIBLA, Icons.Default.Explore, "nav_qibla"),
        NavigationTabItem(NoorScreen.AZKAR, Icons.Default.AutoAwesome, "nav_azkar"),
        NavigationTabItem(NoorScreen.SETTINGS, Icons.Default.Settings, "nav_settings")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                navItems.forEach { item ->
                    val isSelected = (currentScreen == item.screen)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setScreen(item.screen) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.screen.titleArabic
                            )
                        },
                        label = {
                            Text(
                                text = item.screen.titleArabic,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldDeep,
                            selectedTextColor = EmeraldMedium,
                            indicatorColor = GoldPrimary.copy(alpha = 0.25f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screenTransition"
        ) { screen ->
            when (screen) {
                NoorScreen.HOME -> HomeScreen(viewModel = viewModel)
                NoorScreen.QIBLA -> QiblaScreen(viewModel = viewModel)
                NoorScreen.AZKAR -> AzkarScreen(viewModel = viewModel)
                NoorScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
