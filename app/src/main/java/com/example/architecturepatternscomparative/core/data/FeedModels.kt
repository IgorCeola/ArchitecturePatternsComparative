package com.example.architecturepatternscomparative.core.data

data class FeedItem(
    val id: String,
    val title: String,
    val body: String,
    val likes: Int
)

data class HeaderInfo(val tick: Int)