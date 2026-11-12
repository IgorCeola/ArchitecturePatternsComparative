package com.example.architecturepatternscomparative.mvi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.architecturepatternscomparative.core.data.Scenario
import com.example.architecturepatternscomparative.core.ui.FeedHeader
import com.example.architecturepatternscomparative.core.ui.FeedList

@Composable
fun FeedRouteMvi(
    scenario: Scenario,
    viewModel: FeedViewModelMvi = viewModel { FeedViewModelMvi(scenario) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FeedScreenMvi(state = state, onIntent = viewModel::onIntent)
}

@Composable
private fun FeedScreenMvi(state: FeedUiState, onIntent: (FeedIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        FeedHeader(tick = state.header.tick)
        FeedList(
            items = state.items,
            favoriteIds = state.favoriteIds,
            onToggleFavorite = { id ->
                onIntent(FeedIntent.ToggleFavorite(id))
            }
        )
    }
}
