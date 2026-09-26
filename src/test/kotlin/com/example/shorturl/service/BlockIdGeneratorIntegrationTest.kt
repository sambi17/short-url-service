package com.example.shorturl.service

import com.example.shorturl.config.AppProperties
import com.example.shorturl.persistence.CounterRangeRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class BlockIdGeneratorIntegrationTest @Autowired constructor(
    private val counterRepository: CounterRangeRepository,
) {
    @Test
    fun `two simulated servers generate unique IDs concurrently`() {
        val settings = AppProperties(counterBlockSize = 7)
        val serverA = BlockIdGenerator(counterRepository, settings)
        val serverB = BlockIdGenerator(counterRepository, settings)
        val requestCount = 2_000
        val executor = Executors.newFixedThreadPool(32)

        try {
            val tasks = (0 until requestCount).map { index ->
                Callable { if (index % 2 == 0) serverA.nextId() else serverB.nextId() }
            }
            val ids = executor.invokeAll(tasks).map { it.get() }

            assertEquals(requestCount, ids.toSet().size)
            assertTrue(ids.all { it > 0 })
        } finally {
            executor.shutdownNow()
        }
    }
}
