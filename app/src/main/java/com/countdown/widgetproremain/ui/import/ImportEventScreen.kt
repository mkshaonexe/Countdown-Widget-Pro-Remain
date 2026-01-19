package com.countdown.widgetproremain.ui.import

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.countdown.widgetproremain.data.model.CountdownEvent
import com.countdown.widgetproremain.data.repository.CalendarRepository
import com.countdown.widgetproremain.data.repository.SystemCalendarEvent
import com.countdown.widgetproremain.ui.viewmodel.CountdownViewModel
import com.countdown.widgetproremain.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportEventScreen(
    navController: NavController,
    viewModel: CountdownViewModel
) {
    val context = LocalContext.current
    val calendarRepository = remember { CalendarRepository(context) }
    var calendarEvents by remember { mutableStateOf<List<SystemCalendarEvent>>(emptyList()) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            hasPermission = isGranted
            if (isGranted) {
                coroutineScope.launch {
                    calendarEvents = calendarRepository.getUpcomingEvents()
                }
            }
        }
    )

    LaunchedEffect(key1 = hasPermission) {
        if (hasPermission) {
           calendarEvents = calendarRepository.getUpcomingEvents()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import from Calendar") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Calendar permission is needed to import events.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CALENDAR) }) {
                        Text("Grant Permission")
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(calendarEvents) { event ->
                    ListItem(
                        headlineContent = { Text(event.title) },
                        supportingContent = {
                            Text(DateUtils.formatTargetDate(event.dtStart))
                        },
                        modifier = Modifier.clickable {
                            // Import logic
                            val newEvent = CountdownEvent(
                                title = event.title,
                                targetDate = event.dtStart,
                                color = 0xFF4CAF50.toInt() // Default green color
                            )
                            viewModel.insert(newEvent)
                            navController.navigateUp()
                        }
                    )
                    Divider()
                }
                if (calendarEvents.isEmpty()) {
                    item {
                         Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                             Text("No upcoming events found in calendar.")
                         }
                    }
                }
            }
        }
    }
}
