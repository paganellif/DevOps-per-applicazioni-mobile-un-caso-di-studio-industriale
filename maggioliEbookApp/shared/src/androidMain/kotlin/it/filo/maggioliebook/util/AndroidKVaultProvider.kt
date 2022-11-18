package it.filo.maggioliebook.util

import android.content.Context
import com.liftric.kvault.KVault
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

private class AndroidKVaultProvider(context: Context): KVaultProvider {
    override val kvault: KVault = KVault(context)
}

internal actual fun Scope.getKVaultProvider(): KVaultProvider =
    AndroidKVaultProvider(androidContext())