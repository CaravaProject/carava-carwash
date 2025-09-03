package com.carava.carwash.auth.service

import com.carava.carwash.auth.dto.*
import com.carava.carwash.common.constants.UserType
import com.carava.carwash.common.exception.EmailAlreadyExistsException
import com.carava.carwash.domain.auth.entity.Auth
import com.carava.carwash.domain.auth.repository.AuthRepository
import com.carava.carwash.infrastructure.config.security.JwtUtil
import com.carava.carwash.member.dto.CreateMemberRequestDto
import com.carava.carwash.member.service.MemberService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("authService")
@Transactional(readOnly = true)
class AuthService(
    private val authRepository: AuthRepository,
    private val memberService: MemberService,

    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val authenticationManager: AuthenticationManager,
) {

    @Transactional
    fun signUp(request: SignUpRequestDto): SignUpResponseDto {
        if (authRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException("이미 존재하는 이메일입니다.")
        }

        val auth = Auth(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            userType = UserType.OWNER
        )
        val savedAuth = authRepository.save(auth)

        val createMemberRequest = CreateMemberRequestDto(
            name = request.name,
            authId = savedAuth.id
        )
        val member = memberService.createMember(createMemberRequest)

        return SignUpResponseDto(
            email = savedAuth.email,
            userType = savedAuth.userType.toString(),
            createdAt = savedAuth.createdAt
        )
    }

    fun signIn(request: SignInRequestDto): SignInResponseDto {

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val auth = authRepository.findByEmail(request.email)
            .orElseThrow{ UsernameNotFoundException("사용자를 찾을 수 없습니다.") }

        val member = memberService.findByAuthId(auth.id)
        val token = jwtUtil.generateToken(auth.email, member.id)

        return SignInResponseDto(
            accessToken = token,
            expiresIn = 86400
        )
    }

    fun checkUsername(email: String): CheckUsernameResponseDto {
        return CheckUsernameResponseDto(
            isDuplicate = authRepository.existsByEmail(email)
        )
    }
}