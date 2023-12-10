package com.example.lab_2.messages

import com.example.lab_2.Point
import java.io.Serializable

// Sent by server to provide clients with updated ball data
class BallData : Serializable {
    var s0: Point? = null
    var v0: Point? = null
}
