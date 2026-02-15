package se.onemanstudio.playaroundwithai.feature.chat.util

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

private const val JPEG_QUALITY = 100

class FileUtils @Inject constructor(
    private val application: Application
) {
    fun extractFileContent(uri: Uri): Result<String> {
        return try {
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
    fun readTextFromUri(uri: Uri): String {
        return application.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        } ?: throw FileNotFoundException("Could not open input stream for URI: $uri")
    }

    fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(application.contentResolver, uri))
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
