package pl.denys.karol.sudokuocr

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.denys.karol.sudokuocr.repository.DBRepositoryImplementation
import javax.inject.Inject


@HiltViewModel
class DBViewModel @Inject constructor(private val repository: DBRepositoryImplementation): ViewModel(){
    val readAllData: LiveData<List<SudokuEntity>> = repository.getAllSudokuEntities

    fun getItem(id: Int): LiveData<SudokuEntity>{
        return repository.getSudokuEntityById(id)
    }

    fun insert(sudokuEntity: SudokuEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertSudokuEntity(sudokuEntity)
        }
    }

    fun delete (sudokuEntity: SudokuEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteSudokuEntity(sudokuEntity)
        }
    }

    fun updateSudokuFields(id: Int, redBoard: String, badNumber: Int, board: String, solveTime: Long, lastDateTime: String){
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateSudokuFields(id, redBoard, badNumber, board, solveTime, lastDateTime)
        }
    }
}