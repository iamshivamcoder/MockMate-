package com.example.mockmate.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/**
 * Utility class for network-related operations
 */
object NetworkConfig {
    private const val TAG = "NetworkConfig"
    
    /**
     * Checks if the device currently has internet connectivity
     */
    fun isNetworkAvailable(context: Context): Boolean {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network connectivity", e)
            return false
        }
    }
    
    /**
     * Handles API errors and returns appropriate user-friendly messages
     */
    fun getNetworkErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is java.net.UnknownHostException -> "No internet connection. Please check your network."
            is java.net.SocketTimeoutException -> "Request timed out. Please try again."
            is retrofit2.HttpException -> {
                when (throwable.code()) {
                    429 -> "Too many requests. Please try again later."
                    401, 403 -> "Authentication error. Please check your API key."
                    500, 502, 503, 504 -> "Server error. Please try again later."
                    else -> "Network error: ${throwable.message()}"
                }
            }
            is java.io.IOException -> "Network error. Please check your internet connection."
            else -> "Unexpected error: ${throwable.message}"
        }
    }
}