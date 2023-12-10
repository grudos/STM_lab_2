package com.example.lab_2


interface PongConnectionListener {
    fun clientConnecting()
    fun clientConnected()
    fun clientInitialized()
    fun socketError(errorMessage: String?)
}

