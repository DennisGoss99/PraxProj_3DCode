package cga.exercise.components.geometry.gui.animation

import org.joml.Vector2f

data class AdvancedAnimator(var positions: List<Pair<Vector2f,Float>>, override var currentLocationState: Int = 0) : IAnimator {

    override fun getStartPosition(): Vector2f = positions[currentLocationState].first

}