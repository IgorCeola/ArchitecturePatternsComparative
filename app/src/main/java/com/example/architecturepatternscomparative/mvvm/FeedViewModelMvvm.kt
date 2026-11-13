package com.example.architecturepatternscomparative.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.tracing.trace
import com.example.architecturepatternscomparative.core.data.FeedDataSource
import com.example.architecturepatternscomparative.core.data.FeedItem
import com.example.architecturepatternscomparative.core.data.HeaderInfo
import com.example.architecturepatternscomparative.core.data.STATE_UPDATE_SECTION
import com.example.architecturepatternscomparative.core.data.Scenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModelMvvm(private val scenario: Scenario) : ViewModel() {

    private val _items = MutableStateFlow<List<FeedItem>>(emptyList())
    val items: StateFlow<List<FeedItem>> = _items.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _header = MutableStateFlow(HeaderInfo(tick = 0))
    val header: StateFlow<HeaderInfo> = _header.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val initial = FeedDataSource.initialItems()
            trace(STATE_UPDATE_SECTION) { _items.value = initial }

            when (scenario) {
                Scenario.S2_ITEM_UPDATES ->
                    FeedDataSource.ticks(FeedDataSource.ITEM_TICK_MS).collect { tick ->
                        trace(STATE_UPDATE_SECTION) {
                            _items.update { FeedDataSource.applyLikeTick(it, tick) }
                        }
                    }

                Scenario.S4_AUX_STATE ->
                    FeedDataSource.ticks(FeedDataSource.HEADER_TICK_MS).collect { tick ->
                        trace(STATE_UPDATE_SECTION) { _header.value = HeaderInfo(tick) }
                    }

                else -> Unit
            }
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            trace(STATE_UPDATE_SECTION) {
                _favoriteIds.update { if (id in it) it - id else it + id }
            }
        }
    }
}