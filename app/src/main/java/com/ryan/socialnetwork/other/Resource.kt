package com.ryan.socialnetwork.other

//Sealed class is more like interface but no outer class can Extent this class
//Only be extended by children class or the class present inside that file

sealed class Resource<T>(val data: T? = null, val message: String? = null) {

    class Success<T>(data: T): Resource<T>(data)

    class Error<T>(message: String,  data: T? = null): Resource<T>(data, message)

    class Loading<T>(data: T? = null): Resource<T>(data)
}