package com.ryan.socialnetwork.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ryan.socialnetwork.R
import com.ryan.socialnetwork.other.EventObserver
import com.ryan.socialnetwork.ui.auth.AuthViewModel
import com.ryan.socialnetwork.ui.main.MainActivity
import com.ryan.socialnetwork.ui.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.*

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.fragment_login) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        subscribeToObserve()

        login_btn.setOnClickListener {
            viewModel.login(
                login_email_et.text.toString(),
                login_password_et.text.toString()
            )
        }

        register_textview.setOnClickListener {
        if (findNavController().previousBackStackEntry != null ){
            findNavController().popBackStack()
        } else {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                )
            }
        }
    }

    private fun subscribeToObserve() {
        viewModel.loginStatus.observe(viewLifecycleOwner, EventObserver(
            onError =  {
                loginProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading =  {
                loginProgressBar.isVisible = true
            }
        ){
            loginProgressBar.isVisible = false
            Intent(requireContext(), MainActivity::class.java).also {
                startActivity(it)
                requireActivity().finish()
            }
        })
    }
}