package com.countdown.widgetproremain.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countdown.widgetproremain.data.model.CountdownEvent
import com.countdown.widgetproremain.ui.viewmodel.CountdownViewModel
import com.countdown.widgetproremain.ui.viewmodel.SortMode
import com.countdown.widgetproremain.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CountdownViewModel,
    onAddEventConfig: () -> Unit
) {
    val events by viewModel.filteredAndSortedEvents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Countdowns") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        IconButton(onClick = { showSearchBar = !showSearchBar }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = !showSortMenu }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Soonest") },
                                    onClick = {
                                        viewModel.updateSortMode(SortMode.SOONEST)
                                        showSortMenu = false
                                    },
                                    leadingIcon = if (sortMode == SortMode.SOONEST) {
                                        { Text("✓") }
                                    } else null
                                )
                                DropdownMenuItem(
                                    text = { Text("Alphabetical") },
                                    onClick = {
                                        viewModel.updateSortMode(SortMode.ALPHABETICAL)
                                        showSortMenu = false
                                    },
                                    leadingIcon = if (sortMode == SortMode.ALPHABETICAL) {
                                        { Text("✓") }
                                    } else null
                                )
                                DropdownMenuItem(
                                    text = { Text("Created Date") },
                                    onClick = {
                                        viewModel.updateSortMode(SortMode.CREATED_DATE)
                                        showSortMenu = false
                                    },
                                    leadingIcon = if (sortMode == SortMode.CREATED_DATE) {
                                        { Text("✓") }
                                    } else null
                                )
                            }
                        }
                    }
                )
                if (showSearchBar) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search events...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEventConfig) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { innerPadding ->

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No countdowns yet. Add one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    CountdownItem(
                        event = event,
                        onDelete = { viewModel.deleteEvent(event) }
                    )
                }
            }
        }
    }
}

@Composable
fun CountdownItem(event: CountdownEvent, onDelete: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(event.color))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatTargetDate(event.targetDate),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateUtils.getTimeRemaining(event),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
