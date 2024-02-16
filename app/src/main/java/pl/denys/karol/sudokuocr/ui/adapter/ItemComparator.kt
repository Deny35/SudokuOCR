package pl.denys.karol.sudokuocr.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import pl.denys.karol.sudokuocr.SudokuEntity

class ItemComparator : DiffUtil.ItemCallback<SudokuEntity>() {
    override fun areItemsTheSame(oldItem: SudokuEntity, newItem: SudokuEntity): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: SudokuEntity, newItem: SudokuEntity): Boolean {
        return oldItem.id == newItem.id
    }
}