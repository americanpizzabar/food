package com.americanpizzabar.food.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.americanpizzabar.food.presentation.screen.calendar.CalendarScreen
import com.americanpizzabar.food.presentation.screen.home.HomeScreen
import com.americanpizzabar.food.presentation.screen.journal.JournalScreen
import com.americanpizzabar.food.presentation.screen.journal.JournalDetailScreen
import com.americanpizzabar.food.presentation.screen.pantry.PantryScreen
import com.americanpizzabar.food.presentation.screen.photoanalysis.PhotoAnalysisScreen
import com.americanpizzabar.food.presentation.screen.profile.ProfileScreen
import com.americanpizzabar.food.presentation.screen.recipesuggestion.RecipeSuggestionScreen
import com.americanpizzabar.food.presentation.screen.shopping.ShoppingScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "ホーム", Icons.Default.Home)
    object PhotoAnalysis : Screen("photo_analysis", "写真解析", Icons.Default.CameraAlt)
    object Suggestion : Screen("suggestion", "提案", Icons.Default.AutoAwesome)
    object Journal : Screen("journal", "日記", Icons.Default.MenuBook)
    object Calendar : Screen("calendar", "カレンダー", Icons.Default.CalendarMonth)
    object Shopping : Screen("shopping", "買い物", Icons.Default.ShoppingCart)
    object Pantry : Screen("pantry", "在庫", Icons.Default.Kitchen)
    object Profile : Screen("profile", "設定", Icons.Default.Person)
    object JournalDetail : Screen("journal/{entryId}", "日記詳細", Icons.Default.MenuBook)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.PhotoAnalysis,
    Screen.Suggestion,
    Screen.Journal,
    Screen.Calendar
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any {
        currentDestination?.hierarchy?.any { dest -> dest.route == it.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToPhotoAnalysis = { navController.navigate(Screen.PhotoAnalysis.route) },
                    onNavigateToSuggestion = { navController.navigate(Screen.Suggestion.route) },
                    onNavigateToJournal = { navController.navigate(Screen.Journal.route) },
                    onNavigateToShopping = { navController.navigate(Screen.Shopping.route) },
                    onNavigateToPantry = { navController.navigate(Screen.Pantry.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.PhotoAnalysis.route) {
                PhotoAnalysisScreen(onNavigateUp = { navController.navigateUp() })
            }
            composable(Screen.Suggestion.route) {
                RecipeSuggestionScreen(onNavigateUp = { navController.navigateUp() })
            }
            composable(Screen.Journal.route) {
                JournalScreen(
                    onNavigateToDetail = { id -> navController.navigate("journal/$id") },
                    onNavigateUp = { navController.navigateUp() }
                )
            }
            composable("journal/{entryId}") { backStack ->
                val entryId = backStack.arguments?.getString("entryId")?.toLongOrNull() ?: 0L
                JournalDetailScreen(entryId = entryId, onNavigateUp = { navController.navigateUp() })
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(onNavigateUp = { navController.navigateUp() })
            }
            composable(Screen.Shopping.route) {
                ShoppingScreen(onNavigateUp = { navController.navigateUp() })
            }
            composable(Screen.Pantry.route) {
                PantryScreen(onNavigateUp = { navController.navigateUp() })
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onNavigateUp = { navController.navigateUp() })
            }
        }
    }
}
