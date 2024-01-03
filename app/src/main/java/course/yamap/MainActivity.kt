package course.yamap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import course.yamap.databinding.ActivityMainBinding
import java.io.IOException


class MainActivity : AppCompatActivity(), UserLocationObjectListener, CameraListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var checkLocationPermission: ActivityResultLauncher<Array<String>>
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var mapObjectCollection: MapObjectCollection // Коллекция различных объектов на карте
    private lateinit var placemarkMapObject: PlacemarkMapObject // Геопозиционированный объект (метка со значком) на карте
    private lateinit var placemarkMapObject1: PlacemarkMapObject

    private var belgorodLocation = Point(50.595289, 36.587130) // Координаты Белгорода
    private var startLocation = Point(0.0, 0.0)
    private var favoriteLocation = Point(37.422915, -122.088981)
    private var favoriteLocation1 = Point(37.421785, -122.079524)
    private val zoomValue: Float = 14.5f // Величина зума

    private var permissionLocation = false //Есть ли разрешение на определение местоположения.
    private var followUserLocation = false //Включен ли режим следования за пользователем на карте.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.mapview.map.addTapListener(geoObjectTapListener) // Добавляем слушатель тапов по объектам

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
        setMarkerInStartLocation()

    }

    private fun setMarkerInStartLocation() {
        val marker = createBitmapFromVector(R.drawable.ic_heart_svg)
        mapObjectCollection = binding.mapview.map.mapObjects // Инициализируем коллекцию различных объектов на карте
        placemarkMapObject = mapObjectCollection.addPlacemark(favoriteLocation, ImageProvider.fromBitmap(marker)) // Добавляем метку со значком
        placemarkMapObject.opacity = 0.9f // Устанавливка прозрачности метке
        placemarkMapObject.addTapListener(mapObjectTapListener)

        placemarkMapObject1 = mapObjectCollection.addPlacemark(favoriteLocation1, ImageProvider.fromBitmap(marker))
        placemarkMapObject1.opacity = 0.9f
        placemarkMapObject1.addTapListener(mapObjectTapListener)
    }

    //Отображение информации о месте
    private val mapObjectTapListener = object : MapObjectTapListener {
        override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
            Toast.makeText(applicationContext, "Любимое место", Toast.LENGTH_SHORT).show()
            return true
        }
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

    private fun clickHeartButton() {

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
    }
}


