package com.countdown.widgetproremain.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
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

class CountdownWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            SMALL_SQUARE, // 1x1
            HORIZONTAL_RECTANGLE, // 4x1 or 2x1
            BIG_SQUARE // 4x2+
        )
    )

    companion object {
        val SMALL_SQUARE = DpSize(50.dp, 50.dp)
        val HORIZONTAL_RECTANGLE = DpSize(100.dp, 50.dp)
        val BIG_SQUARE = DpSize(100.dp, 100.dp)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as CountdownApplication).repository
        // Get the first event for now (simplification)
        val events = repository.allEvents.firstOrNull() ?: emptyList()
        val nearestEvent = events.minByOrNull { it.targetDate }

        provideContent {
            GlanceTheme {
                WidgetContent(event = nearestEvent)
            }
        }
    }

    @Composable
    fun WidgetContent(event: CountdownEvent?) {
        val size = LocalSize.current
        val isSmall = size.width < 100.dp && size.height < 100.dp
        val isHorizontal = size.width >= 100.dp && size.height < 100.dp

        val backgroundColor = if (event != null) {
            getUrgencyColor(event.targetDate)
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
            if (event != null) {
                if (isSmall) {
                    // 1x1 Layout: Minimal info
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = DateUtils.getTimeRemaining(event.targetDate).split(" ").firstOrNull() ?: "0",
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp)
                        )
                    }
                } else if (isHorizontal) {
                    // 4x1 / 2x1 Layout: Title + Time
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = event.title,
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp)
                        )
                        Text(
                            text = DateUtils.getTimeRemaining(event.targetDate),
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp)
                        )
                    }
                } else {
                    // Default / Large Layout
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = event.title,
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp)
                        )
                        Text(
                            text = DateUtils.getTimeRemaining(event.targetDate),
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 24.sp)
                        )
                    }
                }
            } else {
                Text(
                    text = "No Events",
                    style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp)
                )
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
