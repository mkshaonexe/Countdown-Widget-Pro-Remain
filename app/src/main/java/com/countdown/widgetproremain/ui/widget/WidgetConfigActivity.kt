package com.countdown.widgetproremain.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.countdown.widgetproremain.CountdownApplication
import com.countdown.widgetproremain.data.model.CountdownEvent
import com.countdown.widgetproremain.widget.CountdownWidget
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Find the widget ID from the intent
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        setContent {
            WidgetConfigScreen(
                onEventSelected = { event, showSeconds ->
                    saveWidgetState(event, showSeconds)
                }
            )
        }
    }

    private fun saveWidgetState(event: CountdownEvent, showSeconds: Boolean) {
        val context = this
        val coroutineScope = lifecycleScope
        
        coroutineScope.launch {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
            
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[CountdownWidget.PREF_EVENT_ID] = event.id
                    this[CountdownWidget.PREF_SHOW_SECONDS] = showSeconds
                }
            }
            CountdownWidget().update(context, glanceId)
            
            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(onEventSelected: (CountdownEvent, Boolean) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = (context.applicationContext as CountdownApplication).repository
    val events by repository.allEvents.collectAsState(initial = emptyList())
    var showSeconds by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Select Event for Widget") }) },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Show Seconds (Precise Mode)", modifier = Modifier.weight(1f))
                    Switch(checked = showSeconds, onCheckedChange = { showSeconds = it })
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(events) { event ->
                ListItem(
                    headlineContent = { Text(event.title) },
                    supportingContent = { 
                        Text(com.countdown.widgetproremain.util.DateUtils.formatTargetDate(event.targetDate)) 
                    },
                    modifier = Modifier.clickable { onEventSelected(event, showSeconds) }
                )
                Divider()
            }
            if (events.isEmpty()) {
                item {
                    Text(
                        "No events found. Please create an event in the app first.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
