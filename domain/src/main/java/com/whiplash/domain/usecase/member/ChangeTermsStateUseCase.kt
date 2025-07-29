package com.whiplash.domain.usecase.member

import com.whiplash.domain.entity.auth.request.ChangeTermsStateEntity
import com.whiplash.domain.repository.member.MemberRepository
import kotlinx.coroutines.flow.Flow

class ChangeTermsStateUseCase(
    private val repository: MemberRepository
) {
    suspend operator fun invoke(request: ChangeTermsStateEntity): Flow<Result<Unit>> =
        repository.changeTermsState(request)
}