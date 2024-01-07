package course.yamap.Data.DataBase

import androidx.lifecycle.LiveData
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

    @Query("SELECT * FROM markers WHERE id=:id LIMIT 1")
    fun findById(id: Int): LiveData<MarkerEntity>

    @Query("SELECT * FROM markers ORDER BY id DESC LIMIT 1")
    suspend fun getLastMarker(): MarkerEntity?

    @Delete
    suspend fun deleteMarker(marker: MarkerEntity)

    @Update
    suspend fun update(marker: MarkerEntity)
}