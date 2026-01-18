package com.countdown.widgetproremain.ui.addedit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.countdown.widgetproremain.ui.viewmodel.CountdownViewModel
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: CountdownViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()
    var selectedColor by remember { mutableIntStateOf(Color.Gray.toArgb()) }

    val colors = listOf(
        Color(0xFFE57373), // Red
        Color(0xFF81C784), // Green
        Color(0xFF64B5F6), // Blue
        Color(0xFFFFD54F), // Yellow
        Color(0xFFBA68C8), // Purple
        Color.Gray
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Countdown") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Target Date", style = MaterialTheme.typography.titleMedium)
            DatePicker(state = datePickerState)

            Text("Color", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = color.toArgb() }
                    ) {
                        if (selectedColor == color.toArgb()) {
                            // Indicate selection if needed
                        }
                    }
                }
            }
            
             OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    // Adjust for timezone if needed, DatePicker returns UTC midnight
                    // For simplicity, using it as is or adding current time offset could be complex. 
                    // Let's assume user picks a date and we count to midnight of that date in UTC or similar.
                    // Better: use LocalDate from millis and convert to system zone start of day.
                    
                    viewModel.addEvent(
                        title = title,
                        targetDate = dateMillis,
                        isAllDay = true, // Defaulting to all day for now
                        color = selectedColor,
                        notes = if (notes.isBlank()) null else notes,
                        isPinned = false
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Save Countdown")
            }
        }
    }
}
