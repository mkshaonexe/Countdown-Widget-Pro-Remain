package com.countdown.widgetproremain.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.countdown.widgetproremain.data.model.CountdownEvent
import com.countdown.widgetproremain.data.repository.CountdownRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CountdownViewModel(private val repository: CountdownRepository) : ViewModel() {

    val allEvents: StateFlow<List<CountdownEvent>> = repository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
