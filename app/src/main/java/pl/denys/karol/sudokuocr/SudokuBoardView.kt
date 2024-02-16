package pl.denys.karol.sudokuocr

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SudokuBoardView : View {

    private val linePaint = Paint()
    private val selectedCellPaint = Paint()
    private val numberPaint = Paint()
    private var greenColorModeEnabled = false

    private val board: Array<IntArray> = Array(9) { IntArray(9) }
    private var selectedRow = -1
    private var selectedCol = -1
    private var redNumberPositionsString: String = ""
    private var greyNumberPositionsString: String = ""


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        linePaint.strokeWidth = 5f
        linePaint.color = resources.getColor(android.R.color.black)

        selectedCellPaint.color = resources.getColor(android.R.color.holo_blue_light)

        numberPaint.textSize = 48f
        numberPaint.textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawLines(canvas)
        drawNumbers(canvas)
        drawSelectedCell(canvas)
    }

    private fun drawLines(canvas: Canvas) {
        val cellSize = width / 9f

        for (i in 1 until 9) {
            val paint = getLinePaint(i % 3 == 0)
            canvas.drawLine(i * cellSize, 0f, i * cellSize, width.toFloat(), paint)
            canvas.drawLine(0f, i * cellSize, width.toFloat(), i * cellSize, paint)
        }
    }

    private fun drawSelectedCell(canvas: Canvas) {
        if (selectedRow != -1 && selectedCol != -1) {
            val cellSize = width / 9f
            val rect = Rect(
                (selectedCol * cellSize).toInt(),
                (selectedRow * cellSize).toInt(),
                ((selectedCol + 1) * cellSize).toInt(),
                ((selectedRow + 1) * cellSize).toInt()
            )
            val paint = Paint().apply {
                color = resources.getColor(android.R.color.holo_blue_light)
                style = Paint.Style.STROKE
                strokeWidth = 8f
            }
            canvas.drawRect(rect, paint)
        }
    }

    private fun getLinePaint(isThick: Boolean): Paint {
        val paint = Paint()
        paint.strokeWidth = if (isThick) 8f else 5f
        paint.color = resources.getColor(android.R.color.black)
        return paint
    }

    private fun drawNumbers(canvas: Canvas) {
        val cellSize = width / 9f

        val redNumberPositions = parseRedNumberPositions(redNumberPositionsString)
        val greyNumberPositions = parseGreyNumberPositions(greyNumberPositionsString)

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val x = col * cellSize + cellSize / 2
                val y = row * cellSize + cellSize / 2

                val textColor = when {
                    redNumberPositions.contains(Pair(row, col)) && !greyNumberPositions.contains(Pair(row, col)) -> Color.RED
                    greenColorModeEnabled && !greyNumberPositions.contains(Pair(row, col)) -> Color.GREEN  // Zmiana tutaj
                    greyNumberPositions.contains(Pair(row, col)) -> Color.GRAY
                    else -> Color.BLACK
                }

                numberPaint.color = textColor

                if (board[row][col] != 0) {
                    canvas.drawText(
                        board[row][col].toString(),
                        x,
                        y + getTextHeight() / 2,
                        numberPaint
                    )
                }
            }
        }

    }

    private var cellClickCallback: ((row: Int, col: Int) -> Unit)? = null

    fun setOnCellClickListener(callback: (row: Int, col: Int) -> Unit) {
        this.cellClickCallback = callback
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
                val cellSize = width / 9f
                selectedRow = (event.y / cellSize).toInt()
                selectedCol = (event.x / cellSize).toInt()
                if (!isCellEditable(selectedRow, selectedCol)) {
                    Log.d("pp", "tue")
                    selectedRow = -1
                    selectedCol = -1
                    invalidate()
                }
                invalidate()

                cellClickCallback?.invoke(selectedRow, selectedCol)

        }
        return true
    }


    private fun getTextHeight(): Float {
        val bounds = Rect()
        numberPaint.getTextBounds("1", 0, 1, bounds)
        return bounds.height().toFloat()
    }

    fun setBoard(newBoard: Array<IntArray>) {
        for (i in newBoard.indices) {
            System.arraycopy(newBoard[i], 0, board[i], 0, newBoard[i].size)
        }
        invalidate()
    }
    fun clearSelectedCell() {
        if (selectedRow != -1 && selectedCol != -1) {
            board[selectedRow][selectedCol] = 0

            val updatedRedPositions = parseRedNumberPositions(redNumberPositionsString)
                .filterNot { it == Pair(selectedRow, selectedCol) }
                .toMutableList()

            redNumberPositionsString = updatedRedPositions.joinToString("") { "${it.first}${it.second}" }

            invalidate()
        }
    }

    fun setNumberInCell(number: Int) {
        if (selectedRow != -1 && selectedCol != -1) {
            board[selectedRow][selectedCol] = number

            val updatedRedPositions = parseRedNumberPositions(redNumberPositionsString)
                .filterNot { it == Pair(selectedRow, selectedCol) }
                .toMutableList()

            redNumberPositionsString = updatedRedPositions.joinToString("") { "${it.first}${it.second}" }

            invalidate()
        }
    }

    fun getSudokuArray(): Array<IntArray> {
        val sudokuArray = Array(9) { IntArray(9) }

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                sudokuArray[row][col] = board[row][col]
            }
        }

        return sudokuArray
    }

    fun boardToString(): String {
        val stringBuilder = StringBuilder()

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                stringBuilder.append(board[row][col])
            }
        }

        return stringBuilder.toString()
    }

    fun setBoardFromString(sudokuString: String) {
        val boarddd: Array<IntArray> = Array(9) { IntArray(9) }

        if (sudokuString.length == 81) {
            for (i in sudokuString.indices) {
                val row = i / 9
                val col = i % 9
                boarddd[row][col] = sudokuString[i].toString().toInt()
                Log.d("ll",sudokuString[i].toString() )
            }
            setBoard(boarddd)
        }
    }

    fun setRedNumberPositions(redPositionsString: String) {
        this.redNumberPositionsString = redPositionsString
        invalidate()
    }

    private fun parseRedNumberPositions(redPositionsString: String): List<Pair<Int, Int>> {
        val redPositions = mutableListOf<Pair<Int, Int>>()
        for (i in redPositionsString.indices step 2) {
            val row = Character.getNumericValue(redPositionsString[i])
            val col = Character.getNumericValue(redPositionsString[i + 1])
            redPositions.add(Pair(row, col))
        }
        return redPositions
    }

    fun getRedNumberPositionsString(): String {
        val redBoard = redNumberPositionsString
        return redBoard
    }

    fun setGreyNumberPositions(greyPositionsString: String) {
        this.greyNumberPositionsString = greyPositionsString
        invalidate()
    }

    private fun parseGreyNumberPositions(greyPositionsString: String): List<Pair<Int, Int>> {
        val greyPositions = mutableListOf<Pair<Int, Int>>()
        for (i in greyPositionsString.indices step 2) {
            val row = Character.getNumericValue(greyPositionsString[i])
            val col = Character.getNumericValue(greyPositionsString[i + 1])
            greyPositions.add(Pair(row, col))
        }
        return greyPositions
    }

    fun isCellEditable(row: Int, col: Int): Boolean {
        return !parseGreyNumberPositions(greyNumberPositionsString).contains(Pair(row, col))
    }

    fun setGreenColorModeEnabled(enabled: Boolean) {
        greenColorModeEnabled = enabled
        invalidate()
    }

}

