package it.filo.maggioliebook.util

import com.liftric.kvault.KVault
import org.koin.core.scope.Scope

private class IosKVaultProvider: KVaultProvider {
    override val kvault: KVault = KVault()
}

internal actual fun Scope.getKVaultProvider(): KVaultProvider = IosKVaultProvider()