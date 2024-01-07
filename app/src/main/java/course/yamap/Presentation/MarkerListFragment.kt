package course.yamap.Presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import course.yamap.R
import course.yamap.databinding.FragmentMarkerListBinding

class MarkerListFragment : Fragment() {
    private var _binding: FragmentMarkerListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMarkerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val controller = findNavController()
        binding.addFloatingAction.setOnClickListener{controller.navigate(R.id.addInfoFragment2)}


        //binding.backFloatingActionButton4.setOnClickListener { controller.navigate(R.id.) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
