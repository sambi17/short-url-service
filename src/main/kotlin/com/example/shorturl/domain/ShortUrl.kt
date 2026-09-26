package com.example.shorturl.domain

import java.time.Instant

data class ShortUrl(
    val id: Long,
    val code: String,
    val longUrl: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
)
