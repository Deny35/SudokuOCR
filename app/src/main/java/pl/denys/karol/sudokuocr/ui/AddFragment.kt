package pl.denys.karol.sudokuocr.ui

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import pl.denys.karol.sudokuocr.DBViewModel
import pl.denys.karol.sudokuocr.SudokuBoardView
import pl.denys.karol.sudokuocr.SudokuEntity
import pl.denys.karol.sudokuocr.UiState
import pl.denys.karol.sudokuocr.ViewModel
import pl.denys.karol.sudokuocr.databinding.FragmentAddBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.io.InputStream

@AndroidEntryPoint
class AddFragment : Fragment() {

    private val viewModel: ViewModel by viewModels()
    private val dbViewModel: DBViewModel by viewModels()
    lateinit var binding: FragmentAddBinding
    private lateinit var sudokuBoardView: SudokuBoardView
    private lateinit var uri: Uri
    private var cropUri: String = ""
    private var cliced = false
    private var sudokuString: String = ""
    private var sudokuStringRed: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (this::binding.isInitialized) {
            return binding.root
        } else {
            binding = FragmentAddBinding.inflate(layoutInflater)
            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sudokuBoardView = binding.sudokuBoardView
        setupNumberPad()

        sudokuBoardView.setOnCellClickListener { row, col ->
            Log.d("Cell Clicked", "Row: $row, Col: $col")
        }

        binding.save.setOnClickListener {
            val sudokuString = sudokuBoardView.boardToString()

            val dateFormat = SimpleDateFormat("ddMMyyHHmmss")
            val currentDate = Date()

            val sudokuArray = sudokuBoardView.getSudokuArray()

            viewModel.isValidSudoku(sudokuArray)
            var isValid: Boolean = false
            viewModel.validBord.observe(viewLifecycleOwner, Observer { uiState ->
                when (uiState) {
                    is UiState.Loading -> {
                        Log.i("Solve sudoku", "loding")
                    }

                    is UiState.Success -> {
                        isValid = uiState.data
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

            if (isValid) {
                viewModel.getGray(sudokuArray)

                var grayBoard: String = ""
                viewModel.grayBoard.observe(viewLifecycleOwner, Observer { uiState ->
                    when (uiState) {
                        is UiState.Loading -> {
                            Log.i("Solve sudoku", "loding")
                        }
                        is UiState.Success -> {
                            grayBoard = uiState.data

                            dbViewModel.insert(
                                SudokuEntity(
                                    0,
                                    sudokuString,
                                    sudokuString,
                                    grayBoard,
                                    "",
                                    0,
                                    dateFormat.format(currentDate),
                                    dateFormat.format(currentDate),
                                    0,
                                    false
                                )
                            )
                            findNavController().popBackStack()
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

        binding.cancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.loadImage.setOnClickListener {
            val launchGalleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(launchGalleryIntent, 0)
        }

        binding.checkAll.setOnClickListener {
            sudokuString = sudokuBoardView.boardToString()
            sudokuStringRed = sudokuBoardView.getRedNumberPositionsString()

            viewModel.getRed(-1, -1, sudokuString, sudokuStringRed)
            var checked = 0
            viewModel.redBoard.observe(viewLifecycleOwner, Observer { uiState ->
                when (uiState) {
                    is UiState.Loading -> {
                        Log.i("Solve sudoku", "loding")

                    }

                    is UiState.Success -> {
                        sudokuStringRed = uiState.data
                        sudokuBoardView.setRedNumberPositions(sudokuStringRed)
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === AppCompatActivity.RESULT_OK && data != null && !cliced) {
            cliced = true
            uri = data.data!!
            crop(uri)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            cliced = false
            val result = CropImage.getActivityResult(data)
            cropUri = result.uri.toString()
            if (resultCode == AppCompatActivity.RESULT_OK) {
                Log.d("uri", cropUri)
                val contentResolver = requireContext().contentResolver
                val bitmap: Bitmap = uriToBitmap(contentResolver, cropUri.toUri())
                viewModel.detectSudoku(bitmap, requireContext())

                viewModel.detected.observe(viewLifecycleOwner, Observer { uiState ->
                    when (uiState) {
                        is UiState.Loading -> {
                        }
                        is UiState.Success -> {
                            val detectedData = uiState.data
                            sudokuBoardView.setBoard(detectedData)
                        }
                        is UiState.Failure -> {
                            Log.e("Picture sudoku", "error")
                        }
                    }
                })
            }
        }
    }

    fun crop(uri: Uri?) {
        CropImage.activity(uri).start(requireContext(), this);
    }

    fun uriToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap {
        var inputStream: InputStream? = null
        inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

}