package it.filo.maggioliebook.usecase.user

import it.filo.maggioliebook.domain.user.User
import it.filo.maggioliebook.repository.user.UserRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.*
import org.koin.core.component.inject

class GetUserInfoUseCase: BaseUseCase() {

    private val userRepository: UserRepository by inject()

    suspend fun invoke(): User = userRepository.userInfo()

    fun invokeNative(onSuccess: (user: User) -> Unit,
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