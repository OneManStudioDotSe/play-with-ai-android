package se.onemanstudio.playaroundwithai.feature.chat.data.local.converter

import androidx.room.TypeConverter
import se.onemanstudio.playaroundwithai.feature.chat.domain.model.SyncStatus

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
