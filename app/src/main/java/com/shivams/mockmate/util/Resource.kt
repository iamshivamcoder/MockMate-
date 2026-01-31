package com.shivams.mockmate.util

/**
 * A generic sealed class for representing the state of async operations.
 * Used throughout the app for Repository → ViewModel → UI state management.
 *
 * @param T The type of data on success
 */
sealed class Resource<out T> {
    
    /**
     * Represents a loading state while an operation is in progress.
     */
    data object Loading : Resource<Nothing>()
    
    /**
     * Represents a successful operation with data.
     *
     * @param data The result data
     */
    data class Success<T>(val data: T) : Resource<T>()
    
    /**
     * Represents a failed operation with an error message.
     *
     * @param message Human-readable error message
     * @param exception Optional underlying exception for logging
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : Resource<Nothing>()
    
    /**
     * Returns true if this is a Loading state.
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Returns true if this is a Success state.
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Returns true if this is an Error state.
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Returns the data if Success, null otherwise.
     */
    fun getOrNull(): T? = (this as? Success)?.data
    
    /**
     * Returns the error message if Error, null otherwise.
     */
    fun errorOrNull(): String? = (this as? Error)?.message
    
    /**
     * Maps the data if Success, returns same state otherwise.
     */
    inline fun <R> map(transform: (T) -> R): Resource<R> {
        return when (this) {
            is Loading -> Loading
            is Success -> Success(transform(data))
            is Error -> Error(message, exception)
        }
    }
    
    /**
     * Executes action if Success.
     */
    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Executes action if Error.
     */
    inline fun onError(action: (String, Throwable?) -> Unit): Resource<T> {
        if (this is Error) action(message, exception)
        return this
    }
    
    /**
     * Executes action if Loading.
     */
    inline fun onLoading(action: () -> Unit): Resource<T> {
        if (this is Loading) action()
        return this
    }
}
