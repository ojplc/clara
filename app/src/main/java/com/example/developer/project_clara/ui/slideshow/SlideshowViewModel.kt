package com.example.developer.project_clara.ui.slideshow

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SlideshowViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is slideshow Fragment"
    }
    val text: LiveData<String> = _text

    private var llmInference: LlmInference? = null

    companion object { // <--- Adicione um companion object para a TAG
        private const val TAG = "GalleryViewModelDEBUG"
        private const val MODEL_FILENAME = "gemma-3n-E2B-it-int4.task"
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setupLlm(application.applicationContext)
                _text.postValue("LLM inicializado com sucesso!")
            } catch (e: Exception) {
                // Trate exceções, por exemplo, modelo não encontrado
                _text.postValue("Deu erro ao inicializar o LLM BB: ${e.message}")
            }
        }
    }

    private fun setupLlm(context: Context) {
        val externalDir = File(context.getExternalFilesDir(null), "models")

        if (!externalDir.exists()) {
            externalDir.mkdirs()
        }

        val modelFile = File(externalDir, MODEL_FILENAME)
        val modelPath = modelFile.absolutePath

        val taskOptions = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTopK(64)
            .build()

        llmInference = LlmInference.createFromOptions(context, taskOptions)
    }

    fun generateResponse(prompt: String): String? {
        return try {
            val result = llmInference?.generateResponse(prompt)
            _text.postValue("AI Respondeu com: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar resposta", e)
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        llmInference?.close() // Libere recursos quando o ViewModel for destruído
    }


}