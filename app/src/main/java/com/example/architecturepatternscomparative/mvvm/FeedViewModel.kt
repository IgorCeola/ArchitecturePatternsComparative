package com.example.architecturepatternscomparative.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.architecturepatternscomparative.core.FeedItem
import com.example.architecturepatternscomparative.core.LoadGenerator

class FeedViewModel : ViewModel() {

    private val loadGenerator = LoadGenerator()

    private val _items = MutableStateFlow<List<FeedItem>>(emptyList())
    val items: StateFlow<List<FeedItem>> = _items.asStateFlow()

    init {
        startCollectingData()
    }

    private fun startCollectingData() {
        viewModelScope.launch {
            loadGenerator.startHeavyEmission()
                .collect { incomingItems ->
                    _items.value = incomingItems
                }
        }
    }
}