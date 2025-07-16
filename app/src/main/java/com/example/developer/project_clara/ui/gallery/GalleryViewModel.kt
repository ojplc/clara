package com.example.developer.project_clara.ui.gallery

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "Inicializando LLM..."
    }
    val text: LiveData<String> = _text

    private var llmInference: LlmInference? = null
    private var isInitialized = false

    companion object {
        private const val TAG = "GalleryViewModelDEBUG"
        private const val MODEL_FILENAME = "gemma-3n-E2B-it-int4.task"
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setupLlm(application.applicationContext)
                isInitialized = true
                withContext(Dispatchers.Main) {
                    _text.value = "LLM inicializado com sucesso!"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _text.value = "Erro ao inicializar o LLM: ${e.message}"
                }
            }
        }
    }

    private suspend fun setupLlm(context: Context) = withContext(Dispatchers.IO) {
        val externalDir = File(context.getExternalFilesDir(null), "models")
        if (!externalDir.exists()) {
            externalDir.mkdirs()
        }

        val modelFile = File(externalDir, MODEL_FILENAME)
        val modelPath = modelFile.absolutePath

        if (!modelFile.exists()) {
            throw Exception("Arquivo do modelo não encontrado em: $modelPath")
        }

        Log.i(TAG, "Modelo encontrado em: $modelPath")
        Log.i(TAG, "Tamanho do arquivo: ${modelFile.length()} bytes")

        val taskOptions = LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTopK(64)
            .build()

        llmInference = LlmInference.createFromOptions(context, taskOptions)
        Log.i(TAG, "LLM Inference criado com sucesso")
    }

    suspend fun generateResponse(prompt: String): String? = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            withContext(Dispatchers.Main) {
                _text.value = "LLM ainda não foi inicializado"
            }
            return@withContext null
        }

        return@withContext try {
            val result = llmInference?.generateResponse(prompt)
            Log.i(TAG, "Resposta gerada: $result")
            withContext(Dispatchers.Main) {
                _text.value = "AI Respondeu com: $result"
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar resposta", e)
            withContext(Dispatchers.Main) {
                _text.value = "Erro ao gerar resposta: ${e.message}"
            }
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        llmInference?.close()
    }
}