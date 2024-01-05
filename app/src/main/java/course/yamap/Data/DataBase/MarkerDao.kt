package course.yamap.Data.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MarkerDao {
    @Insert
    fun insertMarker(marker: MarkerEntity)

    @Query("SELECT * FROM markers")
    suspend fun getAllMarkers(): List<MarkerEntity>

    @Delete
    suspend fun deleteMarker(marker: MarkerEntity)

    @Update
    suspend fun update(marker: MarkerEntity)

}