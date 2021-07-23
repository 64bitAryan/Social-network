package com.ryan.socialnetwork.ui.main.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ryan.adapters.UserAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ryan.socialnetwork.R

class LikedByDialog(
    private val userAdapter: UserAdapter
): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val rvLikedBy = RecyclerView(requireContext()).apply{
            adapter = userAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.liked_by_dialog_title)
            .setView(rvLikedBy)
            .create()
    }
}