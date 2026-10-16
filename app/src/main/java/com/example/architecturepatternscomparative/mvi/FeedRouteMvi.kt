package com.example.architecturepatternscomparative.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.architecturepatternscomparative.core.FeedScreen

@Composable
fun FeedRouteMvi(
    viewModel: FeedViewModelMvi = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FeedScreen(
        items = state.items
    )
}