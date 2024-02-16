package pl.denys.karol.sudokuocr.ui.adapter

import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import pl.denys.karol.sudokuocr.DBViewModel
import pl.denys.karol.sudokuocr.SudokuBoardOnlyView
import pl.denys.karol.sudokuocr.SudokuEntity
import pl.denys.karol.sudokuocr.databinding.SudokuItemViewBinding
import pl.denys.karol.sudokuocr.ui.MainFragmentDirections


class ItemViewHolder(private val binding: SudokuItemViewBinding, private val viewModel: DBViewModel) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var sudokuBoardView: SudokuBoardOnlyView


    fun bind(item: SudokuEntity) {
        var data: String =""
        for(i in 0..5){
            data += item.dateTime[i]
            if (i==1 || i == 3){
                data += "/"
            }
        }

        var progres:Int = 0
        for (i in item.board){
            if (i.toString() != "0"){
                progres += 1
            }
        }
        binding.data.text = data
        binding.progres.text= "$progres/81"
        sudokuBoardView = binding.sudokuBoardView

        sudokuBoardView.setBoardFromString(item.board)

        binding.itemLayout.setOnClickListener {
            val action: NavDirections = MainFragmentDirections.actionMainFragmentToSudokuFragment(item.id)
            findNavController(binding.root).navigate(action)
        }

        binding.imageView2.setOnClickListener {
            val action: NavDirections = MainFragmentDirections.actionMainFragmentToInfoFragment(item.id)
            findNavController(binding.root).navigate(action)
        }

        binding.imageView.setOnClickListener {
            viewModel.delete(item)
        }
    }
}