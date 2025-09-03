package com.carava.carwash.infrastructure.resolver

import com.carava.carwash.infrastructure.annotation.CurrentMemberId
import com.carava.carwash.infrastructure.config.security.JwtUtil
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class MemberIdResolver(
    private val jwtUtil: JwtUtil,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter) =
        parameter.hasParameterAnnotation(CurrentMemberId::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Long {
        val token = webRequest.getHeader("Authorization")?.removePrefix("Bearer ")
            ?: throw IllegalArgumentException("Authorization 헤더가 없습니다.")
        return jwtUtil.getMemberIdFromToken(token)
    }
}