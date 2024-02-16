package pl.denys.karol.sudokuocr.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import pl.denys.karol.sudokuocr.DBViewModel
import pl.denys.karol.sudokuocr.SudokuBoardOnlyView
import pl.denys.karol.sudokuocr.SudokuEntity
import pl.denys.karol.sudokuocr.databinding.FragmentInfoBinding
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class InfoFragment : Fragment() {
    lateinit var binding: FragmentInfoBinding
    private lateinit var sudokuBoardView: SudokuBoardOnlyView
    private val viewModel: DBViewModel by viewModels()
    private val itemId: Int by lazy { requireArguments().getInt("id") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (this::binding.isInitialized) {
            return binding.root
        } else {
            binding = FragmentInfoBinding.inflate(layoutInflater)
            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ll", itemId.toString())

        viewModel.getItem(itemId).observe(viewLifecycleOwner, this::displayItem)
    }

    private fun displayItem(item: SudokuEntity){
        Log.d("ll", item.id.toString())
        var data: String =""
        for(i in 0..5){
            data += item.lastDateTime[i]
            if (i==1 || i == 3){
                data += "/"
            }
        }

        binding.textView7.text = data
        binding.textView9.text = item.helpNumber.toString()
        binding.textView11.text = formatDuration(item.solveTime)
        sudokuBoardView = binding.sudokuBoardView

        sudokuBoardView.setBoardFromString(item.board)
        sudokuBoardView.setGreyNumberPositions(item.grayBoard)

    }

    private fun formatDuration(duration: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


}