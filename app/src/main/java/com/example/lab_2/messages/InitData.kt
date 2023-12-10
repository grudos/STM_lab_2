package com.example.lab_2.messages

import java.io.Serializable


class InitData : Serializable {
    var playerId = 0
    var startTime: Long = 0
    var ballData: BallData? = null
    var paletteInfoP1: PaletteInfo? = null
    var paletteInfoP2: PaletteInfo? = null
}
