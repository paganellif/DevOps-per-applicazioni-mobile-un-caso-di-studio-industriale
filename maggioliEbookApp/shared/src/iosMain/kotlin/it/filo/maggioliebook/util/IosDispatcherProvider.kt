package it.filo.maggioliebook.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

private class IosDispatcherProvider: DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.Default
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

internal actual fun getDispatcherProvider(): DispatcherProvider = IosDispatcherProvider()