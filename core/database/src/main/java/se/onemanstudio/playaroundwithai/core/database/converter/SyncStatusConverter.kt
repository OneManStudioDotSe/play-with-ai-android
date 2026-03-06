package se.onemanstudio.playaroundwithai.core.database.converter

import androidx.room.TypeConverter
import se.onemanstudio.playaroundwithai.core.database.entity.SyncStatus

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
