package pl.denys.karol.sudokuocr.repository

import androidx.lifecycle.LiveData
import pl.denys.karol.sudokuocr.SudokuEntity
import pl.denys.karol.sudokuocr.db.SudokuDao
import javax.inject.Inject

class DBRepositoryImplementation @Inject constructor (private val sudokuDao: SudokuDao){
    val getAllSudokuEntities: LiveData<List<SudokuEntity>> = sudokuDao.getAllSudokuEntities()

    fun getSudokuEntityById(id: Int): LiveData<SudokuEntity> {
        return sudokuDao.getSudokuEntityById(id)
    }

    suspend fun insertSudokuEntity(sudokuEntity: SudokuEntity) {
            sudokuDao.insertSudokuEntity(sudokuEntity)
    }

    suspend fun deleteSudokuEntity(sudokuEntity: SudokuEntity) {
            sudokuDao.deleteSudokuEntity(sudokuEntity)
    }

    fun updateSudokuFields(id: Int, redBoard: String, helpNumber: Int, board: String, solveTime: Long, lastDateTime: String){
        sudokuDao.updateSudokuFields(id, redBoard, helpNumber, board, solveTime, lastDateTime)
    }
}