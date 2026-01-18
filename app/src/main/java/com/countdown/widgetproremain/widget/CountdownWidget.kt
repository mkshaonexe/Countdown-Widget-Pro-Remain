package com.countdown.widgetproremain.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.countdown.widgetproremain.CountdownApplication
import com.countdown.widgetproremain.data.model.CountdownEvent
import com.countdown.widgetproremain.util.DateUtils
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.state.PreferencesGlanceStateDefinition


class CountdownWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            SMALL_SQUARE, // 1x1
            HORIZONTAL_RECTANGLE, // 4x1 or 2x1
            BIG_SQUARE, // 2x2
            LIST_MODE // 4x3+
        )
    )

    companion object {
        val SMALL_SQUARE = DpSize(50.dp, 50.dp)
        val HORIZONTAL_RECTANGLE = DpSize(150.dp, 50.dp)
        val BIG_SQUARE = DpSize(100.dp, 100.dp)
        val LIST_MODE = DpSize(100.dp, 180.dp)
        
        val PREF_EVENT_ID = intPreferencesKey("event_id")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as CountdownApplication).repository
        // Fetch all events outside provideContent (suspend function)
        val events = repository.allEvents.firstOrNull() ?: emptyList()
        
        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val eventId = prefs[PREF_EVENT_ID]
            
            val selectedEvent = if (eventId != null) {
                events.find { it.id == eventId }
            } else {
                // Default to nearest or first
                 events.sortedBy { it.targetDate }.firstOrNull()
            }

            GlanceTheme {
                WidgetContent(events = events, selectedEvent = selectedEvent)
            }
        }
    }

    @Composable
    fun WidgetContent(events: List<CountdownEvent>, selectedEvent: CountdownEvent?) {
        val size = LocalSize.current
        
        // Size logic
        val isSmall = size.width < 100.dp && size.height < 100.dp
        val isList = size.height >= 180.dp
        
        // Background color based on selected event (or neutral if list/empty)
        val backgroundColor = if (selectedEvent != null && !isList) {
            getUrgencyColor(selectedEvent.targetDate)
        } else if (isList && events.isNotEmpty()) {
             Color(0xFF263238) // Darker background for list
        } else {
            Color.DarkGray
        }

        Box(
            modifier = androidx.glance.GlanceModifier
                .fillMaxSize()
                .background(backgroundColor.copy(alpha = 0.9f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isList) {
                // List Layout (Full Grid) - Shows ALL events still? 
                // Using "Full Grid (4x3, 4x4): Multiple events in one widget" from PRD.
                // So List mode should probably ignore the specific selection or default to list.
                // If the user picked a specific event but resized to list, maybe highlight it? 
                // For now, let's keep List showing all.
                if (events.isEmpty()) {
                     Text(
                        text = "No Events",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp)
                    )
                } else {
                    val sortedEvents = events.sortedBy { it.targetDate }
                    LazyColumn(modifier = androidx.glance.GlanceModifier.fillMaxSize()) {
                        items(sortedEvents) { event ->
                             Column(
                                modifier = androidx.glance.GlanceModifier.padding(vertical = 4.dp).fillMaxSize()
                            ) {
                                Text(
                                    text = event.title,
                                    style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp)
                                )
                                Text(
                                    text = DateUtils.getTimeRemaining(event),
                                    style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 12.sp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Single Event Modes
                if (selectedEvent == null) {
                    Text(
                        text = "No Event",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp)
                    )
                } else {
                    if (isSmall) {
                        // 1x1 Layout: Minimal info (Time only)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = DateUtils.getTimeRemaining(selectedEvent).split(" ").firstOrNull() ?: "0",
                                style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp)
                            )
                        }
                    } else {
                        // 4x1 / 2x2 Layout: Title + Time
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = selectedEvent.title,
                                style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp)
                            )
                            Text(
                                text = DateUtils.getTimeRemaining(selectedEvent),
                                style = TextStyle(color = ColorProvider(Color.White), fontSize = 24.sp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getUrgencyColor(targetDate: Long): Color {
        val now = System.currentTimeMillis()
        val diff = targetDate - now
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            diff < 0 -> Color.Gray // Past
            days < 1 -> Color(0xFFE53935) // Red (Urgent - < 24h)
            days < 7 -> Color(0xFFFFB300) // Amber (Warning - < 1 week)
            else -> Color(0xFF43A047) // Green (Safe - > 1 week)
        }
    }
}
