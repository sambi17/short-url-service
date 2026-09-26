package com.example.shorturl.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class UrlPolicyTest {
    private val policy = UrlPolicy()

    @Test
    fun `accepts http and https URLs`() {
        assertEquals("https://example.com/path?q=hello", policy.validate("https://example.com/path?q=hello"))
        assertEquals("http://example.com", policy.validate("http://example.com"))
    }

    @Test
    fun `rejects dangerous or ambiguous URLs`() {
        listOf(
            "javascript:alert(1)",
            "file:///etc/passwd",
            "https:///missing-host",
            "https://user:password@example.com/private",
            " https://example.com",
        ).forEach { invalid ->
            assertThrows<InvalidUrlException>(invalid) { policy.validate(invalid) }
        }
    }
}
