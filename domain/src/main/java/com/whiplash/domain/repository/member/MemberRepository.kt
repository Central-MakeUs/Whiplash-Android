package com.whiplash.domain.repository.member

import com.whiplash.domain.entity.auth.request.ChangeTermsStateEntity
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    suspend fun changeTermsState(request: ChangeTermsStateEntity): Flow<Result<Unit>>
}