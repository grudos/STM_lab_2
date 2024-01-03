package com.example.lab_2

import android.content.Context
import android.graphics.*
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.View
import java.lang.String
import kotlin.Boolean
import kotlin.Float


class PongView(context: Context?, private val pongClient: PongClient) : View(context)
{
    private val paint = Paint()
    private var canvasSize: Size? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val sx = getScaleX(width.toFloat())
        val sy = getScaleY(height.toFloat())
        if (canvasSize == null) {
            canvasSize = Size(width, height)
        }
        paint.setARGB(255, 0, 0, 0)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.color = Color.LTGRAY
        paint.strokeWidth = 2.0f
        canvas.drawLine((width / 2).toFloat(), 0.0f, (width / 2).toFloat(), height.toFloat(), paint)
        paint.color = Color.WHITE
        for (i in 0..1) {
            val palette = scaleRect(pongClient.getPalettePos(i), sx, sy)
            canvas.drawRoundRect(palette, 1.0f, 1.0f, paint)
        }
        paint.textSize = 52.0f
        canvas.drawText(String.valueOf(pongClient.getScore(0)), (width / 2 - 100).toFloat(), 100f, paint)
        canvas.drawText(String.valueOf(pongClient.getScore(1)), (width / 2 + 80).toFloat(), 100f, paint)
        val ballPos: Point = pongClient.getBallPos()!!
        ballPos.x = (ballPos.x * sx).toInt()
        ballPos.y = (ballPos.y * sy).toInt()
        canvas.drawCircle(ballPos.x.toFloat(), ballPos.y.toFloat(), 25.0f, paint)
        this.invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_MOVE && event.historySize > 0) {
            for (i in 0 until event.pointerCount) {
                val isOnLeftSide = event.getX(i) < canvasSize!!.width / 2
                if (isOnLeftSide && pongClient.playerId == 0 || !isOnLeftSide && pongClient.playerId == 1)
                {
                    val deltaY = event.getY(i) - event.getHistoricalY(i, 0)
                    Log.i("W", deltaY.toString())
                    pongClient.movePalette(deltaY)
                }
            }
        }
        return true
    }

    private fun scaleRect(rect: RectF, sx: Float, sy: Float): RectF {
        return RectF(rect.left * sx, rect.top * sy, rect.right * sx, rect.bottom * sy)
    }

    companion object {
        private fun getScaleX(cx: Float): Float {
            return cx / PongServer.TableSize.width
        }

        private fun getScaleY(cy: Float): Float {
            return cy / PongServer.TableSize.height
        }
    }
}

