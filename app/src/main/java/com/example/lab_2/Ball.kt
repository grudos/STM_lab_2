package com.example.lab_2

import com.example.lab_2.messages.BallData
import kotlin.math.sign

class Ball(private val startTime: Long)
{
    private val acceleration = 32.0f
    private var velocity0: Point? = null
    private var position0: Point? = null
    private var time0 = 0.0f

    val currentPos: Point
        get() {
            val t = currentTime - time0
            return calculatePath(position0, t, velocity0, acceleration)
        }

    val ballData: BallData
        get() {
            val data = BallData()
            data.s0 = position0
            data.v0 = velocity0
            return data
        }

    private val currentTime: Float
        get() = (System.currentTimeMillis() - startTime) / 1000.0f


    fun update(s0: Point?, negate_x: Boolean, negate_y: Boolean) {
        val t = currentTime - time0
        velocity0 = calculateVelocity(velocity0, t, acceleration)
        position0 = s0
        time0 = currentTime
        if (negate_x) {
            velocity0!!.x = -velocity0!!.x
        }
        if (negate_y) {
            velocity0!!.y = -velocity0!!.y
        }
    }

    fun update(s0: Point?, v0: Point?) {
        position0 = s0
        velocity0 = v0
        time0 = currentTime
    }


    companion object {
        private fun calculatePath(s0: Point?, t: Float, v0: Point?, a: Float): Point {
            return Point(
                (s0!!.x + v0!!.x * t + sign(v0.x.toFloat()) * a * t * t / 2).toInt(),
                (s0.y + v0.y * t + sign(v0.y.toFloat()) * a * t * t / 2).toInt()
            )
        }

        private fun calculateVelocity(v0: Point?, t: Float, a: Float): Point {
            return Point((v0!!.x + sign(v0.x.toFloat()) * a * t).toInt(), (v0.y + sign(v0.y.toFloat()) * a * t).toInt())
        }
    }
}
