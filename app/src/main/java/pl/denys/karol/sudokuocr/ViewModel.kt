package pl.denys.karol.sudokuocr

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.denys.karol.sudokuocr.repository.SolveRepository
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(val solveRepository: SolveRepository): ViewModel() {
    private val _detected = MutableLiveData<UiState<Array<IntArray>>>()
    val detected: LiveData<UiState<Array<IntArray>>>
        get() = _detected

    private val _solveSudoku = MutableLiveData<UiState<String>>()
    val solveSudoku: LiveData<UiState<String>>
        get() = _solveSudoku

    private val _grayBoard = MutableLiveData<UiState<String>>()
    val grayBoard: LiveData<UiState<String>>
        get() = _grayBoard

    private val _redBoard = MutableLiveData<UiState<String>>()
    val redBoard: LiveData<UiState<String>>
        get() = _redBoard

    private val _validBord = MutableLiveData<UiState<Boolean>>()
    val validBord: LiveData<UiState<Boolean>>
        get() = _validBord

    fun detectSudoku(
        uriString: Bitmap,
        context: Context
    ) {
        _detected.value = UiState.Loading
        solveRepository.detectSudoku(
          uriString,
            context
        ) { _detected.value = it }
    }

    fun solveSudoku(
        bord: String
    ) {
        _solveSudoku.value = UiState.Loading
        solveRepository.solveSudoku(
            bord
        ) { _solveSudoku.value = it }
    }

    fun getGray(
        bord: Array<IntArray>
    ) {
        _grayBoard.value = UiState.Loading
        solveRepository.getGray(
            bord
        ) { _grayBoard.value = it }
    }

    fun getRed(selectedRow:Int, selectedCol:Int, bord: String, reddBord: String
    ) {
        _redBoard.value = UiState.Loading
        solveRepository.getRed(
            selectedRow,
            selectedCol,
            bord,
            reddBord
        ) { _redBoard.value = it }
    }

    fun isValidSudoku(
        bord: Array<IntArray>
    ) {
        _validBord.value = UiState.Loading
        solveRepository.isValidSudoku(
            bord
        ) { _validBord.value = it }
    }

}