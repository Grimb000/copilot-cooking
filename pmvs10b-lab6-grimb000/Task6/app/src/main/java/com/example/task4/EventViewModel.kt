package com.example.task6

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository
    val allEvents: LiveData<List<Event>>

    init {
        val dao = EventDatabase.getDatabase(application).eventDao()
        repository = EventRepository(dao)
        allEvents = repository.getAllEvents()
    }

    fun insertEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertEvent(event)
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(event)
        }
    }

    fun clearEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearEvents()
        }
    }
}
