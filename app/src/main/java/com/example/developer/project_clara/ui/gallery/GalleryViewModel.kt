package com.example.developer.project_clara.ui.gallery

import android.app.Application // Ou injetar Context de outra forma
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Para coroutines
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text

    private var llmInference: LlmInference? = null

    companion object { // <--- Adicione um companion object para a TAG
        private const val TAG = "GalleryViewModelDEGUB"
        private const val MODEL_FILENAME = "gemma-3n-E2B-it-int4.task"
    }

    init {
        viewModelScope.launch(Dispatchers.IO) { // Inicialize em uma coroutine para não bloquear a thread principal
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

        // Verificar se o arquivo existe
        if (!modelFile.exists()) {
            throw Exception("Arquivo do modelo não encontrado em: $modelPath")
        }

        Log.i(TAG, "Modelo encontrado em: $modelPath")
        Log.i(TAG, "Tamanho do arquivo: ${modelFile.length()} bytes")

        // Set the configuration options for the LLM Inference task
        val taskOptions = LlmInferenceOptions.builder()
            .setModelPath(modelPath) // CUIDADO: Este caminho é para depuração. Para produção, use assets.
            .setMaxTopK(64)
            .build()

        // Create an instance of the LLM Inference task
        llmInference = LlmInference.createFromOptions(context, taskOptions)
        Log.i(TAG, "LLM Inference criado com sucesso")
    }

    fun generateResponse(prompt: String): String? {
        return try {
            val result = llmInference?.generateResponse(prompt)
            Log.i(TAG, "Resposta gerada: $result")
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