package com.ryan.socialnetwork.ui.main.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.RequestManager
import com.google.firebase.auth.FirebaseAuth
import com.ryan.data.entities.ProfileUpdate
import com.ryan.socialnetwork.R
import com.ryan.socialnetwork.other.EventObserver
import com.ryan.socialnetwork.ui.main.viewmodels.SettingsViewModel
import com.ryan.socialnetwork.ui.snackbar
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var glide: RequestManager
    private val viewModel: SettingsViewModel by viewModels()
    private var curImageUri: Uri? = null
    private lateinit var cropContent: ActivityResultLauncher<Any?>

    private val cropActivityResultContract = object: ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(requireContext())
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropContent = registerForActivityResult(cropActivityResultContract) { uri ->
            uri?.let {
                viewModel.setCurImageUri(it)
                btnUpdateProfile.isEnabled = true
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObserver()
        val uid = FirebaseAuth.getInstance().uid!!
        viewModel.getUser(uid)
        btnUpdateProfile.isEnabled = false
        etUsername.addTextChangedListener {
            btnUpdateProfile.isEnabled = true
        }
        etDescription.addTextChangedListener {
            btnUpdateProfile.isEnabled = true
        }

        ivProfileImage.setOnClickListener {
            cropContent.launch(null)
        }

        btnUpdateProfile.setOnClickListener {
            val username = etUsername.text.toString()
            val description = etDescription.text.toString()
            val profileUpdate = ProfileUpdate(uid, username, description, curImageUri)
            viewModel.updateProfile(profileUpdate)
        }
    }

    private fun subscribeToObserver() {
        viewModel.getUserStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                settingsProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = { settingsProgressBar.isVisible = true }
        ){ user ->
            settingsProgressBar.isVisible = false
            glide.load(user.profilePictureUrl).into(ivProfileImage)
            etUsername.setText(user.userName)
            etDescription.setText(user.description)
            btnUpdateProfile.isEnabled = false
        })
        viewModel.curImageUri.observe(viewLifecycleOwner) { uri->
            curImageUri = uri
            glide.load(uri).into(ivProfileImage)
        }
        viewModel.updateProfileStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                settingsProgressBar.isVisible = false
                snackbar(it)
                btnUpdateProfile.isEnabled = true
            },
            onLoading = {
                settingsProgressBar.isVisible = false
                btnUpdateProfile.isEnabled = false
            }
        ){
            settingsProgressBar.isVisible = false
            btnUpdateProfile.isEnabled = false
            snackbar(requireContext().getString(R.string.profile_update))
        })
    }
}