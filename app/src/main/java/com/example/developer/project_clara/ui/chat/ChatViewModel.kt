package com.example.developer.project_clara.ui.chat

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

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _messages = MutableLiveData<List<ChatMessage>>().apply {
        value = listOf(
            ChatMessage(
                message = "Olá! Sou a Clara, sua assistente pessoal. Como posso ajudá-lo hoje?",
                isUser = false
            )
        )
    }
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLlmReady = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isLlmReady: LiveData<Boolean> = _isLlmReady

    private var llmInference: LlmInference? = null

    companion object {
        private const val TAG = "ChatViewModelDEBUG"
        private const val MODEL_FILENAME = "gemma-3n-E2B-it-int4.task"
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setupLlm(application.applicationContext)
                withContext(Dispatchers.Main) {
                    _isLlmReady.value = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inicializar o LLM", e)
                withContext(Dispatchers.Main) {
                    addMessage("Desculpe, ocorreu um erro ao inicializar a IA. Verifique se o modelo está disponível.", false)
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

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // Adiciona mensagem do usuário
        addMessage(userMessage, true)

        // Verifica se LLM está pronto
        if (_isLlmReady.value != true) {
            addMessage("A IA ainda está sendo inicializada. Tente novamente em alguns instantes.", false)
            return
        }

        // Gera resposta da IA
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _isLoading.value = true
            }

            try {
                val response = llmInference?.generateResponse(userMessage)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    if (!response.isNullOrBlank()) {
                        addMessage(response, false)
                    } else {
                        addMessage("Desculpe, não consegui gerar uma resposta. Tente reformular sua pergunta.", false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao gerar resposta", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    addMessage("Ocorreu um erro ao processar sua mensagem. Tente novamente.", false)
                }
            }
        }
    }

    private fun addMessage(message: String, isUser: Boolean) {
        val currentMessages = _messages.value ?: emptyList()
        val newMessage = ChatMessage(message, isUser)
        _messages.value = currentMessages + newMessage
    }

    override fun onCleared() {
        super.onCleared()
        llmInference?.close()
    }
}