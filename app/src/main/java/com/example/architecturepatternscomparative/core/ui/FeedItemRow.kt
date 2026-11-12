package com.example.architecturepatternscomparative.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.architecturepatternscomparative.core.data.FeedItem

@Composable
fun FeedItemRow(
    item: FeedItem,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("feed_item")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title, style =
                        MaterialTheme.typography.titleSmall
                )
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "${item.likes} curtidas",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            IconToggleButton(
                checked = isFavorite,
                onCheckedChange = { onToggleFavorite(item.id) },
                modifier = Modifier.testTag("favorite_button")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else
                        Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favoritar"
                )
            }
        }
    }
}
