package com.example.lab_2

import android.app.Activity
import android.graphics.RectF
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.example.lab_2.messages.*
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.Socket


class PongClient(private val serverHostName: String) : Thread() {
    private var callbackListener: PongConnectionListener? = null
    private var callbackHandler: Activity? = null
    private var messageHandler: Handler? = null
    private var client: Socket? = null
    private var outputStream: ObjectOutputStream? = null
    var playerId: Int private set
    private var ball: Ball? = null
    private lateinit var playerInfo: Array<PlayerInfo?>

    init {
        playerId = -1
    }

    fun getPalettePos(playerId: Int): RectF {
        val pos = playerInfo[playerId]!!.position
        return RectF(
            pos.x.toFloat(),
            pos.y.toFloat(),
            (pos.x + PongServer.PaletteSize.width).toFloat(),
            (pos.y + PongServer.PaletteSize.height).toFloat()
        )
    }

    fun getScore(playerId: Int): Int {
        return playerInfo[playerId]!!.score
    }

    fun getBallPos(): Point? {
        return ball!!.currentPos
    }

    fun movePalette(deltaY: Float) {
        val update = PaletteUpdate()
        update.yDelta = deltaY.toInt()
        sendMessage(update)
    }

    fun start(listener: PongConnectionListener?, handler: Activity?) {
        callbackListener = listener
        callbackHandler = handler
        super.start()
    }

    override fun run() {
        try {
            callbackHandler!!.runOnUiThread { callbackListener!!.clientConnecting() }
            client = Socket()
            client!!.connect(InetSocketAddress(serverHostName, PongServer.Port), 2000)
            callbackHandler!!.runOnUiThread { callbackListener!!.clientConnected() }
            val ois = ObjectInputStream(client!!.getInputStream())
            outputStream = ObjectOutputStream(client!!.getOutputStream())
            val initData: InitData = ois.readObject() as InitData // Sent by server after accepting connection
            initializeTable(initData)
            val handlerThread = HandlerThread("THREAD!")
            handlerThread.start()
            val looper = handlerThread.looper
            messageHandler = Handler(looper)
            callbackHandler!!.runOnUiThread { callbackListener!!.clientInitialized() }
            while (true) {
                val obj = ois.readObject()
                handleMessage(obj)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            callbackHandler!!.runOnUiThread { callbackListener!!.socketError(e.message) }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            callbackHandler!!.runOnUiThread { callbackListener!!.socketError(e.message) }
        } finally {
            Util.closeStream(client)
        }
    }

    private fun initializeTable(initData: InitData) {
        playerId = initData.playerId
        ball = Ball(System.currentTimeMillis())
        ball!!.update(initData.ballData!!.s0, initData.ballData!!.v0)
        playerInfo = arrayOfNulls(2)
        playerInfo[0] = PlayerInfo(initData.paletteInfoP1!!.position!!)
        playerInfo[1] = PlayerInfo(initData.paletteInfoP2!!.position!!)
    }

    private fun handleMessage(message: Any) {
        if (message is BallData) {
            val ballData: BallData = message as BallData
            ball!!.update(ballData.s0, ballData.v0)
        } else if (message is InitData) {
            initializeTable(message as InitData)
        } else if (message is PaletteInfo) {
            val palInfo: PaletteInfo = message as PaletteInfo
            playerInfo[palInfo.playerId]!!.position = palInfo.position!!
            Log.i(
                "Klient",
                java.lang.String.format(
                    "Received %d: %d %d",
                    palInfo.playerId,
                    palInfo.position!!.x,
                    palInfo.position!!.y
                )
            )
        } else if (message is ScoreData) {
            val scoreData: ScoreData = message as ScoreData
            playerInfo[0]!!.score = scoreData.p1Score
            playerInfo[1]!!.score = scoreData.p2Score
        }
    }

    private fun sendMessage(message: Any) {
        messageHandler!!.post {
            try {
                outputStream!!.writeObject(message)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
