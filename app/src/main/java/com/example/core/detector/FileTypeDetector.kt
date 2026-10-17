package com.example.core.detector

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.data.model.FileType
import java.io.InputStream
import java.util.zip.ZipInputStream

object FileTypeDetector {

    fun detect(
        context: Context,
        uri: Uri,
        fileName: String? = null,
        providedMimeType: String? = null
    ): FileType {
        // 1. Try file name extension if available
        val extFromName = fileName?.substringAfterLast('.', "")
        if (!extFromName.isNullOrEmpty()) {
            val typeFromExt = FileType.fromExtension(extFromName)
            if (typeFromExt != FileType.UNKNOWN) {
                return typeFromExt
            }
        }

        // 2. Try URI extension
        val uriPath = uri.lastPathSegment
        val extFromUri = uriPath?.substringAfterLast('.', "")
        if (!extFromUri.isNullOrEmpty()) {
            val typeFromUriExt = FileType.fromExtension(extFromUri)
            if (typeFromUriExt != FileType.UNKNOWN) {
                return typeFromUriExt
            }
        }

        // 3. Try ContentResolver MIME type
        val contentResolver: ContentResolver = context.contentResolver
        val mime = providedMimeType ?: contentResolver.getType(uri)
        if (!mime.isNullOrBlank()) {
            val typeFromMime = FileType.fromMimeType(mime)
            if (typeFromMime != FileType.UNKNOWN) {
                return typeFromMime
            }
            val extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
            if (!extFromMime.isNullOrEmpty()) {
                val typeFromExt = FileType.fromExtension(extFromMime)
                if (typeFromExt != FileType.UNKNOWN) {
                    return typeFromExt
                }
            }
        }

        // 4. Magic number inspection via stream
        return try {
            contentResolver.openInputStream(uri)?.use { stream ->
                detectFromStream(stream)
            } ?: FileType.UNKNOWN
        } catch (_: Exception) {
            FileType.UNKNOWN
        }
    }

    fun detectFromStream(stream: InputStream): FileType {
        val header = ByteArray(512)
        val bytesRead = stream.read(header)
        if (bytesRead <= 0) return FileType.UNKNOWN

        // PDF check (%PDF)
        if (bytesRead >= 4 &&
            header[0] == 0x25.toByte() && header[1] == 0x50.toByte() &&
            header[2] == 0x44.toByte() && header[3] == 0x46.toByte()
        ) {
            return FileType.PDF
        }

        // PNG check (89 50 4E 47 0D 0A 1A 0A)
        if (bytesRead >= 8 &&
            header[0] == 0x89.toByte() && header[1] == 0x50.toByte() &&
            header[2] == 0x4E.toByte() && header[3] == 0x47.toByte()
        ) {
            return FileType.IMAGE
        }

        // JPEG check (FF D8 FF)
        if (bytesRead >= 3 &&
            (header[0] and 0xFF.toByte()) == 0xFF.toByte() &&
            (header[1] and 0xFF.toByte()) == 0xD8.toByte() &&
            (header[2] and 0xFF.toByte()) == 0xFF.toByte()
        ) {
            return FileType.IMAGE
        }

        // GIF check (GIF87a or GIF89a)
        if (bytesRead >= 6 &&
            header[0] == 'G'.code.toByte() && header[1] == 'I'.code.toByte() &&
            header[2] == 'F'.code.toByte() && header[3] == '8'.code.toByte()
        ) {
            return FileType.IMAGE
        }

        // BMP check (BM)
        if (bytesRead >= 2 && header[0] == 'B'.code.toByte() && header[1] == 'M'.code.toByte()) {
            return FileType.IMAGE
        }

        // WEBP check (RIFF....WEBP)
        if (bytesRead >= 12 &&
            header[0] == 'R'.code.toByte() && header[1] == 'I'.code.toByte() &&
            header[2] == 'F'.code.toByte() && header[3] == 'F'.code.toByte() &&
            header[8] == 'W'.code.toByte() && header[9] == 'E'.code.toByte() &&
            header[10] == 'B'.code.toByte() && header[11] == 'P'.code.toByte()
        ) {
            return FileType.IMAGE
        }

        // RTF check ({\rtf)
        val headerString = String(header, 0, bytesRead.coerceAtMost(256), Charsets.UTF_8).trimStart()
        if (headerString.startsWith("{\\rtf")) {
            return FileType.RTF
        }

        // ZIP container check (PK\x03\x04 or PK\x05\x06) - Could be DOCX, XLSX, PPTX, ODT, ODS, ODP
        if (bytesRead >= 4 &&
            header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() &&
            (header[2] == 0x03.toByte() || header[2] == 0x05.toByte())
        ) {
            // Note: deeper ZIP inspection can be performed if needed, but defaults to DOCX / ZIP
            return FileType.DOCX
        }

        // JSON check
        if (headerString.startsWith("{") || headerString.startsWith("[")) {
            return FileType.JSON
        }

        // XML or HTML check
        if (headerString.startsWith("<?xml")) {
            return FileType.XML
        }
        val lower = headerString.lowercase()
        if (lower.startsWith("<!doctype html") || lower.startsWith("<html")) {
            return FileType.HTML
        }
        if (headerString.startsWith("<") && headerString.contains(">")) {
            return FileType.XML
        }

        // Text / CSV fallback
        val isMostlyPrintable = header.take(bytesRead).count { it in 32..126 || it == 10.toByte() || it == 13.toByte() || it == 9.toByte() } > (bytesRead * 0.9)
        if (isMostlyPrintable) {
            if (headerString.lines().take(5).any { it.contains(",") && it.split(",").size >= 2 }) {
                return FileType.CSV
            }
            return FileType.TXT
        }

        return FileType.UNKNOWN
    }

    private infix fun Byte.and(other: Byte): Byte = (this.toInt() and other.toInt()).toByte()
}
