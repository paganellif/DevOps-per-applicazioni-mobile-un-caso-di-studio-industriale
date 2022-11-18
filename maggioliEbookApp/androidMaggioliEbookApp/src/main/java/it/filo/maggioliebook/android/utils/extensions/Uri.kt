package it.filo.maggioliebook.android.utils.extensions

import android.content.Context
import android.net.Uri
import org.readium.r2.shared.extensions.tryOrNull
import org.readium.r2.shared.util.mediatype.MediaType
import java.io.File
import java.util.*

suspend fun Uri.copyToTempFile(context: Context, dir: File): File? = tryOrNull {
    val filename = UUID.randomUUID().toString()
    val mediaType = MediaType.ofUri(this, context.contentResolver)
    val file = File(dir, "$filename.${mediaType?.fileExtension ?: "tmp"}")
    ContentResolverUtil.getContentInputStream(context, this, file)
    file
}