package cga.exercise.components.geometry.gui.animation

import cga.exercise.components.geometry.RenderCategory
import cga.exercise.components.geometry.gui.GuiElement
import org.joml.Vector2f

abstract class AnimatedGuiElement(
    var animator: IAnimator,
    path: String,
    zAxisPosition : Int,
    shouldRender: List<RenderCategory>,
    scale: Vector2f = Vector2f(1f),
    roll: Float = 0f,
    parent: GuiElement? = null
) : GuiElement(path, zAxisPosition, shouldRender, scale, animator.getStartPosition(), roll, parent) {

    abstract fun update(dt : Float, t: Float)

    fun move(lastPoint: Vector2f, nextPoint: Vector2f, currentSpeed: Float, nextState: Int) {

        if(currentSpeed == 0f)
            return

        val direction = Vector2f(lastPoint.x - nextPoint.x, lastPoint.y - nextPoint.y)

        translateLocal(direction.negate().normalize().mul( currentSpeed))

        val currentPosition = getPosition()

        var changeState = false

        when {
            direction.x >= 0 && direction.y >= 0 ->
                if (currentPosition.x >= nextPoint.x && currentPosition.y >= nextPoint.y)
                    changeState = true

            direction.x <= 0 && direction.y <= 0 ->
                if (currentPosition.x <= nextPoint.x && currentPosition.y <= nextPoint.y)
                    changeState = true

            direction.x >= 0 && direction.y <= 0 ->
                if (currentPosition.x >= nextPoint.x && currentPosition.y <= nextPoint.y)
                    changeState = true

            direction.x <= 0 && direction.y >= 0 ->
                if (currentPosition.x <= nextPoint.x && currentPosition.y >= nextPoint.y)
                    changeState = true

        }

        if (changeState) {
            setPosition(nextPoint)
            animator.currentLocationState = nextState
        }
    }

    abstract fun changeCurrentLocationState(state : Int)

}