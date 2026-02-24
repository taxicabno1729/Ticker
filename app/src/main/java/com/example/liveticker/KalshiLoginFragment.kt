package com.example.liveticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.liveticker.data.AuthRepository
import com.example.liveticker.databinding.FragmentKalshiLoginBinding
import com.example.liveticker.ui.AuthState
import com.example.liveticker.ui.AuthViewModel
import com.example.liveticker.ui.AuthViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KalshiLoginFragment : Fragment() {

    private var _binding: FragmentKalshiLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKalshiLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authRepository = AuthRepository(requireContext())
        val factory = AuthViewModelFactory(requireActivity().application, authRepository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // Check if already logged in
        if (viewModel.isKalshiLoggedIn()) {
            showLoggedInState()
        } else {
            showLoginState()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text?.toString() ?: ""
            val password = binding.passwordInput.text?.toString() ?: ""
            
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.loginKalshi(email, password)
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logoutKalshi()
        }

        binding.continueButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.kalshiAuthState.collectLatest { state ->
                when (state) {
                    is AuthState.Loading -> {
                        binding.loginButton.isEnabled = false
                        binding.loginButton.text = "Logging in..."
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "Login"
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        showLoggedInState()
                    }
                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "Login"
                        binding.errorText.text = state.message
                        binding.errorText.visibility = View.VISIBLE
                    }
                    is AuthState.LoggedOut -> {
                        showLoginState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun showLoginState() {
        binding.loginForm.visibility = View.VISIBLE
        binding.loggedInContainer.visibility = View.GONE
        binding.errorText.visibility = View.GONE
    }

    private fun showLoggedInState() {
        binding.loginForm.visibility = View.GONE
        binding.loggedInContainer.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        binding.loggedInEmail.text = "Logged in as: ${viewModel.getKalshiEmail()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
