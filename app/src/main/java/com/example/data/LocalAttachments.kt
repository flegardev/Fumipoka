package com.example.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.UUID

internal object LocalAttachments {
    private const val DIRECTORY = "attachments"
    private const val PREFIX = "fumipoka-attachment:"
    const val MAX_BYTES = 10L * 1024 * 1024
    private val extensions = mapOf(
        "image/jpeg" to "jpg", "image/png" to "png", "image/gif" to "gif",
        "image/webp" to "webp", "application/pdf" to "pdf", "text/plain" to "txt"
    )
    private val fileNamePattern = Regex("[0-9a-f-]{36}\\.(jpg|png|gif|webp|pdf|txt)")

    data class Saved(val token: String, val title: String, val type: ReferenceType)

    fun import(context: Context, source: Uri): Saved {
        require(source.scheme == ContentResolver.SCHEME_CONTENT) { "Choose a file from an Android document provider." }
        val resolver = context.contentResolver
        val mime = resolver.getType(source)?.lowercase(Locale.ROOT).orEmpty()
        val extension = extensions[mime] ?: throw IllegalArgumentException("Choose a JPEG, PNG, GIF, WebP, PDF or text file.")
        val title = resolver.query(source, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)
                require(sizeColumn < 0 || cursor.isNull(sizeColumn) || cursor.getLong(sizeColumn) <= MAX_BYTES) {
                    "Files must be 10 MiB or smaller."
                }
                val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameColumn < 0) null else cursor.getString(nameColumn)
            }?.filterNot(Char::isISOControl)?.take(120)?.trim()?.ifBlank { null } ?: "Attachment"
        val directory = File(context.filesDir, DIRECTORY)
        check(directory.isDirectory || directory.mkdirs() || directory.isDirectory) { "Could not create attachment storage." }
        val target = File(directory, "${UUID.randomUUID()}.$extension")
        val staging = File.createTempFile("import-", ".tmp", directory)
        try {
            val input = resolver.openInputStream(source) ?: error("The shared file could not be opened.")
            input.buffered().use { stream ->
                stream.mark(16)
                val header = ByteArray(12)
                var length = 0
                while (length < header.size) {
                    val count = stream.read(header, length, header.size - length)
                    if (count <= 0) break
                    length += count
                }
                require(matchesMime(mime, header.copyOf(length))) { "The file contents do not match its reported type." }
                stream.reset()
                staging.outputStream().buffered().use { copyBounded(stream, it) }
            }
            check(staging.renameTo(target)) { "Could not save the attachment." }
        } finally {
            staging.delete()
        }
        val type = if (mime.startsWith("image/")) ReferenceType.IMAGE else ReferenceType.DOCUMENT
        return Saved(PREFIX + target.name, title, type)
    }

    internal fun matchesMime(mime: String, header: ByteArray): Boolean {
        fun startsWith(signature: String, offset: Int = 0) =
            header.size >= offset + signature.length &&
                signature.indices.all { (header[offset + it].toInt() and 0xff) == signature[it].code }
        return when (mime) {
            "image/jpeg" -> startsWith("\u00ff\u00d8\u00ff")
            "image/png" -> startsWith("\u0089PNG\r\n\u001a\n")
            "image/gif" -> startsWith("GIF87a") || startsWith("GIF89a")
            "image/webp" -> startsWith("RIFF") && startsWith("WEBP", 8)
            "application/pdf" -> startsWith("%PDF-")
            "text/plain" -> header.none { it == 0.toByte() }
            else -> false
        }
    }

    internal fun copyBounded(input: InputStream, output: OutputStream, limit: Long = MAX_BYTES) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            require(total <= limit) { "Files must be 10 MiB or smaller." }
            output.write(buffer, 0, read)
        }
        require(total > 0) { "The file is empty." }
    }

    fun isLocal(token: String): Boolean = token.startsWith(PREFIX)

    fun mimeType(token: String): String? {
        val extension = token.removePrefix(PREFIX).takeIf { isLocal(token) && fileNamePattern.matches(it) }
            ?.substringAfterLast('.') ?: return null
        return extensions.entries.firstOrNull { it.value == extension }?.key
    }

    fun readForBackup(context: Context, token: String): ByteArray {
        val file = resolve(context, token)?.takeIf { it.isFile } ?: error("An attachment is missing from the device.")
        val output = java.io.ByteArrayOutputStream()
        file.inputStream().buffered().use { copyBounded(it, output) }
        return output.toByteArray()
    }

    fun restoreFromBackup(context: Context, oldToken: String, bytes: ByteArray): String {
        val mime = requireNotNull(mimeType(oldToken)) { "Invalid attachment identifier." }
        require(bytes.isNotEmpty() && bytes.size <= MAX_BYTES && matchesMime(mime, bytes.copyOfRange(0, minOf(bytes.size, 12)))) {
            "Invalid attachment contents."
        }
        val directory = File(context.filesDir, DIRECTORY)
        check(directory.isDirectory || directory.mkdirs() || directory.isDirectory) { "Could not create attachment storage." }
        val file = File(directory, "${UUID.randomUUID()}.${extensions.getValue(mime)}")
        try {
            file.writeBytes(bytes)
        } catch (error: Exception) {
            file.delete()
            throw error
        }
        return PREFIX + file.name
    }

    fun openUri(context: Context, token: String): Uri? {
        val file = resolve(context, token)?.takeIf { it.isFile } ?: return null
        return FileProvider.getUriForFile(context, "${context.packageName}.attachments", file)
    }

    fun delete(context: Context, token: String) {
        resolve(context, token)?.delete()
    }

    private fun resolve(context: Context, token: String): File? {
        if (!isLocal(token)) return null
        val name = token.removePrefix(PREFIX).takeIf(fileNamePattern::matches) ?: return null
        return File(File(context.filesDir, DIRECTORY), name)
    }
}
