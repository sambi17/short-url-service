package com.example.shorturl.domain

object Base62 {
    private const val ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val RADIX = ALPHABET.length

    fun encode(value: Long): String {
        require(value >= 0) { "Base62 only supports non-negative values" }
        if (value == 0L) return ALPHABET[0].toString()

        var remaining = value
        val encoded = StringBuilder()
        while (remaining > 0) {
            encoded.append(ALPHABET[(remaining % RADIX).toInt()])
            remaining /= RADIX
        }
        return encoded.reverse().toString()
    }
}
