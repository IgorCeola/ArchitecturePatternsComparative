package com.example.architecturepatternscomparative.core.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.architecturepatternscomparative.core.data.FeedItem

@Composable
fun FeedList(
    items: List<FeedItem>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier
        .fillMaxSize()
        .testTag("feed_list")) {
        items(items = items, key = { it.id }) { item ->
            FeedItemRow(
                item = item,
                isFavorite = item.id in favoriteIds,
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}