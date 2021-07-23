package com.ryan.socialnetwork.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryan.adapters.UserAdapter
import com.ryan.socialnetwork.R
import com.ryan.socialnetwork.other.Constants.SEARCH_TIME_DELAY
import com.ryan.socialnetwork.other.EventObserver
import com.ryan.socialnetwork.ui.main.viewmodels.SearchViewModel
import com.ryan.socialnetwork.ui.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: Fragment(R.layout.fragment_search) {
    @Inject
    lateinit var userAdapter: UserAdapter

    private val viewModel: SearchViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        subscribeToObservers()

        var job: Job? = null
        etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = lifecycleScope.launch {
                delay(SEARCH_TIME_DELAY)
                editable?.let{
                    viewModel.searchUser(it.toString())
                }
            }
        }

        userAdapter.setOnUserClickListener { user->
            findNavController()
                .navigate(SearchFragmentDirections.globaActionToOtherProfileFragment(user.uid)
                )
        }
    }

    private fun subscribeToObservers() {
        viewModel.searchResults.observe(viewLifecycleOwner, EventObserver(
            onError = {
                searchProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                searchProgressBar.isVisible = true
            }
        ){ users ->
            searchProgressBar.isVisible = false
            userAdapter.users = users
        })
    }

    private fun setupRecyclerView() = rvSearchResults.apply{
        layoutManager = LinearLayoutManager(requireContext())
        adapter = userAdapter
        itemAnimator = null
    }

}