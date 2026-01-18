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
            getUrgencyColor(selectedEvent.targetDate)
        } else {
             Color(0xFF263238)
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
               when {
                   isSmallSquare -> SmallLayout(selectedEvent)
                   isHorizontal -> HorizontalLayout(selectedEvent)
                   isDetailed -> DetailedLayout(selectedEvent)
                   else -> DetailedLayout(selectedEvent) // Fallback
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
    fun DetailedLayout(event: CountdownEvent) {
        val breakdown = DateUtils.getFullBreakdown(event)
        
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
                MetricItem(breakdown.first, "Days")
                androidx.glance.layout.Spacer(modifier = androidx.glance.GlanceModifier.width(12.dp))
                MetricItem(breakdown.second, "Hours")
                androidx.glance.layout.Spacer(modifier = androidx.glance.GlanceModifier.width(12.dp))
                MetricItem(breakdown.third, "Mins")
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
