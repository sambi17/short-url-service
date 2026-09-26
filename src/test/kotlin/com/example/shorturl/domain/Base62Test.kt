package com.example.shorturl.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class Base62Test {
    @Test
    fun `encodes boundary values`() {
        assertEquals("0", Base62.encode(0))
        assertEquals("1", Base62.encode(1))
        assertEquals("Z", Base62.encode(61))
        assertEquals("10", Base62.encode(62))
        assertEquals("ZZ", Base62.encode(3_843))
        assertEquals("100", Base62.encode(3_844))
    }

    @Test
    fun `encodes the largest positive long within eleven characters`() {
        val encoded = Base62.encode(Long.MAX_VALUE)
        assertEquals("aZl8N0y58M7", encoded)
        assertEquals(11, encoded.length)
    }

    @Test
    fun `rejects negative values`() {
        assertThrows<IllegalArgumentException> { Base62.encode(-1) }
    }
}
