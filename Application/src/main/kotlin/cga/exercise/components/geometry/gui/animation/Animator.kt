package cga.exercise.components.geometry.gui.animation

import org.joml.Vector2f

data class Animator(var speed : Float, var positions : List<Vector2f>, override var currentLocationState : Int = 0) : IAnimator {
    override fun getStartPosition(): Vector2f = positions[currentLocationState]
}