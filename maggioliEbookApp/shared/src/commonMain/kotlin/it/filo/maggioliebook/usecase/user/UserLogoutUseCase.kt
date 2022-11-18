package it.filo.maggioliebook.usecase.user

import it.filo.maggioliebook.repository.user.UserRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.*
import org.koin.core.component.inject

class UserLogoutUseCase: BaseUseCase() {

    private val userRepository: UserRepository by inject()

    fun invoke(): Boolean = userRepository.logout()

    fun invokeNative(onSuccess: (userLogin: Boolean) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            nativeScope.launch {
                onSuccess(invoke())
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}