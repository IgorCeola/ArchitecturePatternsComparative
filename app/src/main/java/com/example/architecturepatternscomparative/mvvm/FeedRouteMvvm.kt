package com.example.architecturepatternscomparative.mvvm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.architecturepatternscomparative.core.FeedScreen

@Composable
fun FeedRouteMvvm(
    viewModel: FeedViewModel = viewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    FeedScreen(
        items = items
    )
}