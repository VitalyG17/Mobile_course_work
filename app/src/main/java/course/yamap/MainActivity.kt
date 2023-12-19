package course.yamap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import course.yamap.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val startLocation = Point(50.595289, 36.587130) // Координаты Белгорода
    private val zoomValue: Float = 10.5f // Величина зума

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey(savedInstanceState)
        MapKitFactory.initialize(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        moveToStartLocation()
    }

    //Переход камеры к стартовому изображению
    private fun moveToStartLocation() {
        binding.mapview.map.move(
            CameraPosition(startLocation, zoomValue, 0.0f, 0.0f), // Позиция камеры
            Animation(Animation.Type.SMOOTH, 2.5f), null // Анимация при переходе на стартовую точку
        )
    }

    //Сохранение ключа, если активность потребуется воссоздать
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(MAPKIT_API_KEY,true)
    }

    private fun setApiKey(savedInstanceState: Bundle?) {
        val haveApiKey = savedInstanceState?.getBoolean(MAPKIT_API_KEY) ?: false
        if (!haveApiKey) {
            MapKitFactory.setApiKey(MAPKIT_API_KEY)
        }
    }

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
