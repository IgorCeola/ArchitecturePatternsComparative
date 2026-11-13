package com.example.architecturepatternscomparative.mvi

import com.example.architecturepatternscomparative.core.data.FeedDataSource
import com.example.architecturepatternscomparative.core.data.HeaderInfo

object FeedReducer {
    fun reduce(state: FeedUiState, intent: FeedIntent): FeedUiState = when (intent) {
        is FeedIntent.ItemsLoaded -> state.copy(items = intent.items)
        is FeedIntent.ItemTick ->
            state.copy(items = FeedDataSource.applyLikeTick(state.items, intent.tick))

        is FeedIntent.HeaderTick -> state.copy(header = HeaderInfo(intent.tick))
        is FeedIntent.ToggleFavorite -> state.copy(
            favoriteIds = if (intent.id in state.favoriteIds) state.favoriteIds - intent.id
            else state.favoriteIds + intent.id
        )
    }
}
