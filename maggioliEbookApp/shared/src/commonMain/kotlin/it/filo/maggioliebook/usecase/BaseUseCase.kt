package it.filo.maggioliebook.usecase

import it.filo.maggioliebook.util.getDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent

open class BaseUseCase: KoinComponent {
    protected val nativeScope = CoroutineScope(getDispatcherProvider().main)

    fun onCancel() {
        nativeScope.cancel()
    }
}