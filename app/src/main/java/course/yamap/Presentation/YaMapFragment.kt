package course.yamap.Presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment
import com.yandex.mapkit.logo.VerticalAlignment
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.search.Address
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import course.yamap.Data.DataBase.AppDatabase
import course.yamap.Data.DataBase.MarkerDao
import course.yamap.Data.DataBase.MarkerEntity
import course.yamap.MainActivity
import course.yamap.R
import course.yamap.databinding.ActivityMainBinding
import course.yamap.databinding.FragmentYaMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class YaMapFragment : Fragment(), UserLocationObjectListener, CameraListener {

    private lateinit var binding: FragmentYaMapBinding
    private lateinit var checkLocationPermission: ActivityResultLauncher<Array<String>>
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var mapObjectCollection: MapObjectCollection // Коллекция различных объектов на карте
    lateinit var searchManager: SearchManager
    lateinit var searchSession: Session

    private var belgorodLocation = Point(50.595289, 36.587130) // Координаты Белгорода
    private var startLocation = Point(0.0, 0.0)

    private val zoomValue: Float = 14.5f // Величина зума

    private var permissionLocation = false //Есть ли разрешение на определение местоположения.
    private var followUserLocation = false //Включен ли режим следования за пользователем на карте.

    private lateinit var navController: NavController
    private lateinit var markerDao: MarkerDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        setApiKey(savedInstanceState)
        binding = FragmentYaMapBinding.inflate(inflater,container,false)
        binding.mapview.map.addTapListener(geoObjectTapListener) // Добавляем слушатель тапов по объектам
        binding.mapview.map.addInputListener(inputListener) // Добавляем слушатель тапов по карте с извлечением информации об улицах
        mapObjectCollection = binding.mapview.map.mapObjects // Инициализируем коллекцию различных объектов на карте
        markerDao = AppDatabase.getDatabase(requireContext()).markerDao()

        checkLocationPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                onMapReady()
            }
        }

        checkPermission()
        userInterface()

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)

        // Найти NavController
        navController = findNavController()

        binding.listFloatingActionButton3.setOnClickListener {
            // Выполнить переход к фрагменту MarkerListFragment2
            navController.navigate(R.id.markerListFragment2)
        }

        binding.saveFloatingActionButton2.setOnClickListener {
            // Выполнить переход к фрагменту addInfoFragment2
            navController.navigate(R.id.addInfoFragment2)
        }

        return binding.root
    }

    //Установка маркера
    private fun setMarker(pointIn: Point) {
        val markerImageProvider = ImageProvider.fromResource(requireContext(), marker)
        val placemarkMapObject = mapObjectCollection.addPlacemark(pointIn, markerImageProvider)
        placemarkMapObject.opacity = 0.9f // Прозрачность
        placemarkMapObject.addTapListener(object : MapObjectTapListener {
            override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
                // Обработка нажатия на маркер
                Toast.makeText(requireContext(), "Любимое место", Toast.LENGTH_SHORT).show()
                //navController.navigate(R.id.addInfoFragment2)
                return true
            }
        })
        GlobalScope.launch(Dispatchers.IO) {
            markerDao.insertMarker(
                MarkerEntity(
                    null, null, null, null, pointIn.latitude, pointIn.longitude
                )
            )
        }
    }

    //Cлушатель нажатий одиночного и продолжительного
    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            searchSession = searchManager.submit(point, 20, SearchOptions(), searchListener)
        }
        override fun onMapLongTap(map: Map, point: Point) {
            setMarker(point)
            //navController.navigate(R.id.addInfoFragment2)
            //loadMarkersFromDatabase()
        }
    }

    // Нажатие на кнопку удаления маркера
    private fun clickHeartButton() {
        GlobalScope.launch(Dispatchers.IO) {
            val lastMarker = markerDao.getLastMarker()
            lastMarker?.let {
                markerDao.deleteMarker(it)
                withContext(Dispatchers.Main) {
                    removeLastAddedMarker()
                    Toast.makeText(requireContext(), "Метка успешно удалена", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Нет меток для удаления", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Функция для удаления последнего добавленного маркера
    private fun removeLastAddedMarker() {
        val lastMarkerId = markerDataList.keys.lastOrNull()
        lastMarkerId?.let {
            val lastMarker = markerDataList.remove(lastMarkerId)
            lastMarker?.let {
                binding.mapview.map.mapObjects.remove(it)

                GlobalScope.launch(Dispatchers.IO) {
                    markerDao.deleteMarker(it.userData as MarkerEntity)
                }
            }
        }
    }
    private fun loadMarkersFromDatabase() {
        GlobalScope.launch(Dispatchers.IO) {
            val markers = markerDao.getAllMarkers()
            withContext(Dispatchers.Main) {
                displayMarkersOnMap(markers)
            }
        }
    }
    private fun displayMarkersOnMap(markers: List<MarkerEntity>) {
        for (marker in markers) {
            val point = Point(marker.latitude, marker.longitude)
            setMarker(point)
        }
    }

    //Проверка разрешений на определение местоположения
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onMapReady()
        } else {
            checkLocationPermission.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    /*Устанавливает положение логотипа Якарты.
    Устанавливает обработчик кнопки отображения местоположения*/
    private fun userInterface() {
        val mapLogoAlignment = Alignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
        val favoritePlaceFab = binding.favoritePlace2
        binding.mapview.map.logo.setAlignment(mapLogoAlignment)
        binding.userLocationFab2.setOnClickListener {
            if (permissionLocation) {
                cameraUserPosition()
                followUserLocation = true
            } else {
                moveToBelgorodLocation()
            }

        }
        favoritePlaceFab.setOnClickListener {
            clickHeartButton()
        }
    }

    private fun onMapReady() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(binding.mapview.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(this)
        binding.mapview.map.addCameraListener(this)
        permissionLocation = true
        cameraUserPosition()
    }

    //Перемещение камеры к текущему местоположению пользователя на карте
    private fun cameraUserPosition() {
        if (userLocationLayer.cameraPosition() != null) {
            startLocation = userLocationLayer.cameraPosition()!!.target
            binding.mapview.map.move(
                CameraPosition(startLocation, zoomValue, 0.0f, 0.0f), // Позиция камеры
                Animation(Animation.Type.SMOOTH, 0.5f), null // Анимация при переходе на точку
            )
        } else {
            binding.mapview.map.move(
                CameraPosition(startLocation, zoomValue, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 3.5f), null // Анимация при переходе на точку
            )
        }
    }

    //Переход камеры к координатам Белгорода
    private fun moveToBelgorodLocation() {
        binding.mapview.map.move(
            CameraPosition(belgorodLocation, zoomValue, 0.0f, 0.0f), // Позиция камеры
            Animation(Animation.Type.SMOOTH, 1.5f), null // Анимация при переходе
        )
    }

    //Обработчик изменения позиции камеры на карте.
    override fun onCameraPositionChanged(
        map: Map, cPos: CameraPosition, cUpd: CameraUpdateReason, finish: Boolean
    ) {
        if (finish) {
            if (followUserLocation) {
                setAnchor()
            }
        } else {
            if (!followUserLocation) {
                noAnchor()
            }
        }
    }

    /* Устанавка якоря для отображения местоположения пользователя.
   Вызывается, когда followUserLocation = true.*/
    private fun setAnchor() {
        // Установка якоря в центр экрана с учетом высоты и ширины карты
        userLocationLayer.setAnchor(
            PointF(
                (binding.mapview.width * 0.5).toFloat(), (binding.mapview.height * 0.5).toFloat()
            ),
            PointF(
                (binding.mapview.width * 0.5).toFloat(), (binding.mapview.height * 0.83).toFloat()
            )
        )
        binding.userLocationFab2.setImageResource(R.drawable.ic_my_location_black_24dp)

        followUserLocation = false
    }

    /* Сбрасывает якорь.
    Вызывается, когда followUserLocation = false.*/
    private fun noAnchor() {
        userLocationLayer.resetAnchor()
        binding.userLocationFab2.setImageResource(R.drawable.ic_location_searching_black_24dp)
    }

    //Выделение объекта при нажатии
    private val geoObjectTapListener = object : GeoObjectTapListener {
        override fun onObjectTap(geoObjectTapEvent: GeoObjectTapEvent): Boolean {
            val selectionMetadata: GeoObjectSelectionMetadata = geoObjectTapEvent
                .geoObject
                .metadataContainer
                .getItem(GeoObjectSelectionMetadata::class.java)
            binding.mapview.map.selectGeoObject(selectionMetadata)
            return false
        }
    }

    //Метаданные информации об улице
    private val searchListener = object : Session.SearchListener {
        override fun onSearchResponse(response: Response) {
            val street = response.collection.children.firstOrNull()?.obj
                ?.metadataContainer
                ?.getItem(ToponymObjectMetadata::class.java)
                ?.address
                ?.components
                ?.firstOrNull { it.kinds.contains(Address.Component.Kind.STREET)}
                ?.name ?: "Информация об улице не найдена"

            Toast.makeText(requireContext(), street, Toast.LENGTH_SHORT).show()
        }
        override fun onSearchError(p0: com.yandex.runtime.Error) {
        }
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        setAnchor()
        userLocationView.pin.setIcon(ImageProvider.fromResource(requireContext(), R.drawable.user_arrow))
        userLocationView.arrow.setIcon(ImageProvider.fromResource(requireContext(), R.drawable.user_arrow))
        userLocationView.accuracyCircle.fillColor = Color.CYAN
    }
    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}
    override fun onObjectRemoved(p0: UserLocationView) {}


    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        binding.mapview.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
        loadMarkersFromDatabase()
    }

    override fun onResume() {
        super.onResume()
        clearMap()
    }

    private fun clearMap() {
        binding.mapview.map.mapObjects.clear()
        markerDataList.clear()
    }

    companion object {
        val marker = R.drawable.ic_heart_png
        val markerDataList = HashMap<Int, PlacemarkMapObject>()
    }
}