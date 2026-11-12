package com.example.architecturepatternscomparative.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun FeedHeader(tick: Int, modifier: Modifier = Modifier) {
    Surface(
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("feed_header")
    ) {
        Text(
            text = "Atualizações recebidas: $tick",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}