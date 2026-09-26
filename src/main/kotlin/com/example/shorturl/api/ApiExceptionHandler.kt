package com.example.shorturl.api

import com.example.shorturl.service.InvalidExpiryException
import com.example.shorturl.service.InvalidUrlException
import com.example.shorturl.service.ShortUrlExpiredException
import com.example.shorturl.service.ShortUrlNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(InvalidUrlException::class, InvalidExpiryException::class)
    fun badRequest(exception: RuntimeException): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, exception.message ?: "Invalid request")

    @ExceptionHandler(ShortUrlNotFoundException::class)
    fun notFound(exception: ShortUrlNotFoundException): ProblemDetail =
        problem(HttpStatus.NOT_FOUND, exception.message ?: "Not found")

    @ExceptionHandler(ShortUrlExpiredException::class)
    fun expired(exception: ShortUrlExpiredException): ProblemDetail =
        problem(HttpStatus.GONE, exception.message ?: "Expired")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(exception: MethodArgumentNotValidException): ProblemDetail {
        val detail = problem(HttpStatus.BAD_REQUEST, "Request validation failed")
        detail.setProperty(
            "errors",
            exception.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") },
        )
        return detail
    }

    private fun problem(status: HttpStatus, detail: String): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).also { it.title = status.reasonPhrase }
}
