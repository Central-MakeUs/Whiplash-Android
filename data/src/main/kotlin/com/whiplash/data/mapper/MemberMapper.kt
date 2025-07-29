package com.whiplash.data.mapper

import com.whiplash.domain.entity.auth.request.ChangeTermsStateEntity
import com.whiplash.network.dto.request.RequestChangeTermsState
import javax.inject.Inject

class MemberMapper @Inject constructor() {

    fun toNetworkRequest(entity: ChangeTermsStateEntity): RequestChangeTermsState {
        with(entity) {
            return RequestChangeTermsState(
                privacyPolicy = privacyPolicy,
                pushNotificationPolicy = pushNotificationPolicy
            )
        }
    }
}