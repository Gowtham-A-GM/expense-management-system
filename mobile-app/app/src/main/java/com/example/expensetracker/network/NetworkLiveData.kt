package com.example.expensetracker.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData

class NetworkLiveData(private val context: Context) : LiveData<Boolean>() {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            postValue(true)
        }

        override fun onLost(network: Network) {
            postValue(false)
        }
    }

    override fun onActive() {
        super.onActive()

        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)
        val connected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        postValue(connected)

        cm.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onInactive() {
        super.onInactive()
        cm.unregisterNetworkCallback(networkCallback)
    }
}
