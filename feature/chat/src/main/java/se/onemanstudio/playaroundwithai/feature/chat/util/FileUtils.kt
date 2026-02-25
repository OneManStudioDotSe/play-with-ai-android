package se.onemanstudio.playaroundwithai.feature.chat.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

class FileUtils @Inject constructor(
    @ApplicationContext private val context: Context
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

    private fun readTextFromUri(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        } ?: throw FileNotFoundException("Could not open input stream for URI: $uri")
    }

    suspend fun uriToByteArray(uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: IOException) {
            Timber.w(e, "Error reading image from URI: $uri")
            null
        } catch (e: SecurityException) {
            Timber.w(e, "Security exception reading image from URI: $uri")
            null
        }
    }
}
