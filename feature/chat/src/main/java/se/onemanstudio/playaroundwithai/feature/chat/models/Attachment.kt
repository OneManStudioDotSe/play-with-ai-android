package se.onemanstudio.playaroundwithai.feature.chat.models

import android.net.Uri
import se.onemanstudio.playaroundwithai.data.chat.domain.model.AnalysisType

@androidx.compose.runtime.Immutable
sealed class Attachment {
    data class Image(val uri: Uri, val analysisType: AnalysisType) : Attachment()
    data class Document(val uri: Uri) : Attachment()
}
