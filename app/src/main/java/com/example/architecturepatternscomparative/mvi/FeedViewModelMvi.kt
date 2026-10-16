package com.example.architecturepatternscomparative.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.architecturepatternscomparative.core.LoadGenerator

class FeedViewModelMvi : ViewModel() {

    private val loadGenerator = LoadGenerator()
    private val reducer = FeedReducer()

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    init {
        startCollectingData()
    }

    private fun startCollectingData() {
        viewModelScope.launch {
            loadGenerator.startHeavyEmission()
                .collect { incomingItems ->
                    dispatch(FeedIntent.LoadItems(incomingItems))
                }
        }
    }

    private fun dispatch(intent: FeedIntent) {
        _state.value = reducer.reduce(_state.value, intent)
    }
}