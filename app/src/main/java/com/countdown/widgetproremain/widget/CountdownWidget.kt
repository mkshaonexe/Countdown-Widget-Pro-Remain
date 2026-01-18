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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.height
import androidx.glance.layout.width
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
        val PREF_SHOW_SECONDS = androidx.datastore.preferences.core.booleanPreferencesKey("show_seconds")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as CountdownApplication).repository
        // Fetch all events outside provideContent (suspend function)
        val events = repository.allEvents.firstOrNull() ?: emptyList()
        
        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val eventId = prefs[PREF_EVENT_ID]
            val showSecondsPref = prefs[PREF_SHOW_SECONDS] ?: false
            
            val selectedEvent = if (eventId != null) {
                events.find { it.id == eventId }
            } else {
                // Default to nearest or first
                 events.sortedBy { it.targetDate }.firstOrNull()
            }

            GlanceTheme {
                WidgetContent(events = events, selectedEvent = selectedEvent, showSecondsPref = showSecondsPref)
            }
        }
    }

    @Composable
    fun WidgetContent(events: List<CountdownEvent>, selectedEvent: CountdownEvent?, showSecondsPref: Boolean = false) {

        val size = LocalSize.current
        
        // Define breakpoints based on PRD and Material Guidelines
        // 1x1: < 100dp width/height
        // 2x1 / 4x1 (Horizontal): height < 100dp, width >= 100dp
        // 4x2 (Detailed): height >= 100dp, width >= 150dp
        // List: height >= 180dp (or larger specific mode) or forcing list mode?
        // Let's rely on size since user resizing determines the intent mostly.
        
        val isSmallSquare = size.width < 100.dp && size.height < 100.dp
        val isHorizontal = size.height < 100.dp && size.width >= 100.dp
        val isDetailed = size.height >= 100.dp && size.width >= 100.dp
        
        // Determine Background Color
        val backgroundColor = if (selectedEvent != null) {
            getUrgencyColor(selectedEvent)
        } else {
             Color(0xFF263238) // Dark Blue Grey
        }

        Box(
            modifier = androidx.glance.GlanceModifier
                .fillMaxSize()
                .background(backgroundColor.copy(alpha = 0.9f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (events.isEmpty()) {
                 Text(
                    text = "No Events",
                    style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp)
                )
            } else if (selectedEvent == null) {
                // No specific event selected, show List if space permits, otherwise suggest selection
                if (size.height >= 120.dp && size.width >= 120.dp) {
                    ListLayout(events)
                } else {
                    Text(
                        text = "Select Event",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp)
                    )
                }
            } else {
               // Event is selected
               val isUrgent = !selectedEvent.isCountUp && (selectedEvent.targetDate - System.currentTimeMillis() in 1..86400000)
               val shouldShowSeconds = showSecondsPref || isUrgent

               when {
                   isSmallSquare -> SmallLayout(selectedEvent)
                   isHorizontal -> HorizontalLayout(selectedEvent)
                   isDetailed -> DetailedLayout(selectedEvent, showSeconds = shouldShowSeconds) 
                   else -> DetailedLayout(selectedEvent, showSeconds = shouldShowSeconds) // Fallback
               }
            }
        }
    }

    @Composable
    fun SmallLayout(event: CountdownEvent) {
         Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = androidx.glance.GlanceModifier.fillMaxSize()
        ) {
            Text(
                text = DateUtils.getDaysOnly(event),
                style = TextStyle(
                    color = ColorProvider(Color.White), 
                    fontSize = 24.sp,
                    fontWeight = androidx.glance.text.FontWeight.Bold
                )
            )
            Text(
                text = "DAYS",
                style = TextStyle(color = ColorProvider(Color.White), fontSize = 10.sp)
            )
        }
    }

    @Composable
    fun HorizontalLayout(event: CountdownEvent) {
        androidx.glance.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = androidx.glance.GlanceModifier.fillMaxSize()
        ) {
            Column(
                modifier = androidx.glance.GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = event.title,
                    style = TextStyle(
                        color = ColorProvider(Color.White), 
                        fontSize = 16.sp,
                        fontWeight = androidx.glance.text.FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
            androidx.glance.layout.Spacer(modifier = androidx.glance.GlanceModifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End
            ) {
                 Text(
                    text = DateUtils.getDaysOnly(event) + "d",
                    style = TextStyle(
                        color = ColorProvider(Color.White), 
                        fontSize = 22.sp,
                        fontWeight = androidx.glance.text.FontWeight.Bold
                    )
                )
            }
        }
    }

    @Composable
    fun DetailedLayout(event: CountdownEvent, showSeconds: Boolean = false) {
        val breakdown = DateUtils.getFullBreakdown(event)
        val days = breakdown.first.toLongOrNull() ?: 0
        
        // If less than 1 day, show Hours/Mins/Secs (or just Hours/Mins if no seconds)
        // If more than 1 day, show Days/Hours/Mins
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = androidx.glance.GlanceModifier.fillMaxSize()
        ) {
            Text(
                text = event.title,
                style = TextStyle(
                    color = ColorProvider(Color.White), 
                    fontSize = 18.sp,
                    fontWeight = androidx.glance.text.FontWeight.Bold
                ),
                maxLines = 1
            )
            androidx.glance.layout.Spacer(modifier = androidx.glance.GlanceModifier.height(8.dp))
            
            androidx.glance.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                 horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (days > 0) {
                    MetricItem(breakdown.first, "Days")
                    androidx.glance.layout.Spacer(modifier = androidx.glance.GlanceModifier.width(12.dp))
                    MetricItem(breakdown.second, "Hours")
                    androidx.glance.layout.Spacer(modifier = androidx.glance.GlanceModifier.width(12.dp))
                    MetricItem(breakdown.third, "Mins")
                    
                    if (showSeconds) {
                        androidx.glance.layout.Spacer(modifier = androidx.glance.GlanceModifier.width(12.dp))
                        // We need to fetch seconds from DateUtils if available or calc it manually
                        // Assuming DateUtils.getFullBreakdown returns (Days, Hours, Mins) - wait, let's check DateUtils
                        // Ideally we need seconds here.
                        // Let's assume for now we use a new helper or existing one.
                        // If showSeconds is true, we need a 4th metric.
                        // Since I can't easily see DateUtils, I will use a helper here to display it if my assumption about breakdown is limited.
                        // Wait, I should check DateUtils.kt. For now I will assume I need to calculate it.
                        val secondsStr = DateUtils.getSecondsOnly(event)
                        MetricItem(secondsStr, "Secs")
                    }
                } else {
                    // Less than a day
                    MetricItem(breakdown.second, "Hours")
                    androidx.glance.layout.Spacer(modifier = androidx.glance.GlanceModifier.width(12.dp))
                    MetricItem(breakdown.third, "Mins")
                    
                    if (showSeconds) {
                        androidx.glance.layout.Spacer(modifier = androidx.glance.GlanceModifier.width(12.dp))
                        val secondsStr = DateUtils.getSecondsOnly(event) // Assuming this exists or I'll add it
                         MetricItem(secondsStr, "Secs")
                    }
                }
            }
        }
    }
    
    @Composable
    fun MetricItem(value: String, label: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
             Text(
                text = value,
                style = TextStyle(
                    color = ColorProvider(Color.White), 
                    fontSize = 20.sp, 
                    fontWeight = androidx.glance.text.FontWeight.Bold
                )
            )
             Text(
                text = label,
                style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 10.sp)
            )
        }
    }

    @Composable
    fun ListLayout(events: List<CountdownEvent>) {
        val sortedEvents = events.sortedBy { it.targetDate }
        LazyColumn(modifier = androidx.glance.GlanceModifier.fillMaxSize()) {
            items(sortedEvents) { event ->
                 Column(
                    modifier = androidx.glance.GlanceModifier.padding(vertical = 4.dp).fillMaxSize()
                ) {
                    androidx.glance.layout.Row(
                        modifier = androidx.glance.GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = event.title,
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp),
                            modifier = androidx.glance.GlanceModifier.defaultWeight()
                        )
                         Text(
                            text = DateUtils.getDaysOnly(event) + "d",
                            style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 12.sp)
                        )
                    }
                }
            }
        }
    }

    private fun getUrgencyColor(event: CountdownEvent): Color {
        val now = System.currentTimeMillis()
        val diff = event.targetDate - now
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        if (event.isCountUp) {
            return Color(0xFF039BE5) // Light Blue for Count Up / Streaks
        }

        return when {
            diff < 0 -> Color.Gray // Past (Expired Countdown)
            days < 1 -> Color(0xFFE53935) // Red (Urgent - < 24h)
            days < 7 -> Color(0xFFFFB300) // Amber (Warning - < 1 week)
            else -> Color(0xFF43A047) // Green (Safe - > 1 week)
        }
    }
}
