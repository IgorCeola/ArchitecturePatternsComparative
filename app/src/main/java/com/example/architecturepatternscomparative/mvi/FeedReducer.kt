package com.example.architecturepatternscomparative.mvi

class FeedReducer {
    fun reduce(currentState: FeedState, intent: FeedIntent): FeedState {
        return when (intent) {
            is FeedIntent.LoadItems -> currentState.copy(
                items = intent.items
            )
        }
    }
}