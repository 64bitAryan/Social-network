package com.ryan.socialnetwork.other

import androidx.lifecycle.Observer

class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set
    // Call if you want to consume the event
    fun getContentIfNotHandled(): T? {
        return if (!hasBeenHandled) {
            hasBeenHandled = true
            return content
        } else null
    }

    fun peekContent() = content
}

class EventObserver<T>(
    private inline val onError: ((String) -> Unit)? = null,
    private inline val onLoading: (() -> Unit)? = null,
    private inline val onSuccess: (T) -> Unit
): Observer<Event<Resource<T>>> {
    override fun onChanged(t: Event<Resource<T>>?) {
        when(val content = t?.peekContent()) {
            is Resource.Success -> {
                content.data?.let(onSuccess)
            }
            is Resource.Error -> {
                t.getContentIfNotHandled()?.let {
                    onError?.let { error ->
                         error(it.message!!)
                    }
                }
            }
            is Resource.Loading -> {
                onLoading?.let { loading ->
                    loading()
                }
            }
        }
    }
}