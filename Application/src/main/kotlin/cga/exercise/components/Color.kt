package cga.exercise.components

import org.joml.*

class Color : Vector4f {

    constructor(red : Int, green : Int, blue : Int){
        x = red / 255f
        y = green / 255f
        z = blue / 255f
    }

    constructor(red : Int, green : Int, blue : Int, alpha : Int) : this(red , green , blue){
        w = alpha / 255f
    }

    constructor(red : Float, green : Float, blue : Float){
        x = red
        y = green
        z = blue
    }

    constructor(red : Float, green : Float, blue : Float, alpha : Float) : this(red , green , blue){
        w = alpha
    }

    fun toVector3f() : Vector3f = Vector3f(x, y, z)

}