package course.yamap.Presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import course.yamap.R
import course.yamap.databinding.FragmentMarkerListBinding

class MarkerListFragment : Fragment() {
    private var _binding: FragmentMarkerListBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMarkerListBinding.inflate(inflater, container, false)

        navController = findNavController()
        binding.backFloatingActionButton4.setOnClickListener {
            // Выполнить переход к фрагменту yaMapFragment
            navController.navigate(R.id.yaMapFragment)
        }
        binding.addFloatingAction.setOnClickListener {
            navController.navigate(R.id.addInfoFragment2)
        }

        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val controller = findNavController()
//        //binding.addFloatingAction.setOnClickListener{controller.navigate(R.id.addInfoFragment2)}
//
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
