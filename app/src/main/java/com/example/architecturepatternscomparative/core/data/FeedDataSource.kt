package com.example.architecturepatternscomparative.core.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

object FeedDataSource {
    const val ITEM_COUNT = 1_000
    const val SEED = 42
    const val ITEM_TICK_MS = 100L
    const val HEADER_TICK_MS = 50L
    private val words = listOf(
        "estado", "fluxo", "interface", "evento", "lista", "quadro",
        "composicao", "modelo", "intencao", "dados", "tela", "usuario"
    )

    fun initialItems(): List<FeedItem> {
        val random = Random(SEED)
        return List(ITEM_COUNT) { index ->
            FeedItem(
                id = "item-%04d".format(index),
                title = "Item $index",
                body = randomText(random, maxLength = 120),
                likes = random.nextInt(0, 500)
            )
        }
    }

    private fun randomText(random: Random, maxLength: Int): String =
        buildString {
            while (length < maxLength) {
                append(words[random.nextInt(words.size)]).append(' ')
            }
        }.take(maxLength)

    fun applyLikeTick(items: List<FeedItem>, tick: Int): List<FeedItem> =
        items.mapIndexed { index, item ->
            if (index % 10 == tick % 10) item.copy(likes = item.likes + 1)
            else item
        }

    fun ticks(intervalMs: Long): Flow<Int> = flow {
        var tick = 0
        while (true) {
            delay(intervalMs)
            emit(tick++)
        }
    }
}