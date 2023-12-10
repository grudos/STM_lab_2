package com.example.lab_2


import android.graphics.RectF
import android.util.Log
import android.util.Range
import android.util.Size
import android.util.SizeF
import com.example.lab_2.messages.InitData
import com.example.lab_2.messages.PaletteInfo
import com.example.lab_2.messages.PaletteUpdate
import com.example.lab_2.messages.ScoreData
import java.io.IOException
import java.io.InputStream
import java.net.ServerSocket


// this shit handles received messages from players and updates ball
// and sends updates back to them
class PongServer : Thread() {
    private var server: ServerSocket? = null
    private lateinit var players: Array<PlayerMessageReceiver?>
    private var ball: Ball? = null
    private lateinit var playerInfo: Array<PlayerInfo?>
    private var pPaletteRange: Range<Float>? = null
    override fun run() {
        val inputStream: InputStream? = null
        try {
            server = ServerSocket(Port)

            // Wait for 2 players
            val startTime = System.currentTimeMillis()
            players = arrayOfNulls(2)
            for (i in 0..1) {
                val socket = server!!.accept()
                players[i] = PlayerMessageReceiver(this, socket, i)
                players[i]!!.start()
            }
            initializeTable(startTime)

            // Send initialization data to players
            for (i in 0..1) {
                val initData = InitData()
                initData.playerId = i
                initData.startTime = startTime
                initData.ballData = ball!!.ballData
                initData.paletteInfoP1 = getPaletteInfo(0)
                initData.paletteInfoP2 = getPaletteInfo(1)
                players[i]!!.sendMessage(initData)
            }
            while (true) {
                sleep(30)
                updateBall()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            Util.closeStream(server)
        }
    }

    @Synchronized
    fun handleMessage(playerId: Int, message: Any) {
        if (message is PaletteUpdate) {
            val update: PaletteUpdate = message as PaletteUpdate
            playerInfo[playerId]!!.position.y += update.yDelta
            sendPaletteData()
        }
    }

    private fun initializeTable(startTime: Long) {
        pPaletteRange = Range.create<Float>(0.0f, TableSize.height - PaletteSize.height)
        playerInfo = arrayOfNulls(2)
        playerInfo[0] = PlayerInfo(Point(0, 0))
        playerInfo[1] = PlayerInfo(Point((TableSize.width - PaletteSize.width).toInt(), 0))
        ball = Ball(startTime)
        ball!!.update(ballStartingPos, BallStartingVelocity)
    }

    @Synchronized
    private fun updateBall() {
        var ballUpdated = false
        var scoreUpdated = false
        val s: Point = ball!!.currentPos
        if (s.y > TableSize.height || s.y < 0.0f) {
            s.y = if (s.y > TableSize.height) TableSize.height.toInt() else 0
            ball!!.update(s, false, true)
            ballUpdated = true
        }
        val p0Scored: Boolean = s.x > TableSize.getWidth()
        val p1Scored = s.x < 0.0f
        if (p0Scored || p1Scored) {
            playerInfo[if (p1Scored) 1 else 0]!!.score++
            ball!!.update(ballStartingPos, BallStartingVelocity)
            scoreUpdated = true
            ballUpdated = true
        } else {
            for (i in 0..1) {
                val pPalettePos = playerInfo[i]!!.position
                val paletteRect = RectF(
                    pPalettePos.x.toFloat(),
                    pPalettePos.y.toFloat(),
                    (pPalettePos.x + PaletteSize.width).toFloat(),
                    (pPalettePos.y + PaletteSize.height).toFloat()
                )
                if (paletteRect.contains(s.x.toFloat(), s.y.toFloat())) {
                    s.x = if (i == 0) pPalettePos.x + PaletteSize.width + 1 else pPalettePos.x - 1
                    ball!!.update(s, true, false)
                    ballUpdated = true
                }
            }
        }
        if (scoreUpdated) {
            sendMessageToAll(createScoreData())
        }
        if (ballUpdated) {
            sendMessageToAll(ball!!.ballData)
        }
    }

    private val ballStartingPos: Point
        private get() = Point((TableSize.width / 2).toInt(), (TableSize.height / 2).toInt())

    private fun sendMessageToAll(message: Any) {
        for (i in players.indices) {
            players[i]!!.sendMessage(message)
        }
    }

    private fun sendPaletteData() {
        for (i in 0..1) {
            sendMessageToAll(getPaletteInfo(i))
        }
    }

    private fun getPaletteInfo(playerId: Int): PaletteInfo {
        val palettePosition = PaletteInfo()
        palettePosition.playerId = playerId
        palettePosition.position = playerInfo[playerId]!!.position
        Log.i(
            "Server",
            String.format(
                "getPaletteInfo(%d): %d %d",
                palettePosition.playerId,
                palettePosition.position!!.x,
                palettePosition.position!!.y
            )
        )
        return palettePosition
    }

    private fun createScoreData(): ScoreData {
        val data = ScoreData()
        data.p1Score = playerInfo[0]!!.score
        data.p2Score = playerInfo[1]!!.score
        return data
    }

    companion object {
        const val Port = 38197
        val TableSize = SizeF(1280.0f, 720.0f)

        val PaletteSize = Size(50, 150)
        private val BallStartingVelocity = Point(10, 7)
    }
}

