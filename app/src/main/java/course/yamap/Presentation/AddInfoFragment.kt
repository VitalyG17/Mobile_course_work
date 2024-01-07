package course.yamap.Presentation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import course.yamap.Data.DataBase.AppDatabase
import course.yamap.Data.DataBase.MarkerEntity
import course.yamap.databinding.FragmentAddInfoBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddInfoFragment : Fragment() {
    private var selectedImageBitmap: Bitmap? = null
    private lateinit var database: AppDatabase
    private lateinit var binding: FragmentAddInfoBinding // Добавлено объявление binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddInfoBinding.inflate(inflater, container, false) // Используем binding
        database = AppDatabase.getDatabase(requireContext())

        val descriptionEditText: EditText = binding.descriptionEditText
        val commentsEditText: EditText = binding.commentsEditText
        val pictureImageView: ImageView = binding.pictureImageView
        val saveFloatingActionButton: FloatingActionButton = binding.saveFloatingActionButton

        saveFloatingActionButton.setOnClickListener {
            if (areFieldsValid(descriptionEditText, commentsEditText)) {
                val marker = MarkerEntity(
                    null,
                    descriptionEditText.text.toString(),
                    commentsEditText.text.toString(),
                    selectedImageBitmap
                )

                Thread {
                    database.markerDao().insertMarker(marker)
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Запись добавлена", Toast.LENGTH_LONG).show()
                    }
                }.start()
            } else {
                Toast.makeText(requireContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        pictureImageView.setOnClickListener {
            openGallery()
        }

        return binding.root // Используем binding.root вместо view
    }

    private fun areFieldsValid(descriptionEditText: EditText, commentsEditText: EditText): Boolean {
        return descriptionEditText.text.isNotBlank() &&
                commentsEditText.text.isNotBlank()
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
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
        }
    }
}
