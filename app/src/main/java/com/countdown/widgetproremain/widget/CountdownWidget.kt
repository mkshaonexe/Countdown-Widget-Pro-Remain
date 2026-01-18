package com.countdown.widgetproremain.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countdown.widgetproremain.CountdownApplication
import com.countdown.widgetproremain.data.model.CountdownEvent
import com.countdown.widgetproremain.util.DateUtils
import kotlinx.coroutines.flow.firstOrNull

class CountdownWidget : GlanceAppWidget() {

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
        Box(
            modifier = androidx.glance.GlanceModifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (event != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = event.title,
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp)
                    )
                    Text(
                        text = DateUtils.getTimeRemaining(event.targetDate),
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 24.sp)
                    )
                }
            } else {
                Text(
                    text = "No Events",
                    style = TextStyle(color = ColorProvider(Color.White))
                )
            }
        }
    }
}
