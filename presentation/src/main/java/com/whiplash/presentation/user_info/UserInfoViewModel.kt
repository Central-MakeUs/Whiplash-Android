package com.whiplash.presentation.user_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.entity.auth.request.ChangeTermsStateEntity
import com.whiplash.domain.usecase.member.ChangeTermsStateUseCase
import com.whiplash.domain.usecase.member.WithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val changeTermsStateUseCase: ChangeTermsStateUseCase,
    private val withdrawUseCase: WithdrawUseCase,
): ViewModel() {

    data class UserInfoUiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        // 회원 탈퇴 성공 여부
        val isWithdrawCompleted: Boolean = false,

        // 약관 동의 상태 변경 성공 여부
        val isTermsStateChanged: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UserInfoUiState())
    val uiState: StateFlow<UserInfoUiState> = _uiState.asStateFlow()

    // 회원 탈퇴
    fun withdraw() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        try {
            withdrawUseCase.invoke().collect { result ->
                result.onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isWithdrawCompleted = true
                        )
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun clearWithdrawCompleted() = _uiState.update { it.copy(isWithdrawCompleted = false) }

    // 약관 동의 상태 변경
    fun changeTermsState(
        privacyPolicy: Boolean,
        pushNotificationPolicy: Boolean
    ) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        try {
            val request = ChangeTermsStateEntity(
                privacyPolicy = privacyPolicy,
                pushNotificationPolicy = pushNotificationPolicy
            )

            changeTermsStateUseCase.invoke(request).collect { result ->
                result.onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isTermsStateChanged = true
                        )
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}