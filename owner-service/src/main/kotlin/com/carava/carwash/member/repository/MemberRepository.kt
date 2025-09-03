package com.carava.carwash.member.repository

import com.carava.carwash.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository("memberRepository")
interface MemberRepository : JpaRepository<Member, Long> {

    fun findByAuthId(authId: Long): Member?
}