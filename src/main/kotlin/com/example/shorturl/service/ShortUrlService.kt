package com.example.shorturl.service

import com.example.shorturl.domain.Base62
import com.example.shorturl.domain.ShortUrl
import com.example.shorturl.persistence.ShortUrlRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class ShortUrlService(
    private val idGenerator: BlockIdGenerator,
    private val repository: ShortUrlRepository,
    private val urlPolicy: UrlPolicy,
    private val clock: Clock,
) {
    fun create(rawUrl: String, expiresAt: Instant?): ShortUrl {
        val now = clock.instant()
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw InvalidExpiryException("expiresAt must be in the future")
        }

        val id = idGenerator.nextId()
        val shortUrl = ShortUrl(
            id = id,
            code = Base62.encode(id),
            longUrl = urlPolicy.validate(rawUrl),
            createdAt = now,
            expiresAt = expiresAt,
        )
        repository.insert(shortUrl)
        return shortUrl
    }

    fun getActive(code: String): ShortUrl {
        val shortUrl = repository.findByCode(code) ?: throw ShortUrlNotFoundException(code)
        if (shortUrl.expiresAt?.isAfter(clock.instant()) == false) {
            throw ShortUrlExpiredException(code)
        }
        return shortUrl
    }

    fun delete(code: String) {
        if (!repository.deleteByCode(code)) throw ShortUrlNotFoundException(code)
    }
}
