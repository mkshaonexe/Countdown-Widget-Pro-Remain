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
    
    // Backup/Restore functionality
    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState
    
    /**
     * Export all events to JSON string for backup
     * @return JSON string containing all events
     */
    suspend fun exportToBackup(): Result<String> {
        return try {
            _backupState.value = BackupState.Exporting
            val events = repository.exportAllEvents()
            val jsonString = com.countdown.widgetproremain.util.BackupManager.exportToJson(events)
            _backupState.value = BackupState.ExportSuccess(events.size)
            Result.success(jsonString)
        } catch (e: Exception) {
            _backupState.value = BackupState.Error(e.message ?: "Export failed")
            Result.failure(e)
        }
    }
    
    /**
     * Import events from JSON backup string
     * @param jsonString JSON backup string
     * @param clearExisting If true, deletes all existing events before import
     * @return Result with count of imported events or error
     */
    fun importFromBackup(jsonString: String, clearExisting: Boolean = false) {
        viewModelScope.launch {
            try {
                _backupState.value = BackupState.Importing
                val result = com.countdown.widgetproremain.util.BackupManager.importFromJson(jsonString)
                
                result.onSuccess { events ->
                    val importedCount = repository.importEvents(events, clearExisting)
                    _backupState.value = BackupState.ImportSuccess(importedCount)
                }.onFailure { error ->
                    _backupState.value = BackupState.Error(error.message ?: "Import failed")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Import failed")
            }
        }
    }
    
    /**
     * Reset backup state to idle
     */
    fun resetBackupState() {
        _backupState.value = BackupState.Idle
    }
}

/**
 * Sealed class representing backup/restore states
 */
sealed class BackupState {
    object Idle : BackupState()
    object Exporting : BackupState()
    data class ExportSuccess(val eventCount: Int) : BackupState()
    object Importing : BackupState()
    data class ImportSuccess(val eventCount: Int) : BackupState()
    data class Error(val message: String) : BackupState()
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
