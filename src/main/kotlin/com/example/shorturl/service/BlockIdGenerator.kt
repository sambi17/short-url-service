package com.example.shorturl.service

import com.example.shorturl.config.AppProperties
import com.example.shorturl.persistence.CounterRangeRepository
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Reserves disjoint counter ranges in the shared database, then serves IDs from
 * memory. Different application instances can never reserve the same range.
 */
@Component
class BlockIdGenerator(
    private val counterRepository: CounterRangeRepository,
    properties: AppProperties,
) {
    private val blockSize = properties.counterBlockSize
    private val refillLock = ReentrantLock()

    @Volatile
    private var current = LocalRange.empty()

    fun nextId(): Long {
        while (true) {
            val snapshot = current
            val candidate = snapshot.next.getAndIncrement()
            if (candidate <= snapshot.lastInclusive) return candidate

            refillLock.withLock {
                if (current === snapshot) {
                    val reserved = counterRepository.reserve(blockSize)
                    current = LocalRange(AtomicLong(reserved.first), reserved.lastInclusive)
                }
            }
        }
    }

    private data class LocalRange(
        val next: AtomicLong,
        val lastInclusive: Long,
    ) {
        companion object {
            fun empty() = LocalRange(AtomicLong(0), -1)
        }
    }
}
