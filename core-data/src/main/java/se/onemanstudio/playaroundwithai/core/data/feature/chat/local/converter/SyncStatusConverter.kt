package se.onemanstudio.playaroundwithai.core.data.feature.chat.local.converter

import androidx.room.TypeConverter
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus

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
