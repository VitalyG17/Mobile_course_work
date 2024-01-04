package course.yamap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.room.Room
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
import course.yamap.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity(), UserLocationObjectListener, CameraListener {
    private lateinit var binding: ActivityMainBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.mapview.map.addTapListener(geoObjectTapListener) // Добавляем слушатель тапов по объектам
        binding.mapview.map.addInputListener(inputListener) // Добавляем слушатель тапов по карте с извлечением информации об улицах
        mapObjectCollection = binding.mapview.map.mapObjects // Инициализируем коллекцию различных объектов на карте
        val view = binding.root
        setContentView(view)

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

            Toast.makeText(applicationContext, street, Toast.LENGTH_SHORT).show()
        }
        override fun onSearchError(p0: com.yandex.runtime.Error) {
            TODO("Not yet implemented")
        }
    }

    //Установка маркера
    private fun setMarker(pointIn: Point) {
        val markerImageProvider = ImageProvider.fromResource(this, marker)
        val placemarkMapObject = mapObjectCollection.addPlacemark(pointIn, markerImageProvider)
        placemarkMapObject.opacity = 0.9f // Прозрачность
        placemarkMapObject.addTapListener(object : MapObjectTapListener {
            override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
                // Обработка нажатия на маркер
                Toast.makeText(applicationContext, "Любимое место", Toast.LENGTH_SHORT).show()
                return true
            }
        })
        markerDataList[Num] = placemarkMapObject // Хранение меток
        Num += 1
    }

    //Cлушатель нажатий одиночного и продолжительного
    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            searchSession = searchManager.submit(point, 20, SearchOptions(), searchListener)
        }
        override fun onMapLongTap(map: Map, point: Point) {
            setMarker(point)
        }
    }

    // Нажатие на кнопку удаления маркера
    private fun clickHeartButton() {
        if (markerDataList.isNotEmpty()) {
            // Получение ключа последнего добавленного маркера
            val lastMarkerKey = markerDataList.keys.last()
            // Получение маркера из списка и удаление его
            val lastMarker = markerDataList.remove(lastMarkerKey)
            lastMarker?.let {
                // Удаление маркера из коллекции объектов на карте
                binding.mapview.map.mapObjects.remove(it)
                Toast.makeText(applicationContext, "Метка успешно удалена", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(applicationContext, "Нет меток для удаления", Toast.LENGTH_SHORT).show()
        }
    }

    //Проверка разрешений на определение местоположения
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
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
        val favoritePlaceFab = binding.favoritePlace
        binding.mapview.map.logo.setAlignment(mapLogoAlignment)
        binding.userLocationFab.setOnClickListener {
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

        cameraUserPosition()
        permissionLocation = true
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
            binding.mapview.map.move(CameraPosition(startLocation, zoomValue, 0f, 0f),
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
        binding.userLocationFab.setImageResource(R.drawable.ic_my_location_black_24dp)

        followUserLocation = false
    }

    /* Сбрасывает якорь.
    Вызывается, когда followUserLocation = false.*/
    private fun noAnchor() {
        userLocationLayer.resetAnchor()
        binding.userLocationFab.setImageResource(R.drawable.ic_location_searching_black_24dp)
    }

    //Сохранение API-ключа, если активность потребуется воссоздать
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(MAPKIT_API_KEY,true)
    }

    //Проверяет наличие API-ключа в активности. Для проверки его единоразовой установки
    private fun setApiKey(savedInstanceState: Bundle?) {
        val haveApiKey = savedInstanceState?.getBoolean(MAPKIT_API_KEY) ?: false
        if (!haveApiKey) {
            MapKitFactory.setApiKey(MAPKIT_API_KEY)
        }
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        setAnchor()
        userLocationView.pin.setIcon(ImageProvider.fromResource(this, R.drawable.user_arrow))
        userLocationView.arrow.setIcon(ImageProvider.fromResource(this, R.drawable.user_arrow))
        userLocationView.accuracyCircle.fillColor = Color.CYAN
    }


    private fun createBitmapFromVector(@DrawableRes vectorDrawableId: Int): Bitmap {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorDrawableId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return bitmap
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
    }

    companion object {
        const val MAPKIT_API_KEY = "a9e6fdbd-c9ab-4668-b9c3-ef111ab8f7f0"
        val marker = R.drawable.ic_heart_png
        val markerDataList = HashMap<Int, PlacemarkMapObject>()
        var Num : Int = 0
    }
}




