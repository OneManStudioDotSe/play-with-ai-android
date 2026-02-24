package se.onemanstudio.playaroundwithai.feature.chat.data.remote.dto

import com.google.firebase.firestore.PropertyName

data class PromptFirestoreDto(
    @get:PropertyName("text") @set:PropertyName("text") var text: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = 0L
)
