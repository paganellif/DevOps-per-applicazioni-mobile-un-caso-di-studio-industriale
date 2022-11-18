package it.filo.maggioliebook.android.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import it.filo.maggioliebook.android.R
import it.filo.maggioliebook.android.databinding.FragmentLoginBinding
import it.filo.maggioliebook.repository.user.UserRepository
import kotlinx.coroutines.launch

class LoginFragment: Fragment() {

    private val LOG_TAG: String = "LoginFragment"
    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        val loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val login: Button = binding.login
        val usernameInput: EditText = binding.username
        val passwordInput: EditText = binding.password
        val rememberMeInput: SwitchCompat = binding.switchRememberMe

        login.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                loginViewModel.login(usernameInput.text.toString(),
                    passwordInput.text.toString(), rememberMeInput.isChecked)
                    .observe(viewLifecycleOwner){
                        if(it) {
                            findNavController().navigate(R.id.action_login_to_home)
                            Toast.makeText(requireActivity().applicationContext,
                                R.string.login_successful, Toast.LENGTH_SHORT).show()
                        } else
                            Toast.makeText(requireActivity().applicationContext,
                                R.string.login_failed, Toast.LENGTH_SHORT).show()
                    }
            }
        }

        if (UserRepository().isUserLoggedIn())
            findNavController().navigate(R.id.action_login_to_home)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
