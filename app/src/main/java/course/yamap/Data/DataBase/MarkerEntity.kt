package course.yamap.Data.DataBase

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val description: String,
    val comments: String,
    val image: String?
)
