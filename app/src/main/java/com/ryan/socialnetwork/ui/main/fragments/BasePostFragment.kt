package com.ryan.socialnetwork.ui.main.fragments

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.google.firebase.auth.FirebaseAuth
import com.ryan.adapters.PostAdapter
import com.ryan.adapters.UserAdapter
import com.ryan.socialnetwork.R
import com.ryan.socialnetwork.other.EventObserver
import com.ryan.socialnetwork.ui.main.dialogs.DeletePostDialogs
import com.ryan.socialnetwork.ui.main.dialogs.LikedByDialog
import com.ryan.socialnetwork.ui.main.viewmodels.BasePostViewModel
import com.ryan.socialnetwork.ui.snackbar
import javax.inject.Inject

abstract class BasePostFragment(
    layoutId: Int
): Fragment(layoutId) {
    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var postAdapter: PostAdapter

    protected abstract val basePostViewModel: BasePostViewModel

    private var curLikedIndex: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObserver()

        postAdapter.setOnLikeClickListener() { post, i ->
            curLikedIndex = i
            post.isLiked = !post.isLiked
            basePostViewModel.toggleLikedForPost(post)
        }

        postAdapter.setOnDeletePostClickListener { post ->
            DeletePostDialogs().apply {
                setPositiveListener {
                    basePostViewModel.deletePost(post)
                }
            }.show(childFragmentManager, null)
        }

        postAdapter.setOnLikedByClickListener { post->
            basePostViewModel.getUsers(post.likedBy)
        }

        postAdapter.setOnCommentsClickedListener { post ->
            findNavController().navigate(
                R.id.globaActionToCommentDialog,
                Bundle().apply {
                    putString("postId", post.id)
                }
            )
        }
    }

    private fun subscribeToObserver() {

        basePostViewModel.likedPostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                curLikedIndex?.let { index ->
                    postAdapter.peek(index)?.isLiking = false
                    postAdapter.notifyItemChanged(index)
                }
                snackbar(it)
            },
            onLoading = {
                curLikedIndex?.let{ index ->
                    postAdapter.peek(index)?.isLiking = true
                    postAdapter.notifyItemChanged(index)
                }
            }
        ){ isLiked ->
            curLikedIndex?.let{ index->
                val uid = FirebaseAuth.getInstance().uid!!
                postAdapter.peek(index)?.apply {
                    this.isLiked = isLiked
                    isLiking = false
                    if(isLiked) {
                        likedBy += uid
                    } else {
                        likedBy -= uid
                    }
                }
                postAdapter.notifyItemChanged(index)
            }
        })

        basePostViewModel.likedByUsers.observe(viewLifecycleOwner, EventObserver(
            onError = { snackbar(it) }
        ){ users ->
            val userAdapter = UserAdapter(glide)
            LikedByDialog(userAdapter).show(childFragmentManager, null)
        })
    }
}