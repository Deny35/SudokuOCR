package pl.denys.karol.sudokuocr.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pl.denys.karol.sudokuocr.DBViewModel
import pl.denys.karol.sudokuocr.databinding.FragmentMainBinding
import pl.denys.karol.sudokuocr.ui.adapter.ItemAdapter
import pl.denys.karol.sudokuocr.ui.adapter.ItemComparator

@AndroidEntryPoint
class MainFragment : Fragment() {
    private val viewModel: DBViewModel by viewModels()
    lateinit var binding: FragmentMainBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (this::binding.isInitialized) {
            return binding.root
        } else {
            binding = FragmentMainBinding.inflate(layoutInflater)
            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ItemAdapter(ItemComparator(), viewModel)
        binding.listRecyclerView.adapter = adapter
        binding.listRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.readAllData.observe(viewLifecycleOwner, adapter::submitList)
        binding.addButton.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToAddFragment())
        }
    }


}