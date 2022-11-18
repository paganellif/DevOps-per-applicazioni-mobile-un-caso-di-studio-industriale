package it.filo.maggioliebook.util

import com.liftric.kvault.KVault
import org.koin.core.scope.Scope

interface KVaultProvider {
    val kvault: KVault
}

internal expect fun Scope.getKVaultProvider(): KVaultProvider