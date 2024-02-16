package pl.denys.karol.sudokuocr.ui

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import pl.denys.karol.sudokuocr.DBViewModel
import pl.denys.karol.sudokuocr.SudokuBoardView
import pl.denys.karol.sudokuocr.SudokuEntity
import pl.denys.karol.sudokuocr.UiState
import pl.denys.karol.sudokuocr.ViewModel
import pl.denys.karol.sudokuocr.databinding.FragmentSudokuBinding
import androidx.lifecycle.Observer
import java.text.SimpleDateFormat
import java.util.Date


@AndroidEntryPoint
class SudokuFragment : Fragment() {
    lateinit var binding: FragmentSudokuBinding
    private val viewModel: ViewModel by viewModels()
    private val dbViewModel: DBViewModel by viewModels()
    private var sudokuString: String = ""
    private var sudokuStringSolved: String = ""
    private var sudokuStringDefault: String = ""
    private var sudokuStringGray: String = ""
    private var sudokuStringRed: String = ""
    private var helpNumberSudoku: Int = 0
    private var solveTime: Long = 0
    private var selectedRow = -1
    private var selectedCol = -1
    private lateinit var sudokuBoardView: SudokuBoardView
    private val itemId: Int by lazy { requireArguments().getInt("id") }
    private var fragmentStartTime: Long = 0
    private var fragmentDuration: Long = 0
    private var isSolved = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (this::binding.isInitialized) {
            return binding.root
        } else {
            binding = FragmentSudokuBinding.inflate(layoutInflater)
            return binding.root
        }
    }


    override fun onResume() {
        super.onResume()
        resetTimer()
        fragmentStartTime = SystemClock.elapsedRealtime()
    }

    override fun onPause() {
        super.onPause()
        if (!isSolved) {
            saveTime()
        }
    }

    private fun saveTime(){
        val dateFormat = SimpleDateFormat("ddMMyyHHmmss")
        val currentDate = Date()
        fragmentDuration += SystemClock.elapsedRealtime() - fragmentStartTime
        solveTime += fragmentDuration
        sudokuString = sudokuBoardView.boardToString()
        Log.e("aa", sudokuString.length.toString())
        dbViewModel.updateSudokuFields(itemId, sudokuStringRed, helpNumberSudoku, sudokuString, solveTime,  dateFormat.format(currentDate) )
        Log.d("SudokuFragment", "Fragment duration: $fragmentDuration milliseconds")
    }

    private fun resetTimer() {
        fragmentStartTime = SystemClock.elapsedRealtime()
        fragmentDuration = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sudokuBoardView = binding.sudokuBoardView

        dbViewModel.getItem(itemId).observe(viewLifecycleOwner, this::setItem)
        setupNumberPad()

        sudokuBoardView.setOnCellClickListener { row, col ->
            selectedRow = row
            selectedCol = col
            Log.e("Cell Clicked", "Row: $row, Col: $col")
        }

        binding.checkAll.setOnClickListener {
            sudokuString = sudokuBoardView.boardToString()
            sudokuStringRed = sudokuBoardView.getRedNumberPositionsString()

            viewModel.getRed(-1, -1, sudokuString, sudokuStringRed )
            var checked = 0
            viewModel.redBoard.observe(viewLifecycleOwner, Observer { uiState ->
                when (uiState) {
                    is UiState.Loading -> {
                        Log.i("Solve sudoku", "loding")

                    }
                    is UiState.Success -> {
                        sudokuStringRed = uiState.data
                        sudokuBoardView.setRedNumberPositions(sudokuStringRed)
                        sudokuBoardView.setGreyNumberPositions(sudokuStringGray)
                        checked = 1
                    }
                    is UiState.Failure -> {
                        Toast.makeText(
                            context,
                            uiState.error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
            helpNumberSudoku += checked
        }

        binding.checkOne.setOnClickListener {
            sudokuString = sudokuBoardView.boardToString()
            sudokuStringRed = sudokuBoardView.getRedNumberPositionsString()

            viewModel.getRed(selectedRow, selectedCol, sudokuString, sudokuStringRed )
            var checked = 0
            viewModel.redBoard.observe(viewLifecycleOwner, Observer { uiState ->
                when (uiState) {
                    is UiState.Loading -> {
                        Log.i("Solve sudoku", "loding")

                    }
                    is UiState.Success -> {
                        sudokuStringRed = uiState.data
                        sudokuBoardView.setRedNumberPositions(sudokuStringRed)
                        sudokuBoardView.setGreyNumberPositions(sudokuStringGray)
                        checked = 1
                    }
                    is UiState.Failure -> {
                        Toast.makeText(
                            context,
                            uiState.error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
            helpNumberSudoku += checked

        }

        binding.solveAll.setOnClickListener {
            sudokuString = sudokuBoardView.boardToString()
            val array = Array(9) { row ->
                IntArray(9) { col ->
                    sudokuString[row * 9 + col].toString().toInt()
                }
            }
            viewModel.isValidSudoku(array)

            viewModel.validBord.observe(viewLifecycleOwner, Observer { uiState ->
                when (uiState) {
                    is UiState.Loading -> {
                        Log.e("Solve sudoku", "loding")

                    }
                    is UiState.Success -> {
                        if(uiState.data){
                            viewModel.solveSudoku(sudokuString)

                            viewModel.solveSudoku.observe(viewLifecycleOwner, Observer { uiState ->
                                when (uiState) {
                                    is UiState.Loading -> {
                                        Log.i("Solve sudoku", "loding")

                                    }
                                    is UiState.Success -> {
                                        sudokuStringSolved= uiState.data
                                        sudokuStringRed = ""
                                        sudokuBoardView.setRedNumberPositions(sudokuStringRed)
                                        sudokuBoardView.setBoardFromString(sudokuStringSolved)
                                        sudokuBoardView.setGreyNumberPositions(sudokuStringGray)
                                        checkSolved(sudokuStringSolved)
                                    }
                                    is UiState.Failure -> {
                                        Toast.makeText(
                                            context,
                                            uiState.error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            })
                        }
                    }
                    is UiState.Failure -> {
                        sudokuStringRed = sudokuBoardView.getRedNumberPositionsString()
                        sudokuStringRed = sudokuBoardView.getRedNumberPositionsString()

                        viewModel.getRed(-1, -1, sudokuString, sudokuStringRed )
                        var checked = 0
                        viewModel.redBoard.observe(viewLifecycleOwner, Observer { uiState ->
                            when (uiState) {
                                is UiState.Loading -> {
                                    Log.i("Solve sudoku", "loding")

                                }
                                is UiState.Success -> {
                                    sudokuStringRed = uiState.data
                                    sudokuBoardView.setRedNumberPositions(sudokuStringRed)
                                    sudokuBoardView.setGreyNumberPositions(sudokuStringGray)
                                    checked = 1
                                }
                                is UiState.Failure -> {
                                    Toast.makeText(
                                        context,
                                        uiState.error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                        Toast.makeText(
                            context,
                            uiState.error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        }

        binding.clearAll.setOnClickListener {
            sudokuStringRed = ""
            sudokuBoardView.setBoardFromString(sudokuStringDefault)
            sudokuBoardView.setGreyNumberPositions(sudokuStringGray)
            sudokuBoardView.setRedNumberPositions(sudokuStringRed)
            sudokuBoardView.setGreenColorModeEnabled(false)

        }
    }

    private fun setupNumberPad() {
        val buttonClear = binding.clear
        buttonClear.setOnClickListener { sudokuBoardView.clearSelectedCell() }

        val numberButtons = listOf(
            binding.button1, binding.button2, binding.button3,
            binding.button4, binding.button5, binding.button6,
            binding.button7, binding.button8, binding.button9
        )

        for (button in numberButtons) {
            button.setOnClickListener { onNumberButtonClick(it) }
        }
    }

    fun onNumberButtonClick(view: View) {
        val number = (view as Button).text.toString().toInt()
        sudokuBoardView.setNumberInCell(number)
        sudokuString = sudokuBoardView.boardToString()
        checkSolved(sudokuString)

    }
    private fun checkSolved(sudokuStr: String, save: Boolean = true){
        Log.e("Cell Clicked", sudokuStr)

        if(!sudokuStr.contains("0")){
            isSolved = true
            sudokuBoardView.setGreenColorModeEnabled(isSolved)
            sudokuBoardView.setGreyNumberPositions(sudokuStringGray)

            if(save) {
                saveTime()
            }
        }else{
            isSolved = false
        }
    }
    private fun setItem(item: SudokuEntity){
        sudokuString = item.board
        sudokuStringGray = item.grayBoard
        sudokuStringDefault = item.defaultBoard
        sudokuStringRed = item.redBoard
        helpNumberSudoku = item.helpNumber
        solveTime = item.solveTime

        sudokuBoardView.setBoardFromString(sudokuString)
        sudokuBoardView.setGreyNumberPositions(sudokuStringGray)
        sudokuBoardView.setRedNumberPositions(sudokuStringRed)

        Log.e("Cell", sudokuBoardView.boardToString())

        checkSolved(item.board, false)

    }



}