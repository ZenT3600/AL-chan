package it.matteoleggio.alchan.data.network

import it.matteoleggio.alchan.helper.enums.ResponseStatus

sealed class Resource<T>(
    val responseStatus: ResponseStatus,
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(ResponseStatus.SUCCESS, data)
    class Loading<T>(data: T? = null) : Resource<T>(ResponseStatus.LOADING, data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(ResponseStatus.ERROR, data, message)

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        val resource = other as Resource<*>
        return (responseStatus == resource.responseStatus && data == resource.data && message == resource.message)
    }
}

