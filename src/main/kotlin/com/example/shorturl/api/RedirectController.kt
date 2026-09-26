package com.example.shorturl.api

import com.example.shorturl.service.ShortUrlService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class RedirectController(private val service: ShortUrlService) {
    @GetMapping("/{code:[0-9A-Za-z]+}")
    fun redirect(@PathVariable code: String): ResponseEntity<Void> {
        val shortUrl = service.getActive(code)
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(shortUrl.longUrl))
            .build()
    }
}
