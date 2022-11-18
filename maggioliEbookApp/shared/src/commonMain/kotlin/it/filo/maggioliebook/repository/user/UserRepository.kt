package it.filo.maggioliebook.repository.user

import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import it.filo.maggioliebook.datasource.remote.isValid
import it.filo.maggioliebook.datasource.remote.user.LoginDataSource
import it.filo.maggioliebook.datasource.remote.user.UserDataSource
import it.filo.maggioliebook.domain.user.JwtToken
import it.filo.maggioliebook.domain.user.User
import it.filo.maggioliebook.util.*
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val EMAIL_ADDRESS: String =
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"

private const val PWD_MIN_LENGTH: Int = 4

class UserRepository: KoinComponent {
    private val dispatcherProvider: DispatcherProvider by inject()
    private val userDataSource: UserDataSource by inject()
    private val jwtManager: JwtManager by inject()
    private val loginDataSource: LoginDataSource by inject()

    /**
     *
     */
    suspend fun userInfo(): User = withContext(dispatcherProvider.io) {
        userDataSource.getUserInfo().body()
    }

    /**
     *
     */
    suspend fun login(username: String, password: String, rememberMe: Boolean = true): Boolean =
        withContext(dispatcherProvider.io){
            if (isUserNameValid(username) && isPasswordValid(password)){
                if(jwtManager.isStoredAuthToken())
                    true
                else {
                    val response = loginDataSource.login(username, password, rememberMe)
                    if(response.isValid()){
                        val token: JwtToken = loginDataSource.login(username, password, rememberMe).body()
                        Napier.d("Login Successful")
                        jwtManager.storeAuthToken(token.token)
                    } else {
                        Napier.e("Login Failed: status code ${response.status}")
                        false
                    }
                }
            } else
                false
        }

    /**
     *
     */
    fun logout(): Boolean = jwtManager.removeAuthToken()

    fun isUserLoggedIn(): Boolean = jwtManager.isStoredAuthToken()

    /**
     *
     */
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            username.matches(Regex(EMAIL_ADDRESS))
        } else {
            username.isNotBlank()
        }
    }

    /**
     *
     */
    private fun isPasswordValid(password: String): Boolean {
        return password.length > PWD_MIN_LENGTH
    }
}