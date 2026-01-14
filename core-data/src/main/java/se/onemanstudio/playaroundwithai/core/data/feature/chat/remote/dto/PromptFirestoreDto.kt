package se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto

import com.google.firebase.firestore.PropertyName

data class PromptFirestoreDto(
    @get:PropertyName("text") @set:PropertyName("text") var text: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = 0L,
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "anonymous"
)
