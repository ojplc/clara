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

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text

    private var llmInference: LlmInference? = null

    companion object { // <--- Adicione um companion object para a TAG
        private const val TAG = "GalleryViewModelDEGUB"
    }

    init {
        viewModelScope.launch(Dispatchers.IO) { // Inicialize em uma coroutine para não bloquear a thread principal
            try {
                setupLlm(application.applicationContext)
            } catch (e: Exception) {
                // Trate exceções, por exemplo, modelo não encontrado
                _text.postValue("Erro ao inicializar o LLM: ${e.message}")
            }
        }
    }

    private fun setupLlm(context: Context) {
        // Set the configuration options for the LLM Inference task
        val taskOptions = LlmInferenceOptions.builder()
            .setModelPath("/data/local/tmp/llm/gemma-3n-E2B-it-int4.task") // CUIDADO: Este caminho é para depuração. Para produção, use assets.
            .setMaxTopK(64)
            .build()

        // Create an instance of the LLM Inference task
        llmInference = LlmInference.createFromOptions(context, taskOptions)
    }

    fun generateResponse(prompt: String): String? {
        val result = llmInference?.generateResponse(prompt)
        Log.i(TAG, "Result: $result")
        return result

    }

    override fun onCleared() {
        super.onCleared()
        llmInference?.close() // Libere recursos quando o ViewModel for destruído
    }
}