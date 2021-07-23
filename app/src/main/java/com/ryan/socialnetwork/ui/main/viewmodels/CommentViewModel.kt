package com.ryan.socialnetwork.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryan.data.entities.Comment
import com.ryan.repositories.MainRepository
import com.ryan.socialnetwork.other.Event
import com.ryan.socialnetwork.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel(){

    private val _createCommentStatus = MutableLiveData<Event<Resource<Comment>>>()
    val createCommentStatus: LiveData<Event<Resource<Comment>>> = _createCommentStatus

    private val _deleteCommentStatus = MutableLiveData<Event<Resource<Comment>>>()
    val deleteCommentStatus: LiveData<Event<Resource<Comment>>> = _deleteCommentStatus

    private val _commentsForPosts = MutableLiveData<Event<Resource<List<Comment>>>>()
    val commentsForPosts: LiveData<Event<Resource<List<Comment>>>> = _commentsForPosts

    fun createComment(commentText: String, postId: String) {
        if(commentText.isEmpty()) return
        _createCommentStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.createComment(commentText, postId)
            _createCommentStatus.postValue(Event(result))
        }
    }

    fun deleteComment(comment: Comment) {
        _deleteCommentStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.deleteComment(comment)
            _deleteCommentStatus.postValue(Event(result))
        }
    }

    fun getCommentForPost(postId: String) {
        _commentsForPosts.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getCommentFromPost(postId)
            _commentsForPosts.postValue(Event(result))
        }
    }

}