package course.yamap.Data.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double
)
