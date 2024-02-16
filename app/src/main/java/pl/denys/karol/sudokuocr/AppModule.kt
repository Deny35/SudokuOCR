package pl.denys.karol.sudokuocr

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.denys.karol.sudokuocr.db.SudokuDao
import pl.denys.karol.sudokuocr.db.SudokuDatabase
import pl.denys.karol.sudokuocr.repository.SolveRepository
import pl.denys.karol.sudokuocr.repository.SolveRepositoryImplementation
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideDetectRepository(): SolveRepository {
        return SolveRepositoryImplementation()
    }

    @Singleton
    @Provides
    fun getAppDB(app: Application): SudokuDatabase{
        return SudokuDatabase.getDatabase(app)
    }

    @Singleton
    @Provides
    fun getDao(db: SudokuDatabase): SudokuDao{
        return db.SudokuDao()
    }
}
