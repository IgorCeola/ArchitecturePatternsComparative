package com.example.architecturepatternscomparative.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.tracing.trace
import com.example.architecturepatternscomparative.core.data.FeedDataSource
import com.example.architecturepatternscomparative.core.data.STATE_UPDATE_SECTION
import com.example.architecturepatternscomparative.core.data.Scenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModelMvi(private val scenario: Scenario) : ViewModel() {
    private val intents = Channel<FeedIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            for (intent in intents) {
                trace(STATE_UPDATE_SECTION) {
                    _state.value = FeedReducer.reduce(_state.value, intent)
                }
            }
        }
        viewModelScope.launch(Dispatchers.Default) {
            onIntent(FeedIntent.ItemsLoaded(FeedDataSource.initialItems()))
            when (scenario) {
                Scenario.S2_ITEM_UPDATES ->
                    FeedDataSource.ticks(FeedDataSource.ITEM_TICK_MS)
                        .collect { onIntent(FeedIntent.ItemTick(it)) }

                Scenario.S4_AUX_STATE ->
                    FeedDataSource.ticks(FeedDataSource.HEADER_TICK_MS)
                        .collect { onIntent(FeedIntent.HeaderTick(it)) }

                else -> Unit
            }
        }
    }

    fun onIntent(intent: FeedIntent) {
        intents.trySend(intent)
    }
}