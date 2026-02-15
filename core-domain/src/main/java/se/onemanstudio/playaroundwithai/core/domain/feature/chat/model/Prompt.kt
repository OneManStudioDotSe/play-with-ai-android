package se.onemanstudio.playaroundwithai.core.domain.feature.chat.model

import java.time.Instant

data class Prompt(
    val id: Long = 0,
    val text: String,
    val timestamp: Instant = Instant.now(),
    val syncStatus: SyncStatus = SyncStatus.Pending,
    val firestoreDocId: String? = null,
    val imageAttachment: ByteArray? = null,
    val documentAttachment: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Prompt

        if (id != other.id) return false
        if (text != other.text) return false
        if (timestamp != other.timestamp) return false
        if (syncStatus != other.syncStatus) return false
        if (firestoreDocId != other.firestoreDocId) return false
        if (imageAttachment != null) {
            if (other.imageAttachment == null) return false
            if (!imageAttachment.contentEquals(other.imageAttachment)) return false
        }
        else if (other.imageAttachment != null) return false
        if (documentAttachment != other.documentAttachment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + syncStatus.hashCode()
        result = 31 * result + (firestoreDocId?.hashCode() ?: 0)
        result = 31 * result + (imageAttachment?.contentHashCode() ?: 0)
        result = 31 * result + (documentAttachment?.hashCode() ?: 0)
        return result
    }
}
