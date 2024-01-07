package course.yamap.Presentation

import MarkerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import course.yamap.Data.DataBase.AppDatabase
import course.yamap.Data.DataBase.MarkerDao
import course.yamap.Data.DataBase.MarkerEntity
import course.yamap.R
import course.yamap.databinding.FragmentMarkerListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarkerListFragment : Fragment() {
    private var _binding: FragmentMarkerListBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var markerDao: MarkerDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMarkerListBinding.inflate(inflater, container, false)

        navController = findNavController()
        binding.backFloatingActionButton4.setOnClickListener {
            navController.navigate(R.id.yaMapFragment)
        }
        binding.addFloatingAction.setOnClickListener {
            navController.navigate(R.id.addInfoFragment2)
        }

        val appDatabase = AppDatabase.getDatabase(requireContext())
        markerDao = appDatabase.markerDao()

        val adapter = MarkerAdapter(requireContext(), mutableListOf())
        binding.rcView.adapter = adapter
        binding.rcView.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            val markerList = getMarkerListFromDatabase()
            adapter.setData(markerList)
        }

        return binding.root
    }

    private suspend fun getMarkerListFromDatabase(): List<MarkerEntity> {
        return withContext(Dispatchers.IO) {
            markerDao.getAllMarkers()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
