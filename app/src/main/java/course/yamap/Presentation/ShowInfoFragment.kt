package course.yamap.Presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import course.yamap.Data.DataBase.AppDatabase
import course.yamap.R
import course.yamap.databinding.FragmentAddInfoBinding
import course.yamap.databinding.FragmentShowInfoBinding


class ShowInfoFragment : Fragment() {

    private lateinit var binding: FragmentShowInfoBinding
    private lateinit var navController: NavController
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShowInfoBinding.inflate(inflater, container, false)
        database = AppDatabase.getDatabase(requireContext())


        navController = findNavController()

        binding.backFloatingActionButton2.setOnClickListener {
            navController.navigate(R.id.markerListFragment2)
        }

        val markerId = arguments?.getInt("markerId", -1) ?: -1

        return binding.root
    }
}

