package it.filo.maggioliebook.util

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

internal expect fun getDispatcherProvider(): DispatcherProvider