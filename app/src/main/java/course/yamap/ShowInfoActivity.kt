package course.yamap

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import course.yamap.Data.DataBase.AppDatabase
import course.yamap.Data.DataBase.MarkerEntity
import course.yamap.databinding.ActivityShowInfoBinding

class ShowInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivityShowInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val databse = AppDatabase.getDatabase(this)

        binding.saveFloatingActionButton.setOnClickListener {
            val marker = MarkerEntity(null,
                binding.descriptionEditText.text.toString(),
                binding.commentsEditText.text.toString(),
                binding.commentsEditText.text.toString(),
                //getBitmapFromImageView(binding.pictureImageView)
                )
            Thread {
                databse.markerDao().insertMarker(marker)
            }.start()
        }
    }

    // Функция для извлечения битмапа из ImageView
    private fun getBitmapFromImageView(imageView: ImageView): Bitmap? {
        imageView.isDrawingCacheEnabled = true
        imageView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        imageView.layout(0, 0, imageView.measuredWidth, imageView.measuredHeight)
        imageView.buildDrawingCache(true)

        val bitmap = Bitmap.createBitmap(imageView.drawingCache)
        imageView.isDrawingCacheEnabled = false

        return bitmap
    }
}
