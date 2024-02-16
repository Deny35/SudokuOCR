package pl.denys.karol.sudokuocr.repository

import android.content.Context
import android.graphics.Bitmap
import pl.denys.karol.sudokuocr.UiState

interface SolveRepository {

    fun detectSudoku(uriString: Bitmap, context: Context, result:   (UiState<Array<IntArray>>) -> Unit)
    fun solveSudoku(board: String, result: (UiState<String>) -> Unit)
    fun getGray(board: Array<IntArray>, result: (UiState<String>) -> Unit)
    fun getRed( selectedRow:Int, selectedCol:Int, board: String, reddBord: String, result: (UiState<String>) -> Unit)
    fun isValidSudoku(board: Array<IntArray>, result: (UiState<Boolean>) -> Unit)
}