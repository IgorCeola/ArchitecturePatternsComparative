package com.example.architecturepatternscomparative.mvi

import com.example.architecturepatternscomparative.core.data.FeedItem
import com.example.architecturepatternscomparative.core.data.HeaderInfo

data class FeedUiState(
    val items: List<FeedItem> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val header: HeaderInfo = HeaderInfo(tick = 0)
)

sealed interface FeedIntent {
    data class ItemsLoaded(val items: List<FeedItem>) : FeedIntent
    data class ItemTick(val tick: Int) : FeedIntent
    data class HeaderTick(val tick: Int) : FeedIntent
    data class ToggleFavorite(val id: String) : FeedIntent
}