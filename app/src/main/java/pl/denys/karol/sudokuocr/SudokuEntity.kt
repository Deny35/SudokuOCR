package pl.denys.karol.sudokuocr

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sudokuBoard")
data class SudokuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val board: String ,
    val defaultBoard: String,
    val grayBoard: String,
    val redBoard: String,
    val helpNumber: Int,
    val dateTime: String,
    val lastDateTime: String,
    val solveTime: Long,
    val isSolved: Boolean
    )
