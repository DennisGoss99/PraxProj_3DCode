package cga.exercise.components.geometry.gui.animation

import cga.exercise.components.geometry.RenderCategory
import cga.exercise.components.geometry.gui.GuiElement
import org.joml.Vector2f

class AdvancedAnimatedGuiElement (
    animator: AdvancedAnimator,
    path: String,
    zAxisPosition: Int,
    shouldRender: List<RenderCategory>,
    scale: Vector2f = Vector2f(1f),
    roll: Float = 0f,
    parent: GuiElement? = null
) : AnimatedGuiElement(animator, path, zAxisPosition, shouldRender, scale, roll, parent) {

    override fun update(dt: Float, t: Float) {

        val advancedAnimator = animator as AdvancedAnimator

        val currentState = animator.currentLocationState
        val nextState = (animator.currentLocationState + 1) % advancedAnimator.positions.size

        val currentSpeed = advancedAnimator.positions[currentState].second
        val lastPoint = advancedAnimator.positions[currentState].first
        val nextPoint = advancedAnimator.positions[nextState].first

        move(lastPoint, nextPoint, dt * currentSpeed, nextState)
    }

    override fun changeCurrentLocationState(state: Int) {
        val advancedAnimator = animator as AdvancedAnimator

        advancedAnimator.currentLocationState = state

        setPosition(advancedAnimator.positions[advancedAnimator.currentLocationState].first)

    }


}