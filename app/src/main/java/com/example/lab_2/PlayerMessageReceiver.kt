package com.example.lab_2

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket


class PlayerMessageReceiver(
    private val server: PongServer,
    private val playerSocket: Socket,
    private val playerId: Int
) : Thread() {
    private var outputStream: ObjectOutputStream? = null
    fun sendMessage(message: Any?) {
        try {
            outputStream!!.writeObject(message)
            outputStream!!.reset()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        try {
            outputStream = ObjectOutputStream(playerSocket.getOutputStream())
            val objStream = ObjectInputStream(playerSocket.getInputStream())
            while (true) {
                val obj = objStream.readObject()
                server.handleMessage(playerId, obj)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }
}

