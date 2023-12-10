package com.example.lab_2.messages

import com.example.lab_2.Point
import java.io.Serializable


// Sent by server to provide clients with updated palette position data
class PaletteInfo : Serializable {
    var playerId = 0
    var position: Point? = null
}

