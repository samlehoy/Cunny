package com.eleonorez.cunny.ui.practice

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.eleonorez.cunny.ml.FruitClassifier
import com.eleonorez.cunny.ml.ExplainabilityEngine
import com.eleonorez.cunny.ml.PredictionResult
import com.eleonorez.cunny.ml.LabelConfidence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed class PracticeUiState {
    data object Idle : PracticeUiState()
    data object Loading : PracticeUiState()
    data class Success(val result: PredictionResult, val imageUri: Uri) : PracticeUiState()
    data class Error(val message: String) : PracticeUiState()
}

class PracticeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<PracticeUiState>(PracticeUiState.Idle)
    val uiState: LiveData<PracticeUiState> = _uiState

    private val _selectedImageUri = MutableLiveData<Uri?>(null)
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    fun setImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun uploadImage(file: File, lessonSlug: String) {
        _uiState.value = PracticeUiState.Loading
        viewModelScope.launch {
            try {
                if (lessonSlug == "confidence-and-uncertainty") {
                    val result = PredictionResult(
                        predictedLabel = "Jeruk",
                        confidence = 0.38f,
                        rationale = "AI tidak terlalu yakin — coba foto lebih jelas atau sudut lain.",
                        topK = listOf(
                            LabelConfidence("Jeruk", 0.38f, "orange"),
                            LabelConfidence("Apel", 0.12f, "apple"),
                            LabelConfidence("Pisang", 0.05f, "banana")
                        )
                    )
                    val uri = _selectedImageUri.value
                    if (uri != null) {
                        _uiState.value = PracticeUiState.Success(result, uri)
                    } else {
                        _uiState.value = PracticeUiState.Error("No image selected")
                    }
                    return@launch
                }
                val bitmap = withContext(Dispatchers.IO) {
                    android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                }
                if (bitmap == null) {
                    _uiState.value = PracticeUiState.Error("Failed to decode image file")
                    return@launch
                }

                val classifier = FruitClassifier(getApplication())
                val rawPredictions = classifier.classify(bitmap)
                classifier.close()

                val engine = ExplainabilityEngine(getApplication())
                val result = engine.compile(rawPredictions)

                val uri = _selectedImageUri.value
                if (uri != null) {
                    _uiState.value = PracticeUiState.Success(result, uri)
                } else {
                    _uiState.value = PracticeUiState.Error("No image selected")
                }
            } catch (e: Exception) {
                _uiState.value = PracticeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = PracticeUiState.Idle
    }
}


