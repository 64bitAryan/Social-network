package com.ryan.socialnetwork.ui.auth.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ryan.socialnetwork.R
import com.ryan.socialnetwork.other.EventObserver
import com.ryan.socialnetwork.ui.auth.AuthViewModel
import com.ryan.socialnetwork.ui.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_register.*

@AndroidEntryPoint
class RegisterFragment: Fragment(R.layout.fragment_register) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        subscribeToObservers()

        register_btn.setOnClickListener {
            viewModel.register(
                register_email_et.text.toString(),
                register_username_et.text.toString(),
                register_password_et.text.toString(),
                register_renterpwd_et.text.toString()
            )
        }

        login_textview.setOnClickListener {
            if(findNavController().previousBackStackEntry != null) {
                findNavController().popBackStack()
            } else {
                findNavController().navigate(
                    RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
                )
            }
        }

    }
    private fun subscribeToObservers() {
        viewModel.registerStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                registerProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                registerProgressBar.isVisible = true
            }
        ){
            registerProgressBar.isVisible = false
            snackbar(getString(R.string.success_registration))
        })
    }
}