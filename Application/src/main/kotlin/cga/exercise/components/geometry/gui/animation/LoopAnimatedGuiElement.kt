package cga.exercise.components.geometry.gui.animation

import cga.exercise.components.geometry.RenderCategory
import cga.exercise.components.geometry.gui.GuiElement
import org.joml.Vector2f

class LoopAnimatedGuiElement(
    animator: Animator,
    path: String,
    zAxisPosition: Int,
    shouldRender: List<RenderCategory>,
    scale: Vector2f = Vector2f(1f),
    roll: Float = 0f,
    parent: GuiElement? = null
) : AnimatedGuiElement(animator, path, zAxisPosition, shouldRender, scale, roll, parent) {

    override fun update(dt: Float, t: Float) {

        val loopAnimator = animator as Animator

        val currentState = animator.currentLocationState
        val nextState = (animator.currentLocationState + 1) % loopAnimator.positions.size

        val lastPoint = loopAnimator.positions[currentState]
        val nextPoint = loopAnimator.positions[nextState]

        move(lastPoint, nextPoint, dt * loopAnimator.speed, nextState)
    }

    override fun changeCurrentLocationState(state: Int) {
        val loopAnimator = animator as Animator

        loopAnimator.currentLocationState = state

        setPosition(loopAnimator.positions[loopAnimator.currentLocationState])
    }

}