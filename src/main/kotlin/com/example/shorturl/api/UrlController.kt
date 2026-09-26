package com.example.shorturl.api

import com.example.shorturl.config.AppProperties
import com.example.shorturl.service.ShortUrlService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/urls")
class UrlController(
    private val service: ShortUrlService,
    private val properties: AppProperties,
) {
    @PostMapping
    fun create(@Valid @RequestBody request: CreateShortUrlRequest): ResponseEntity<ShortUrlResponse> {
        val created = service.create(request.url, request.expiresAt)
        val publicUri = publicUri(created.code)
        return ResponseEntity.created(publicUri).body(ShortUrlResponse.from(created, publicUri))
    }

    @GetMapping("/{code}")
    fun get(@PathVariable code: String): ShortUrlResponse {
        val shortUrl = service.getActive(code)
        return ShortUrlResponse.from(shortUrl, publicUri(code))
    }

    @DeleteMapping("/{code}")
    fun delete(@PathVariable code: String): ResponseEntity<Void> {
        service.delete(code)
        return ResponseEntity.noContent().build()
    }

    private fun publicUri(code: String): URI =
        URI.create("${properties.baseUrl.toString().trimEnd('/')}/$code")
}
