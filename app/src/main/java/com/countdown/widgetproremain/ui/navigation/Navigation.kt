package com.countdown.widgetproremain.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.countdown.widgetproremain.ui.viewmodel.CountdownViewModel
import com.countdown.widgetproremain.ui.home.HomeScreen
import com.countdown.widgetproremain.ui.addedit.AddEditScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddEdit : Screen("add_edit")
    data object Import : Screen("import")
    data object Settings : Screen("settings")
}


@Composable
fun CountdownNavHost(
    navController: NavHostController,
    viewModel: CountdownViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onAddEventConfig = { navController.navigate(Screen.AddEdit.route) },
                onImportEvent = { navController.navigate(Screen.Import.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.AddEdit.route) {
            AddEditScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Import.route) {
            com.countdown.widgetproremain.ui.import.ImportEventScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(Screen.Settings.route) {
            com.countdown.widgetproremain.ui.settings.SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
