package se.onemanstudio.playaroundwithai.feature.chat.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

private const val JPEG_QUALITY = 100

class FileUtils @Inject constructor(
    private val context: Context
) {
    suspend fun extractFileContent(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val content = readTextFromUri(uri)
            Result.success(content)
        } catch (e: FileNotFoundException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: SecurityException) {
            Result.failure(e)
        }
    }

    @Throws(IOException::class, SecurityException::class)
    private fun readTextFromUri(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        } ?: throw FileNotFoundException("Could not open input stream for URI: $uri")
    }

    suspend fun uriToByteArray(uri: Uri): ByteArray? = withContext(Dispatchers.Default) {
        try {
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bos)
            bos.toByteArray()
        } catch (e: IOException) {
            Timber.d("Error decoding image: ${e.message}")
            null
        } catch (e: SecurityException) {
            Timber.d("Security exception decoding image: ${e.message}")
            null
        }
    }
}
