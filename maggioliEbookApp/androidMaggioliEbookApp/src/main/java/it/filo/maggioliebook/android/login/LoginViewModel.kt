package it.filo.maggioliebook.android.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.filo.maggioliebook.usecase.user.CheckUserLoggedUseCase
import it.filo.maggioliebook.usecase.user.UserLoginUseCase

class LoginViewModel: ViewModel() {

    private suspend fun _login(username: String, password: String, rememberMe: Boolean) =
        MutableLiveData<Boolean>().apply {
            value = UserLoginUseCase().invoke(username, password, rememberMe)
        }

    private val _isUserLogged = MutableLiveData<Boolean>().apply {
        value = CheckUserLoggedUseCase().invoke()
    }

    suspend fun login(username: String, password: String, rememberMe: Boolean): LiveData<Boolean> =
        _login(username, password, rememberMe)

    val isUserLogged: LiveData<Boolean> = _isUserLogged
}