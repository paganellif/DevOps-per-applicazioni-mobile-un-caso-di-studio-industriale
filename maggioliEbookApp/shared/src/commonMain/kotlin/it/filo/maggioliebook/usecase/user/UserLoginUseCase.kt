package it.filo.maggioliebook.usecase.user

import it.filo.maggioliebook.repository.user.UserRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.*
import org.koin.core.component.inject

class UserLoginUseCase: BaseUseCase() {

    private val userRepository: UserRepository by inject()

    suspend fun invoke(username: String, password: String, rememberMe: Boolean): Boolean =
        userRepository.login(username, password, rememberMe)

    fun invokeNative(username: String,
                     password: String,
                     rememberMe: Boolean,
                     onSuccess: (userLogin: Boolean) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            nativeScope.launch {
                onSuccess(invoke(username, password, rememberMe))
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}