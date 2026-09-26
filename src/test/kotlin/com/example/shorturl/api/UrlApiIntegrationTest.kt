package com.example.shorturl.api

import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class UrlApiIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @Test
    fun `creates reads redirects and deletes a short URL`() {
        val destination = "https://example.com/articles/42?source=test"
        val createResult = mockMvc.post("/api/v1/urls") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"url":"$destination"}"""
        }.andExpect {
            status { isCreated() }
            header { string("Location", startsWith("http://localhost:8080/")) }
            jsonPath("$.code") { isNotEmpty() }
            jsonPath("$.longUrl") { value(destination) }
            jsonPath("$.shortUrl") { value(startsWith("http://localhost:8080/")) }
        }.andReturn()

        val code = Regex("\"code\":\"([^\"]+)\"")
            .find(createResult.response.contentAsString)!!
            .groupValues[1]

        mockMvc.get("/api/v1/urls/{code}", code).andExpect {
            status { isOk() }
            jsonPath("$.code") { value(code) }
            jsonPath("$.longUrl") { value(destination) }
        }

        mockMvc.get("/{code}", code).andExpect {
            status { isFound() }
            header { string("Location", destination) }
        }

        mockMvc.delete("/api/v1/urls/{code}", code).andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/{code}", code).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `rejects an unsafe URL scheme`() {
        mockMvc.post("/api/v1/urls") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"url":"javascript:alert(1)"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("url must be an absolute http or https URL with a host") }
        }
    }

    @Test
    fun `rejects a past expiration time`() {
        mockMvc.post("/api/v1/urls") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"url":"https://example.com","expiresAt":"2000-01-01T00:00:00Z"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("expiresAt must be in the future") }
        }
    }

    @Test
    fun `returns validation details for a missing URL`() {
        mockMvc.post("/api/v1/urls") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"url":""}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors.url") { exists() }
        }
    }

    @Test
    fun `returns not found for an unknown code`() {
        mockMvc.get("/api/v1/urls/NotThere").andExpect {
            status { isNotFound() }
        }
    }
}
