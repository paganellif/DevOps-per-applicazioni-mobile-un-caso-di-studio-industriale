package it.filo.maggioliebook.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

private class AndroidDispatcherProvider: DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

actual fun getDispatcherProvider(): DispatcherProvider = AndroidDispatcherProvider()