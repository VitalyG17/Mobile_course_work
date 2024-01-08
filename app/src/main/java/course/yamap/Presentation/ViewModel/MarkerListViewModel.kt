package course.yamap.Presentation.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import course.yamap.Data.DataBase.AppDatabase
import course.yamap.Data.DataBase.MarkerDao
import course.yamap.Data.DataBase.MarkerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.yandex.mapkit.geometry.Point

class MarkerListViewModel(ctx: Application) : AndroidViewModel(ctx) {

    private lateinit var markerDao: MarkerDao
    private lateinit var database: AppDatabase
    public val markerList: LiveData<List<MarkerEntity>>

    init {
        database = AppDatabase.getDatabase(ctx)
        markerDao = database.markerDao()
        markerList = markerDao.watchAllMarkers()
    }

    fun addMarkers(pointIn: Point) {
        viewModelScope.launch(Dispatchers.IO) {
            markerDao.insertMarker(
                MarkerEntity(
                    null, null, null, null, pointIn.latitude, pointIn.longitude
                )
            )
        }
    }
    fun deleteLastMarker() {
        val lastMarker = markerList.value!!.last()
        viewModelScope.launch(Dispatchers.IO) {
            markerDao.deleteMarker(lastMarker)
        }
    }
}
