package com.carava.carwash.auth.controller

import com.carava.carwash.auth.dto.*
import com.carava.carwash.auth.service.AuthService
import com.carava.carwash.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController("authController")
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: SignUpRequestDto,
    ) : ApiResponse<SignUpResponseDto> {

        val response = authService.signUp(request)
        return ApiResponse.success(data = response)
    }

    @PostMapping("/signin")
    fun signIn(
        @Valid @RequestBody request: SignInRequestDto,
    ) : ApiResponse<SignInResponseDto> {

        val response = authService.signIn(request)
        return ApiResponse.success(data = response)
    }

    @GetMapping("/check-username")
    fun checkUsername(@Valid @RequestParam email: String): ApiResponse<CheckUsernameResponseDto>
            = ApiResponse.success(data = authService.checkUsername(email))


}