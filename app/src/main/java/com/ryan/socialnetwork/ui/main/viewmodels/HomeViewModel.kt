package com.ryan.socialnetwork.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.ryan.data.entities.Post
import com.ryan.data.pagingsource.FollowPostPagingSource
import com.ryan.repositories.MainRepository
import com.ryan.socialnetwork.other.Constants.PAGE_SIZE
import com.ryan.socialnetwork.other.Event
import com.ryan.socialnetwork.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): BasePostViewModel(repository, dispatcher) {
     val paginFlow = Pager( PagingConfig(PAGE_SIZE)) {
         FollowPostPagingSource(FirebaseFirestore.getInstance())
     }.flow.cachedIn(viewModelScope)
}