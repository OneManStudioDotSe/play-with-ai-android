package se.onemanstudio.playaroundwithai.feature.chat.util

import android.app.Application
import android.content.Context
import se.onemanstudio.playaroundwithai.feature.chat.R
import javax.inject.Inject

class ResourceProvider @Inject constructor(private val context: Context) {

    fun getFallbackSuggestions(): List<String> {
        return listOf(
            context.getString(R.string.fallback_suggestion_joke),
            context.getString(R.string.fallback_suggestion_physics),
            context.getString(R.string.fallback_suggestion_roast)
        )
    }

    fun getLocalSaveFailedMessage(): String {
        return context.getString(R.string.local_save_failed)
    }

    fun getLocalUpdateFailedMessage(): String {
        return context.getString(R.string.local_update_failed)
    }
}
