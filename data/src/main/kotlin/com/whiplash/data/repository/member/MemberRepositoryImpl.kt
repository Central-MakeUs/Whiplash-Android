package com.whiplash.data.repository.member

import com.whiplash.data.mapper.MemberMapper
import com.whiplash.data.repository.safeApiCallWithTransform
import com.whiplash.domain.entity.auth.request.ChangeTermsStateEntity
import com.whiplash.domain.repository.member.MemberRepository
import com.whiplash.network.api.MemberService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MemberRepositoryImpl @Inject constructor(
    private val service: MemberService,
    private val mapper: MemberMapper,
): MemberRepository {
    override suspend fun changeTermsState(request: ChangeTermsStateEntity): Flow<Result<Unit>> =
        safeApiCallWithTransform(
            apiCall = { service.changeTermsState(mapper.toNetworkRequest(request)) },
            transform = {}
        )
}