package se.onemanstudio.playaroundwithai.data.dream.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import se.onemanstudio.playaroundwithai.data.dream.data.local.dao.DreamsDao
import se.onemanstudio.playaroundwithai.data.dream.data.local.entity.DreamEntity

@Database(entities = [DreamEntity::class], version = 1, exportSchema = false)
abstract class DreamDatabase : RoomDatabase() {
    abstract fun dreamsDao(): DreamsDao
}
