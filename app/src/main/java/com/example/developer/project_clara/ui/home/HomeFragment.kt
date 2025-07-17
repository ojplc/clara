package com.example.developer.project_clara.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.developer.project_clara.R
import com.example.developer.project_clara.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupClickListeners()

        return root
    }

    private fun setupClickListeners() {
        binding.cardVoiceAssistant.setOnClickListener {
            Toast.makeText(context, "Assistente de Voz - Em breve!", Toast.LENGTH_SHORT).show()
        }

        binding.cardConversation.setOnClickListener {
            Toast.makeText(context, "Conversação - Em breve!", Toast.LENGTH_SHORT).show()
        }

        binding.cardStudyMethod.setOnClickListener {
            Toast.makeText(context, "Método de Estudo - Em breve!", Toast.LENGTH_SHORT).show()
        }

        binding.cardChat.setOnClickListener {
            findNavController().navigate(R.id.nav_chat)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}