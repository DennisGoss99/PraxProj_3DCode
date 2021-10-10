package cga.exercise.components.geometry.gui.animation

import org.joml.Vector2f

interface IAnimator {
    var currentLocationState : Int
    fun getStartPosition() : Vector2f
}