package it.filo.maggioliebook.util.extensions

import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import platform.Foundation.NSData
import platform.Foundation.create
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.koin.core.component.KoinComponent
import platform.posix.memcpy

actual class ByteArrayConverter: KoinComponent {
    fun toData(byteArray: ByteArray): NSData = memScoped {
        NSData.create(bytes = allocArrayOf(byteArray),
            length = byteArray.size.toULong())
    }

/*actual fun NSData.toByteArray(): ByteArray = ByteArray(this@toByteArray.length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
}*/
}

