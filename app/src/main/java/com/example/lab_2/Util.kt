package com.example.lab_2

import java.io.Closeable
import java.io.IOException


object Util {
    fun closeStream(s: Closeable?) {
        try {
            s?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

