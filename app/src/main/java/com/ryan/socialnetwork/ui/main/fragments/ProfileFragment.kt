package com.ryan.socialnetwork.ui.main.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ryan.socialnetwork.R
import com.ryan.socialnetwork.other.EventObserver
import com.ryan.socialnetwork.ui.main.viewmodels.BasePostViewModel
import com.ryan.socialnetwork.ui.main.viewmodels.ProfileViewModel
import com.ryan.socialnetwork.ui.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile.*

@AndroidEntryPoint
open class ProfileFragment: BasePostFragment(R.layout.fragment_profile) {

    override val postProgressBar: ProgressBar
        get() = profilePostsProgressBar
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: ProfileViewModel by viewModels()
            return vm
        }

    protected val viewModel: ProfileViewModel
        get() = basePostViewModel as ProfileViewModel

    protected open val uid: String
        get() = FirebaseAuth.getInstance().uid!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        subscribeToObserve()

        btnToggleFollow.isVisible = false
        viewModel.loadProfile((uid))
    }

    private fun setupRecyclerView() = rvPosts.apply{
        adapter = postAdapter
        itemAnimator = null
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObserve() {
        viewModel.profileMeta.observe(viewLifecycleOwner, EventObserver(
            onError = {
                profileMetaProgressBar.isVisible = false
                snackbar((it))
            },
            onLoading = {
                profileMetaProgressBar.isVisible = true
            }
        ){ user ->
            profileMetaProgressBar.isVisible = false
            tvUsername.text = user.userName
            tvProfileDescription.text = if(user.description.isEmpty()){
                requireContext().getString(R.string.no_description)
            } else user.description
            glide.load(user.profilePictureUrl).into(ivProfileImage)
        })
    }
}