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
                onAddEventConfig = { navController.navigate(Screen.AddEdit.route) }
            )
        }
        composable(Screen.AddEdit.route) {
            AddEditScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
