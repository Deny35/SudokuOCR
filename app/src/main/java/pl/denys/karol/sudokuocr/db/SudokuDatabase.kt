package pl.denys.karol.sudokuocr.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.denys.karol.sudokuocr.SudokuEntity

@Database(entities = [SudokuEntity::class], version = 1)
abstract class SudokuDatabase : RoomDatabase() {

    abstract fun SudokuDao(): SudokuDao

    companion object{
        @Volatile private var INSTANCE: SudokuDatabase? = null

        fun getDatabase(context: Context): SudokuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SudokuDatabase::class.java,
                    "sudoku_db"
                ).build().also { INSTANCE = it }
                instance
            }
        }
    }

}