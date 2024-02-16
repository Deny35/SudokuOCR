package pl.denys.karol.sudokuocr.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import pl.denys.karol.sudokuocr.UiState

class SolveRepositoryImplementation: SolveRepository {

    override fun detectSudoku(uriString: Bitmap, context: Context, result:  (UiState<Array<IntArray>>) -> Unit) {
        OpenCVLoader.initDebug()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val inputMat = Mat()
        Utils.bitmapToMat(uriString, inputMat)

        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(inputMat, inputMat, Size(5.0, 5.0), 0.0)
        Imgproc.threshold(inputMat, inputMat, 100.0, 255.0, Imgproc.THRESH_BINARY)
        Imgproc.Canny(inputMat, inputMat, 0.0, 150.0)

        val elementSize = 5
        val element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(elementSize.toDouble(), elementSize.toDouble()))
        Imgproc.dilate(inputMat, inputMat, element)

        val contours = ArrayList<MatOfPoint>()
        Imgproc.findContours(inputMat, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var maxArea = 0.0
        var maxSquare = MatOfPoint2f()

        for (contour in contours) {
            val epsilon = 0.1 * Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*contour.toArray()), approx, epsilon, true)

            val area = Imgproc.contourArea(approx)
            if (area > maxArea && approx.rows() == 4) {
                maxArea = area
                maxSquare = approx
            }
        }

        val targetMat = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(0.0, inputMat.rows().toDouble()),
            Point(inputMat.cols().toDouble(), inputMat.rows().toDouble()),
            Point(inputMat.cols().toDouble(), 0.0)
        )

        val perspectiveMatrix = Imgproc.getPerspectiveTransform(maxSquare, targetMat)

        val outputMat = Mat()
        val outputMatt = Mat()
        Utils.bitmapToMat(uriString, outputMatt)
        Imgproc.warpPerspective(outputMatt, outputMat, perspectiveMatrix, Size(inputMat.cols().toDouble(), inputMat.rows().toDouble()))

        runBlocking {
            val gridSize = 9
            val cellSizeX = (outputMat.cols() / gridSize)
            val cellSizeY = (outputMat.rows() / gridSize)

            val enlargedCellSizeX = (cellSizeX * 1.1).toInt()
            val enlargedCellSizeY = (cellSizeY * 1.1).toInt()

            val sudokuGrid = Array(gridSize) { col ->
                Array(gridSize) { row ->
                    val offsetX = (col * cellSizeX - (enlargedCellSizeX - cellSizeX) / 2).coerceAtLeast(0)
                    val offsetY = (row * cellSizeY - (enlargedCellSizeY - cellSizeY) / 2).coerceAtLeast(0)

                    val width = if (offsetX + enlargedCellSizeX > outputMat.cols()) outputMat.cols() - offsetX else enlargedCellSizeX
                    val height = if (offsetY + enlargedCellSizeY > outputMat.rows()) outputMat.rows() - offsetY else enlargedCellSizeY

                    val roi = Rect(offsetX, offsetY, width, height)
                    val cellMat = Mat(outputMat, roi)

                    val enlargementFactor = 3.0
                    val enlargedCellMat = Mat()
                    Imgproc.resize(cellMat, enlargedCellMat, Size(cellMat.cols() * enlargementFactor, cellMat.rows() * enlargementFactor))
                    val cellBitmap = Bitmap.createBitmap(enlargedCellMat.cols(), enlargedCellMat.rows(), Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(enlargedCellMat, cellBitmap)

                    cellBitmap
                }
            }


        val sudokiBoard: Array<IntArray> =  Array(9) { IntArray(9) { 0 } }
            for (i in 0 until 9) {
                for (j in 0 until 9) {
                    val image = InputImage.fromBitmap(sudokuGrid[i][j], 0)
                    val deferred = async(Dispatchers.Default) {
                        try {
                            val visionText = recognizer.process(image).await()
                            val text = processRecognizedText(visionText).replace(Regex("[^0-9]"), "")
                            if (text.isNotEmpty()) {
                                var numberInt = 0
                                try {
                                    numberInt = text.toInt()
                                    sudokiBoard[j][i] = numberInt
                                } catch (e: NumberFormatException) {
                                    null
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }
                    deferred.await()
                }
            }
            result.invoke(
                UiState.Success(sudokiBoard)
            )
        }

    }

    private fun processRecognizedText(visionText: Text):String {
        for (block in visionText.textBlocks) {
            val elementText = block.text
            Log.d("fir", elementText)
            return elementText
        }
        return "null"
    }


    //         Sudoku Solve
    override fun solveSudoku(board: String, result: (UiState<String>) -> Unit) {
        val boardArray = stringToBoardArray(board)
        if (solve(boardArray)) {
            result(UiState.Success(boardToString(boardArray)))
        } else {
            result(UiState.Failure("Unable to solve Sudoku"))
        }
    }

    private fun stringToBoardArray(board: String): Array<CharArray> {
        val array = Array(9) { CharArray(9) }
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                array[i][j] = board[i * 9 + j]
            }
        }
        return array
    }


    private fun solve(board: Array<CharArray>): Boolean {
        for (row in board.indices) {
            for (col in board[0].indices) {
                if (board[row][col] == '0') {
                    for (num in '1'..'9') {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num
                            if (solve(board)) {
                                return true
                            }
                            board[row][col] = '0'
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun isValid(board: Array<CharArray>, row: Int, col: Int, num: Char): Boolean {
        for (i in 0 until 9) {
            if (board[row][i] == num || board[i][col] == num || board[3 * (row / 3) + i / 3][3 * (col / 3) + i % 3] == num) {
                return false
            }
        }
        return true
    }

    private fun boardToString(board: Array<CharArray>): String {
        return buildString {
            for (row in board) {
                append(row)
            }
        }
    }
    //
    override fun getGray(board: Array<IntArray>, result: (UiState<String>) -> Unit) {
        val grayBord = StringBuilder()
        for (i in 0 until 9){
            for (j in 0 until 9){
                Log.d("gog",board[i][j].toString())

                if (board[i][j] != 0){
                    grayBord.append(i).append(j)
                }
            }
        }
        result.invoke(
            UiState.Success(grayBord.toString())
        )
    }
    fun stringToBoardArrayh(board: String): Array<IntArray> {
        if (board.length != 81) {
            throw IllegalArgumentException("Długość stringu board musi być równa 81")
        }

        return Array(9) { row ->
            IntArray(9) { col ->
                board[row * 9 + col].toString().toInt()
            }
        }
    }
    override fun getRed(
        selectedRow: Int,
        selectedCol: Int,
        board: String,
        reddBodr: String,
        result: (UiState<String>) -> Unit
    ) {

        val boardArray = stringToBoardArrayh(board)


        val redBoard = StringBuilder()
        if (selectedRow == -1 && selectedCol == -1){
            for(i in 0 until 9) {
                for (j in 0 until 9) {
                    if (boardArray[i][j] != 0) {
                        if (!isValidNumber(boardArray, i, j, boardArray[i][j])) {
                            redBoard.append(i).append(j)
                        }
                    }
                }
            }
        }else{
            redBoard.append(reddBodr)
            if(boardArray[selectedRow][selectedCol] != 0)  {
                Log.e("cos", boardArray[selectedRow][selectedCol].toString())
                Log.e("coss",  isValidNumber(boardArray, selectedRow, selectedCol, boardArray[selectedRow][selectedCol]).toString())

                if (!isValidNumber(boardArray, selectedRow, selectedCol, boardArray[selectedRow][selectedCol]) ){
                    redBoard.append(selectedRow).append(selectedCol)

                }

            }
            Log.e("gog","c")

        }
        result.invoke(
            UiState.Success(redBoard.toString())
        )
    }

    private fun isValidNumber(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (i in 0 until 9) {
            if (i != col && board[row][i] == num) {
                return false
            }

            if (i != row && board[i][col] == num) {
                return false
            }

            val boxRow = 3 * (row / 3) + i / 3
            val boxCol = 3 * (col / 3) + i % 3
            if ((boxRow != row || boxCol != col) && board[boxRow][boxCol] == num) {
                return false
            }
        }
        return true
    }

    override fun isValidSudoku(board: Array<IntArray>, result: (UiState<Boolean>) -> Unit) {
        fun isValidUnit(unit: IntArray): Boolean {
            val filteredUnit = unit.filter { it != 0 }
            return filteredUnit.toSet().size == filteredUnit.size
        }

        for (i in 0 until 9) {
            if (!isValidUnit(board[i]) || !isValidUnit(IntArray(9) { j -> board[j][i] })) {
                Log.e("Solve sudoku", "row || col")
                result(UiState.Failure("Invalid Sudoku: Row or Column validation failed."))
                return
            }
        }

        for (i in 0 until 9 step 3) {
            for (j in 0 until 9 step 3) {
                val subgrid = (i until i + 3).flatMap { x -> (j until j + 3).map { y -> board[x][y] } }.toIntArray()
                if (!isValidUnit(subgrid)) {
                    Log.e("Solve sudoku", "3x3")
                    result(UiState.Failure("Invalid Sudoku: Subgrid validation failed."))
                    return
                }
            }
        }

        result(UiState.Success(true))
    }

}


