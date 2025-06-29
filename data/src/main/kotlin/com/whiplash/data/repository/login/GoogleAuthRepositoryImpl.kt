package com.whiplash.data.repository.login

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.whiplash.data.BuildConfig
import com.whiplash.domain.model.UserEntity
import com.whiplash.domain.repository.login.GoogleAuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
) : GoogleAuthRepository {

    override suspend fun signInWithGoogleToken(idToken: String): Result<UserEntity> {
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

            val user = authResult.user
            if (user != null) {
                val userEntity = UserEntity(
                    id = user.uid,
                    email = user.email,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString()
                )
                Result.success(userEntity)
            } else {
                Result.failure(Exception("로그인 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): UserEntity? {
        val user = firebaseAuth.currentUser
        return if (user != null) {
            UserEntity(
                id = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString()
            )
        } else null
    }

    override fun getGoogleSignInIntent(): Any {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }

    override suspend fun handleSignInResult(data: Any?): Result<String> {
        return try {
            val intent = data as? Intent
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)

            account.idToken?.let { idToken ->
                Result.success(idToken)
            } ?: Result.failure(Exception("ID Token을 가져올 수 없습니다"))
        } catch (e: ApiException) {
            Result.failure(Exception("Google Sign-In 실패: ${e.statusCode}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}