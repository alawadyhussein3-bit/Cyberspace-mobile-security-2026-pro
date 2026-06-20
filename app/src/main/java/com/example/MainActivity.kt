package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppHostNav()
            }
        }
    }
}

@Composable
fun MainAppHostNav() {
    val navController = rememberNavController()
    val viewModel: SecurityViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationTabs = listOf(
        NavigationTabItem(
            route = "app_dashboard",
            title = "SHIELD",
            icon = Icons.Default.Shield,
            tag = "tab_dashboard"
        ),
        NavigationTabItem(
            route = "app_checklist",
            title = "HARDEN",
            icon = Icons.Default.CheckCircle,
            tag = "tab_checklist"
        ),
        NavigationTabItem(
            route = "app_chat",
            title = "AI ADVISOR",
            icon = Icons.Default.BugReport,
            tag = "tab_chat"
        ),
        NavigationTabItem(
            route = "app_shield",
            title = "DNS SHIELD",
            icon = Icons.Default.Language,
            tag = "tab_shield"
        ),
        NavigationTabItem(
            route = "app_profile",
            title = "OPERATOR",
            icon = Icons.Default.Person,
            tag = "tab_profile"
        )
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg),
        bottomBar = {
            Column {
                HorizontalDivider(color = CyberGrid, thickness = 1.dp)
                NavigationBar(
                    containerColor = CyberSurface,
                    modifier = Modifier.height(80.dp),
                    tonalElevation = 0.dp
                ) {
                navigationTabs.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberBg,
                            selectedTextColor = CyberPrimary,
                            indicatorColor = CyberPrimary,
                            unselectedIconColor = CyberMuted,
                            unselectedTextColor = CyberMuted
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "app_dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("app_dashboard") {
                DashboardScreen(viewModel = viewModel)
            }
            composable("app_checklist") {
                HardeningChecklistScreen(viewModel = viewModel)
            }
            composable("app_chat") {
                ChatScreen(viewModel = viewModel)
            }
            composable("app_shield") {
                ShieldInspectScreen(viewModel = viewModel)
            }
            composable("app_profile") {
                ProfileScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavigationTabItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val tag: String
)
