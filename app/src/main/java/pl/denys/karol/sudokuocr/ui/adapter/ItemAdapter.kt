package pl.denys.karol.sudokuocr.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import pl.denys.karol.sudokuocr.DBViewModel
import pl.denys.karol.sudokuocr.SudokuEntity
import pl.denys.karol.sudokuocr.databinding.SudokuItemViewBinding


class ItemAdapter(itemComparator: ItemComparator, private val viewModel: DBViewModel) : ListAdapter<SudokuEntity, ItemViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            SudokuItemViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), viewModel)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}