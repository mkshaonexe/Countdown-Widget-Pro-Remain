package com.countdown.widgetproremain.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.countdown.widgetproremain.data.model.CountdownEvent
import com.countdown.widgetproremain.data.repository.CountdownRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode {
    SOONEST,
    ALPHABETICAL,
    CREATED_DATE
}

class CountdownViewModel(private val repository: CountdownRepository) : ViewModel() {

    val allEvents: StateFlow<List<CountdownEvent>> = repository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortMode = MutableStateFlow(SortMode.SOONEST)
    val sortMode: StateFlow<SortMode> = _sortMode

    // Filtered and sorted events based on search query and sort mode
    val filteredAndSortedEvents: StateFlow<List<CountdownEvent>> = combine(
        allEvents,
        searchQuery,
        sortMode
    ) { events, query, mode ->
        // First filter by search query
        val filtered = if (query.isBlank()) {
            events
        } else {
            events.filter { event ->
                event.title.contains(query, ignoreCase = true)
            }
        }

        // Then sort based on selected mode
        when (mode) {
            SortMode.SOONEST -> filtered.sortedBy { it.targetDate }
            SortMode.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
            SortMode.CREATED_DATE -> filtered.sortedByDescending { it.createdAt }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun addEvent(
        title: String,
        targetDate: Long,
        isAllDay: Boolean,
        includeTime: Boolean,
        isCountUp: Boolean,
        recurrence: String,
        color: Int,
        notes: String?,
        isPinned: Boolean
    ) {
        viewModelScope.launch {
            repository.insertEvent(
                CountdownEvent(
                    title = title,
                    targetDate = targetDate,
                    isAllDay = isAllDay,
                    includeTime = includeTime,
                    isCountUp = isCountUp,
                    recurrence = recurrence,
                    color = color,
                    notes = notes,
                    isPinned = isPinned
                )
            )
        }
    }
    
    fun updateEvent(event: CountdownEvent) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun insert(event: CountdownEvent) {
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }

    fun deleteEvent(event: CountdownEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }
}

class CountdownViewModelFactory(private val repository: CountdownRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CountdownViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CountdownViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
