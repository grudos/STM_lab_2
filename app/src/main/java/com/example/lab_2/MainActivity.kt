package com.example.lab_2

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast


class MainActivity : Activity(), PongConnectionListener {
    private var pongView: PongView? = null
    private var pongServer: PongServer? = null
    private var pongClient: PongClient? = null
    private var txtHostName: EditText? = null
    private var txtStatus: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setContentView(R.layout.activity_main)
        txtHostName = findViewById<EditText>(R.id.ui_txtHostName)
        txtStatus = findViewById<TextView>(R.id.ui_txtStatus)
    }

    override fun clientConnecting() {
        txtStatus!!.text = "Connecting to the server"
    }

    override fun clientConnected() {
        txtStatus!!.text = "Waiting for the game to start"
    }

    override fun clientInitialized() {
        renderTable(pongClient)
    }

    override fun socketError(errorMessage: String?) {
        txtStatus!!.text = "Cannot connect to server.\n$errorMessage"
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    fun StartServer_Click(view: View?) {
        pongServer = PongServer()
        pongServer!!.start()
        pongClient = PongClient("127.0.0.1")
        pongClient!!.start(this, this)
    }

    fun ConnectToServer_Click(view: View?) {
        pongClient = PongClient(txtHostName!!.text.toString())
        pongClient!!.start(this, this)
    }

    private fun renderTable(pongClient: PongClient?) {
        pongView = PongView(this, pongClient!!)
        setContentView(pongView)
    }
}