package com.ryan.socialnetwork.ui.main.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.ryan.socialnetwork.R
import com.ryan.socialnetwork.other.EventObserver
import com.ryan.socialnetwork.ui.main.viewmodels.CreatePostViewModel
import com.ryan.socialnetwork.ui.snackbar
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_create_post.*
import javax.inject.Inject

@AndroidEntryPoint
class CreatePostFragment: Fragment(R.layout.fragment_create_post) {
    @Inject
    lateinit var glide: RequestManager

    private val viewModel: CreatePostViewModel by viewModels()

    private lateinit var cropContent: ActivityResultLauncher<String>

    private val cropActivityResultContract = object: ActivityResultContract<String, Uri?>() {
        override fun createIntent(context: Context, input: String?): Intent {
            return CropImage.activity()
                .setAspectRatio(16, 9)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(requireContext())
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }

    private var curImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropContent = registerForActivityResult(cropActivityResultContract) {
            it?.let {
                viewModel.setCurrentImageUri(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObserve()
        btnSetPostImage.setOnClickListener {
            cropContent.launch("image/*")
        }
        profileImageView.setOnClickListener {
            cropContent.launch("image/*")
        }

        btnPost.setOnClickListener {
            curImageUri?.let{ uri ->
                viewModel.createPost(uri, postEditText.text.toString())
            } ?: snackbar(getString(R.string.error_no_image_chosen))
        }
    }

    private fun subscribeToObserve() {

        viewModel.curImageUri.observe(viewLifecycleOwner) {
            curImageUri = it
            btnSetPostImage.isVisible = false
            glide.load(curImageUri).into(profileImageView)
        }

        viewModel.createPostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
               createpostProgressBar.isVisible = false
               snackbar(it)
            },
            onLoading = {
                createpostProgressBar.isVisible = true
            }
        ){
            createpostProgressBar.isVisible = false
            findNavController().popBackStack()
        })
    }
}