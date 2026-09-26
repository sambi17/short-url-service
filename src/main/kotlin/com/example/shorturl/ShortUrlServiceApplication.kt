package com.example.shorturl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ShortUrlServiceApplication

fun main(args: Array<String>) {
	runApplication<ShortUrlServiceApplication>(*args)
}
