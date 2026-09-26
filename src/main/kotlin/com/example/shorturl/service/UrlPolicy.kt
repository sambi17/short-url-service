package com.example.shorturl.service

import org.springframework.stereotype.Component
import java.net.URI

@Component
class UrlPolicy {
    fun validate(rawUrl: String): String {
        if (rawUrl != rawUrl.trim()) {
            throw InvalidUrlException("url must not have leading or trailing whitespace")
        }

        val uri = try {
            URI(rawUrl)
        } catch (_: IllegalArgumentException) {
            throw InvalidUrlException("url must be a valid absolute URI")
        }

        if (uri.scheme?.lowercase() !in ALLOWED_SCHEMES || uri.host.isNullOrBlank()) {
            throw InvalidUrlException("url must be an absolute http or https URL with a host")
        }
        if (uri.userInfo != null) {
            throw InvalidUrlException("url must not contain embedded credentials")
        }

        return uri.toASCIIString()
    }

    private companion object {
        val ALLOWED_SCHEMES = setOf("http", "https")
    }
}

class InvalidUrlException(message: String) : RuntimeException(message)
class InvalidExpiryException(message: String) : RuntimeException(message)
class ShortUrlNotFoundException(code: String) : RuntimeException("Short URL '$code' was not found")
class ShortUrlExpiredException(code: String) : RuntimeException("Short URL '$code' has expired")
