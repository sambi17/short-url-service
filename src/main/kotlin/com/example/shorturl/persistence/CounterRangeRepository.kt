package com.example.shorturl.persistence

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.lang.Math.addExact

data class IdRange(val first: Long, val lastInclusive: Long)

@Repository
class CounterRangeRepository(private val jdbcTemplate: JdbcTemplate) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun reserve(blockSize: Int): IdRange {
        require(blockSize > 0) { "blockSize must be positive" }

        val first = jdbcTemplate.queryForObject(
            "SELECT next_value FROM id_counters WHERE name = ? FOR UPDATE",
            Long::class.java,
            COUNTER_NAME,
        ) ?: error("Counter '$COUNTER_NAME' is missing")

        val nextValue = addExact(first, blockSize.toLong())
        val updated = jdbcTemplate.update(
            "UPDATE id_counters SET next_value = ? WHERE name = ?",
            nextValue,
            COUNTER_NAME,
        )
        check(updated == 1) { "Counter '$COUNTER_NAME' could not be updated" }

        return IdRange(first, nextValue - 1)
    }

    private companion object {
        const val COUNTER_NAME = "short_url"
    }
}
