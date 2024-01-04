package course.yamap.Data.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MarkerDao {
    @Insert
    suspend fun insertMarker(marker: MarkerEntity)

    @Query("SELECT * FROM markers")
    suspend fun getAllMarkers(): List<MarkerEntity>

    @Delete
    suspend fun deleteMarker(marker: MarkerEntity)
}