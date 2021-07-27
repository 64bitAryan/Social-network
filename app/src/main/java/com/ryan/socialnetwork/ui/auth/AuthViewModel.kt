package com.ryan.socialnetwork.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.ryan.repositories.AuthRepository
import com.ryan.socialnetwork.R
import com.ryan.socialnetwork.other.Constants.MAX_USERNAME_LENGTH
import com.ryan.socialnetwork.other.Constants.MIN_PASSWORD_LENGTH
import com.ryan.socialnetwork.other.Constants.MIN_USERNAME_LENGTH
import com.ryan.socialnetwork.other.Event
import com.ryan.socialnetwork.other.Resource
import dagger.Provides
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val applicationContext: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {

    private val _registerStatus = MutableLiveData<Event<Resource<AuthResult>>>()
    val registerStatus: LiveData<Event<Resource<AuthResult>>> = _registerStatus

    private val _loginStatus = MutableLiveData<Event<Resource<AuthResult>>>()
    val loginStatus: LiveData<Event<Resource<AuthResult>>> = _loginStatus

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            val error = applicationContext.getString(R.string.error_input_empty)
            _loginStatus.postValue(Event(Resource.Error(error)))
        } else {
            _loginStatus.postValue(Event(Resource.Loading()))
            viewModelScope.launch (dispatcher){
                val result = repository.login(email, password)
                _loginStatus.postValue(Event(result))
            }
        }
    }

    fun register(email: String, username: String, password: String, repeatedPassword: String) {
        val error = if(email.isEmpty() || username.isEmpty() || password.isEmpty()){
            applicationContext.getString(R.string.error_input_empty)
        } else if (password != repeatedPassword) {
            applicationContext.getString(R.string.error_incorrect_repeated_password)
        } else if(username.length < MIN_USERNAME_LENGTH) {
            applicationContext.getString(R.string.error_username_too_short, MIN_USERNAME_LENGTH)
        } else if(username.length > MAX_USERNAME_LENGTH) {
            applicationContext.getString(R.string.error_username_too_long, MAX_USERNAME_LENGTH)
        } else if(password.length < MIN_PASSWORD_LENGTH) {
            applicationContext.getString(R.string.error_password_too_short, MIN_PASSWORD_LENGTH)
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            applicationContext.getString(R.string.error_not_a_valid_email)
        } else null

        //PostValue to liveData about the Error
        //error variable is not equal to null
        error?.let {
            _registerStatus.postValue(Event(Resource.Error(it)))
            return
        }

        //No Error Occurred and everything went well
        _registerStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.register(email, username, password)
            _registerStatus.postValue(Event(result))
        }
    }
}