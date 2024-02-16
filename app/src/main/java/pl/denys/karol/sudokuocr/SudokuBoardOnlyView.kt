package pl.denys.karol.sudokuocr


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SudokuBoardOnlyView : View {

    private val linePaint = Paint()
    private val selectedCellPaint = Paint()
    private val numberPaint = Paint()

    private val board: Array<IntArray> = Array(9) { IntArray(9) }
    private var selectedRow = -1
    private var selectedCol = -1
    private var greyNumberPositionsString: String = ""
    private var redNumberPositionsString: String = ""



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
            Log.d("ii", i.toString())
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
                    redNumberPositions.contains(Pair(row, col)) -> Color.RED
                    else -> {
                        if (!greyNumberPositions.contains(Pair(row, col))) {
                            Color.BLACK
                        } else {
                            Color.GRAY
                        }
                    }
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


    private fun parseRedNumberPositions(redPositionsString: String): List<Pair<Int, Int>> {
        val redPositions = mutableListOf<Pair<Int, Int>>()
        for (i in redPositionsString.indices step 2) {
            val row = Character.getNumericValue(redPositionsString[i])
            val col = Character.getNumericValue(redPositionsString[i + 1])
            redPositions.add(Pair(row, col))
        }
        return redPositions
    }
}

