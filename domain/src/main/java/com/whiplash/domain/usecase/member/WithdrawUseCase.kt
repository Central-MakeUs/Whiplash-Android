package com.whiplash.domain.usecase.member

import com.whiplash.domain.repository.member.MemberRepository
import kotlinx.coroutines.flow.Flow

class WithdrawUseCase(
    private val repository: MemberRepository
) {
    suspend operator fun invoke(): Flow<Result<Unit>> = repository.withdraw()
}