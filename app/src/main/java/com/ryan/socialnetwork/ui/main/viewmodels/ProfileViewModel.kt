package com.ryan.socialnetwork.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.ryan.data.entities.Post
import com.ryan.data.entities.User
import com.ryan.data.pagingsource.ProfilePostPagingSource
import com.ryan.repositories.MainRepository
import com.ryan.socialnetwork.other.Constants.PAGE_SIZE
import com.ryan.socialnetwork.other.Event
import com.ryan.socialnetwork.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): BasePostViewModel(repository, dispatcher) {

    private val _profileMeta = MutableLiveData<Event<Resource<User>>>()
    val profileMeta: LiveData<Event<Resource<User>>> = _profileMeta

    private val _followStatus = MutableLiveData<Event<Resource<Boolean>>>()
    val followStatus: LiveData<Event<Resource<Boolean>>> = _followStatus

    private val _posts = MutableLiveData<Event<Resource<List<Post>>>>()

    fun getPaginfFlow(uid: String): Flow<PagingData<Post>> {
        val pagingSource = ProfilePostPagingSource(
            FirebaseFirestore.getInstance(),
            uid
        )
        return Pager(PagingConfig(PAGE_SIZE)) {
            pagingSource
        }.flow.cachedIn(viewModelScope)
    }

    fun toggleFollowForUser(uid: String) {
        _followStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.toggleFollowForUser(uid)
            _followStatus.postValue(Event(result))
        }
    }

    fun loadProfile(uid: String) {
        _profileMeta.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getUser(uid)
            _profileMeta.postValue(Event(result))
        }
    }

}