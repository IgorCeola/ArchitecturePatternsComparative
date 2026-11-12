package com.example.architecturepatternscomparative.mvvm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.architecturepatternscomparative.core.data.FeedItem
import com.example.architecturepatternscomparative.core.data.HeaderInfo
import com.example.architecturepatternscomparative.core.data.Scenario
import com.example.architecturepatternscomparative.core.ui.FeedHeader
import com.example.architecturepatternscomparative.core.ui.FeedList
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FeedRouteMvvm(
    scenario: Scenario,
    viewModel: FeedViewModelMvvm = viewModel { FeedViewModelMvvm(scenario) }
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HeaderSectionMvvm(header = viewModel.header)
        ListSectionMvvm(
            items = viewModel.items,
            favoriteIds = viewModel.favoriteIds,
            onToggleFavorite = viewModel::toggleFavorite
        )
    }
}

@Composable
private fun HeaderSectionMvvm(header: StateFlow<HeaderInfo>) {
    val value by header.collectAsStateWithLifecycle()
    FeedHeader(tick = value.tick)
}

@Composable
private fun ListSectionMvvm(
    items: StateFlow<List<FeedItem>>,
    favoriteIds: StateFlow<Set<String>>,
    onToggleFavorite: (String) -> Unit
) {
    val itemsValue by items.collectAsStateWithLifecycle()
    val favorites by favoriteIds.collectAsStateWithLifecycle()
    FeedList(items = itemsValue, favoriteIds = favorites, onToggleFavorite = onToggleFavorite)
}