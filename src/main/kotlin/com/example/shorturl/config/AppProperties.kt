package com.example.shorturl.config

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.net.URI

@Validated
@ConfigurationProperties("app")
data class AppProperties(
    val baseUrl: URI = URI.create("http://localhost:8080"),
    @field:Min(1)
    @field:Max(1_000_000)
    val counterBlockSize: Int = 1_000,
)
