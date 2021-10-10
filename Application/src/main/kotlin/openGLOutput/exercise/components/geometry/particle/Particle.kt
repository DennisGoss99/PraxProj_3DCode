package openGLOutput.exercise.components.geometry.particle

import org.joml.*

class Particle(var position : Vector3f, var velocity : Vector3f, val lifeLength : Float, var rotation : Float, var scale : Float) {

    var elapsedTime = 0f

    fun update(dt: Float) : Boolean{

        val movement = Vector3f(velocity).mul(dt)

        position.add(movement)

        elapsedTime += dt

        return elapsedTime >= lifeLength
    }

    fun getCopy() : Particle = Particle(Vector3f(position), Vector3f(velocity), lifeLength, rotation, scale)

}