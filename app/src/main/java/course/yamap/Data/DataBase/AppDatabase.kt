package course.yamap.Data.DataBase

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MarkerEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun markerDao(): MarkerDao
}