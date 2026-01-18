package com.countdown.widgetproremain.ui.addedit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

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
    
    var includeTime by remember { mutableStateOf(false) }
    var isCountUp by remember { mutableStateOf(false) }
    var recurrence by remember { mutableStateOf("NONE") }
    val timePickerState = rememberTimePickerState()
    
    var recurrenceExpanded by remember { mutableStateOf(false) }
    val recurrenceOptions = listOf("NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY")

    val scrollState = rememberScrollState()

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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Count Up Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Count Up (Time Since)", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isCountUp,
                    onCheckedChange = { isCountUp = it }
                )
            }

            Divider()

            Text("Target Date", style = MaterialTheme.typography.titleMedium)
            // Note: DatePicker in a Column might take up a lot of space. 
            // Often best used in a Dialog, but keeping inline for now as requested or per previous design.
            // Using a shorter height for DatePicker if possible or just letting it scroll.
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                modifier = Modifier.height(350.dp) // Limit height
            )
            
            // Include Time Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Include Time", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = includeTime,
                    onCheckedChange = { includeTime = it }
                )
            }

            if (includeTime) {
                TimeInput(state = timePickerState)
            }

            Divider()

            // Recurrence Dropdown
            ExposedDropdownMenuBox(
                expanded = recurrenceExpanded,
                onExpandedChange = { recurrenceExpanded = !recurrenceExpanded }
            ) {
                OutlinedTextField(
                    value = recurrence,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Recurrence") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = recurrenceExpanded,
                    onDismissRequest = { recurrenceExpanded = false }
                ) {
                    recurrenceOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                recurrence = option
                                recurrenceExpanded = false
                            }
                        )
                    }
                }
            }

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
                            // Indicate selection if needed (e.g. checkmark)
                        }
                    }
                }
            }
            
             OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    val dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    
                    // Combine Date and Time
                    val zoneId = ZoneId.systemDefault()
                    val date = Instant.ofEpochMilli(dateMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    var dateTime = date.atStartOfDay()
                    
                    if (includeTime) {
                         dateTime = dateTime.withHour(timePickerState.hour).withMinute(timePickerState.minute)
                    }
                    
                    // Convert back to millis (System Default Zone for consistency with DateUtils logic or just UTC?)
                    // The DatePicker returns UTC midnight. 
                    // To be safe, let's treat the picked date as "User's local date".
                    val finalTargetMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    viewModel.addEvent(
                        title = title,
                        targetDate = finalTargetMillis,
                        isAllDay = !includeTime,
                        includeTime = includeTime,
                        isCountUp = isCountUp,
                        recurrence = recurrence,
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
