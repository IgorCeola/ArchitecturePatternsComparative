package com.example.architecturepatternscomparative.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class LoadGenerator {
    fun startHeavyEmission(intervalMillis: Long = 50L, batchSize: Int = 50): Flow<List<FeedItem>> =
        flow {
            val inMemoryCache = mutableListOf<FeedItem>()

            while (true) {
                val payload = List(batchSize) {
                    FeedItem(
                        id = UUID.randomUUID().toString(),
                        content = UUID.randomUUID().toString().repeat(5),
                        timestamp = System.currentTimeMillis()
                    )
                }

                inMemoryCache.addAll(0, payload)

                if (inMemoryCache.size > 3000) {
                    inMemoryCache.subList(3000, inMemoryCache.size).clear()
                }

                emit(inMemoryCache.toList())
                delay(intervalMillis)
            }
        }
}