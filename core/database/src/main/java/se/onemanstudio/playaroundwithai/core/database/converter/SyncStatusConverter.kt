package se.onemanstudio.playaroundwithai.core.database.converter

import androidx.room.TypeConverter
import se.onemanstudio.playaroundwithai.core.database.entity.SyncStatus
import timber.log.Timber

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = try {
        SyncStatus.valueOf(value)
    } catch (e: IllegalArgumentException) {
        Timber.e(e, "Unknown SyncStatus value '$value', defaulting to Pending")
        SyncStatus.Pending
    }
}
