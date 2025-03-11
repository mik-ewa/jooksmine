package com.example.fitness_tracking_app.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import javax.inject.Inject

class NetworkTracker @Inject constructor(
    context: Context
) {
    private val appContext = context.applicationContext

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val connectedNetworkCapabilities = listOf(
        NetworkCapabilities.NET_CAPABILITY_INTERNET,
        NetworkCapabilities.NET_CAPABILITY_VALIDATED
    )

    fun isConnectedToInternet(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        return connectedNetworkCapabilities.all { capability ->
            networkCapabilities?.hasCapability(capability) == true
        }
    }

    fun registerNetworkCallback(onNetworkStatusChanged: (Boolean) -> Unit) {
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                onNetworkStatusChanged(true)
            }

            override fun onLost(network: android.net.Network) {
                onNetworkStatusChanged(false)
            }
        }

        connectivityManager.registerNetworkCallback(
            networkRequest,
            networkCallback!!
        )
    }

    fun unregisterNetworkCallback() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
            networkCallback = null
        }
    }
}