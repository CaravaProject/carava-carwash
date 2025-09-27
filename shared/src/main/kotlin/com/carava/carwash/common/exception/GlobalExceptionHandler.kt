package com.carava.carwash.common.exception

import com.carava.carwash.common.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    // ==== 인증/권한 ====
    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleBadCredentialsException(ex: BadCredentialsException) =
        ApiResponse.error("INVALID_CREDENTIALS")

    @ExceptionHandler(UsernameNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUsernameNotFoundException(ex: UsernameNotFoundException) =
        ApiResponse.error("USER_NOT_FOUND")

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbiddenException(ex: ForbiddenException) =
        ApiResponse.error("FORBIDDEN")

    // ==== 데이터 검증 ====
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleValidationException(ex: MethodArgumentNotValidException) =
        ApiResponse.error("VALIDATION_ERROR")

    @ExceptionHandler(EmailAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException) =
        ApiResponse.error("EMAIL_ALREADY_EXISTS")

    @ExceptionHandler(ConflictException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflictException(ex: ConflictException) =
        ApiResponse.error("CONFLICT")

    // ==== 리소스 ====
    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handlerNotFoundException(ex: NotFoundException) =
        ApiResponse.error("NOT_FOUND")

    // ==== 시스템 오류 ====
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGenericException(ex: Exception) =
        ApiResponse.error("INTERNAL_SERVER_ERROR")
}