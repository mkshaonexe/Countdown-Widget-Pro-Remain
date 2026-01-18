package com.countdown.widgetproremain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.countdown.widgetproremain.ui.theme.CountdownWidgetProRemainTheme


import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.countdown.widgetproremain.ui.navigation.CountdownNavHost
import com.countdown.widgetproremain.ui.viewmodel.CountdownViewModel
import com.countdown.widgetproremain.ui.viewmodel.CountdownViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as CountdownApplication)
        
        setContent {
            CountdownWidgetProRemainTheme {
                val navController = rememberNavController()
                val viewModel: CountdownViewModel = viewModel(
                    factory = CountdownViewModelFactory(appContainer.repository)
                )
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CountdownNavHost(
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}