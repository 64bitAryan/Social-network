package com.ryan.socialnetwork.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryan.data.entities.Post
import com.ryan.data.entities.User
import com.ryan.repositories.MainRepository
import com.ryan.socialnetwork.other.Event
import com.ryan.socialnetwork.other.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BasePostViewModel (
    private val repository: MainRepository,
    private val dispatchers: CoroutineDispatcher = Dispatchers.Main
    ):ViewModel(){

    private val _likedPostStatus = MutableLiveData<Event<Resource<Boolean>>>()
    val likedPostStatus: LiveData<Event<Resource<Boolean>>> = _likedPostStatus

    private val _deletePostStatus = MutableLiveData<Event<Resource<Post>>>()
    val deletePostStatus: LiveData<Event<Resource<Post>>> = _deletePostStatus

    private val _likedByUsers = MutableLiveData<Event<Resource<List<User>>>>()
    val likedByUsers: LiveData<Event<Resource<List<User>>>> = _likedByUsers

    abstract val posts: LiveData<Event<Resource<List<Post>>>>

    abstract fun getPosts(uid: String = "")

    fun getUsers(uids: List<String>) {
        if (uids.isEmpty()) return
        _likedByUsers.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getUsers(uids)
            _likedByUsers.postValue(Event(result))
        }
    }

    fun toggleLikedForPost(post: Post) {
        _likedPostStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.toggleLikeForPosts(post)
            _likedPostStatus.postValue(Event(result))
        }
    }

    fun deletePost(post: Post) {
        _deletePostStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.deletePost(post)
            _deletePostStatus.postValue(Event(result))
        }
    }
}