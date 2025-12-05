package se.onemanstudio.playaroundwithai.feature.chat

import android.net.Uri
import se.onemanstudio.playaroundwithai.core.data.AnalysisType

sealed class Attachment {
    data class Image(val uri: Uri, val analysisType: AnalysisType) : Attachment()
    data class Document(val uri: Uri) : Attachment()
}
