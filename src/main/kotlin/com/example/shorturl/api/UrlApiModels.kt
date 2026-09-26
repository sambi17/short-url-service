package com.example.shorturl.api

import com.example.shorturl.domain.ShortUrl
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.net.URI
import java.time.Instant

data class CreateShortUrlRequest(
    @field:NotBlank
    @field:Size(max = 2_048)
    val url: String,
    val expiresAt: Instant? = null,
)

data class ShortUrlResponse(
    val code: String,
    val shortUrl: URI,
    val longUrl: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
) {
    companion object {
        fun from(value: ShortUrl, publicUri: URI) = ShortUrlResponse(
            code = value.code,
            shortUrl = publicUri,
            longUrl = value.longUrl,
            createdAt = value.createdAt,
            expiresAt = value.expiresAt,
        )
    }
}
