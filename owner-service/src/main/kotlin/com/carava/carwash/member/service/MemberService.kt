package com.carava.carwash.member.service

import com.carava.carwash.member.dto.CreateMemberRequestDto
import com.carava.carwash.member.entity.Member
import com.carava.carwash.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("memberService")
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun createMember(request: CreateMemberRequestDto) : Member {
        val member = Member(
            name = request.name,
            authId = request.authId
        )

        val savedMember = memberRepository.save(member)

        return savedMember
    }

    fun findByAuthId(authId: Long): Member {
        return memberRepository.findByAuthId(authId)
            ?: throw IllegalStateException("Auth에 연결된 Member가 없습니다.")
    }
}