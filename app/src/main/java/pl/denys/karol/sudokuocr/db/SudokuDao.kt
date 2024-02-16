package pl.denys.karol.sudokuocr.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.denys.karol.sudokuocr.SudokuEntity

@Dao
interface SudokuDao {
    @Query("SELECT * FROM sudokuBoard")
    fun getAllSudokuEntities(): LiveData<List<SudokuEntity>>

    @Query("SELECT * FROM sudokuBoard WHERE id = :id")
    fun getSudokuEntityById(id: Int): LiveData<SudokuEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSudokuEntity(sudokuEntity: SudokuEntity)

    @Delete
    suspend fun deleteSudokuEntity(sudokuEntity: SudokuEntity)

    @Query("UPDATE sudokuBoard SET  board = :board, redBoard = :redBoard, helpNumber = :badNumber, solveTime = :solveTime, lastDateTime = :lastDateTime WHERE id = :id")
    fun updateSudokuFields(id: Int, redBoard: String, badNumber: Int, board: String, solveTime: Long, lastDateTime: String)
}