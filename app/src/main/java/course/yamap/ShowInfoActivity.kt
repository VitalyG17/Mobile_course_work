package course.yamap

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import course.yamap.Data.DataBase.AppDatabase
import course.yamap.Data.DataBase.MarkerEntity
import course.yamap.databinding.ActivityShowInfoBinding

class ShowInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivityShowInfoBinding
    private var selectedImageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val database = AppDatabase.getDatabase(this)

        binding.saveFloatingActionButton.setOnClickListener {
            val marker = MarkerEntity(
                null,
                binding.descriptionEditText.text.toString(),
                binding.commentsEditText.text.toString(),
                selectedImageBitmap
            )
            Thread {
                database.markerDao().insertMarker(marker)
            }.start()
        }

        binding.pictureImageView.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImage.launch(intent)
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                binding.pictureImageView.setImageURI(uri)
                // Преобразование URI в Bitmap
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        }
    }
}
