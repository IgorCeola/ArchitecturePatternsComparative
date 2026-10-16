package com.example.architecturepatternscomparative.mvi

import com.example.architecturepatternscomparative.core.FeedItem

data class FeedState(
    val items: List<FeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface FeedIntent {
    data class LoadItems(val items: List<FeedItem>) : FeedIntent
}