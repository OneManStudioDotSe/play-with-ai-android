package se.onemanstudio.playaroundwithai.data.chat.data.local.converter

import androidx.room.TypeConverter
import se.onemanstudio.playaroundwithai.data.chat.domain.model.SyncStatus

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(statusString: String): SyncStatus {
        return SyncStatus.valueOf(statusString)
    }
}
