package com.example.shorturl.persistence

import com.example.shorturl.domain.ShortUrl
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class ShortUrlRepository(private val jdbcTemplate: JdbcTemplate) {
    fun insert(shortUrl: ShortUrl) {
        val inserted = jdbcTemplate.update(
            """
            INSERT INTO short_urls (id, code, long_url, created_at, expires_at)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            shortUrl.id,
            shortUrl.code,
            shortUrl.longUrl,
            shortUrl.createdAt.atOffset(ZoneOffset.UTC),
            shortUrl.expiresAt?.atOffset(ZoneOffset.UTC),
        )
        check(inserted == 1) { "Short URL was not inserted" }
    }

    fun findByCode(code: String): ShortUrl? = jdbcTemplate.query(
        """
        SELECT id, code, long_url, created_at, expires_at
        FROM short_urls
        WHERE code = ?
        """.trimIndent(),
        { resultSet, _ -> resultSet.toShortUrl() },
        code,
    ).firstOrNull()

    fun deleteByCode(code: String): Boolean =
        jdbcTemplate.update("DELETE FROM short_urls WHERE code = ?", code) == 1

    private fun ResultSet.toShortUrl() = ShortUrl(
        id = getLong("id"),
        code = getString("code"),
        longUrl = getString("long_url"),
        createdAt = getObject("created_at", OffsetDateTime::class.java).toInstant(),
        expiresAt = getObject("expires_at", OffsetDateTime::class.java)?.toInstant(),
    )
}
