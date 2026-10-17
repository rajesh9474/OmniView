package com.example.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PdfEngine(private val context: Context) {

    private var fileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var tempPdfFile: File? = null

    val pageCount: Int
        get() = pdfRenderer?.pageCount ?: 0

    suspend fun open(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        close()
        try {
            val pfd: ParcelFileDescriptor? = when (uri.scheme) {
                "file" -> {
                    val file = File(uri.path ?: "")
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                }
                "content" -> {
                    try {
                        context.contentResolver.openFileDescriptor(uri, "r")
                    } catch (_: Exception) {
                        // Fallback: Copy content stream to cache file
                        copyUriToCache(uri)
                    }
                }
                else -> {
                    copyUriToCache(uri)
                }
            }

            if (pfd == null) {
                return@withContext Result.failure(Exception("Unable to open file descriptor for PDF"))
            }

            fileDescriptor = pfd
            pdfRenderer = PdfRenderer(pfd)
            Result.success(pdfRenderer?.pageCount ?: 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun openFromStream(inputStream: InputStream): Result<Int> = withContext(Dispatchers.IO) {
        close()
        try {
            val tempFile = File.createTempFile("omniview_pdf_", ".pdf", context.cacheDir)
            tempPdfFile = tempFile
            FileOutputStream(tempFile).use { out ->
                inputStream.copyTo(out)
            }
            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            fileDescriptor = pfd
            pdfRenderer = PdfRenderer(pfd)
            Result.success(pdfRenderer?.pageCount ?: 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyUriToCache(uri: Uri): ParcelFileDescriptor? {
        val stream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("omniview_cached_", ".pdf", context.cacheDir)
        tempPdfFile = tempFile
        FileOutputStream(tempFile).use { out ->
            stream.copyTo(out)
        }
        return ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    suspend fun renderPage(pageIndex: Int, scaleFactor: Float = 1.5f): Bitmap? = withContext(Dispatchers.IO) {
        val renderer = pdfRenderer ?: return@withContext null
        if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

        var page: PdfRenderer.Page? = null
        try {
            page = renderer.openPage(pageIndex)
            val width = (page.width * scaleFactor).toInt().coerceAtLeast(1)
            val height = (page.height * scaleFactor).toInt().coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            // Fill with pure white background
            bitmap.eraseColor(Color.WHITE)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } catch (e: Exception) {
            null
        } finally {
            try {
                page?.close()
            } catch (_: Exception) {}
        }
    }

    fun close() {
        try {
            pdfRenderer?.close()
        } catch (_: Exception) {}
        pdfRenderer = null

        try {
            fileDescriptor?.close()
        } catch (_: Exception) {}
        fileDescriptor = null

        try {
            tempPdfFile?.delete()
        } catch (_: Exception) {}
        tempPdfFile = null
    }
}
