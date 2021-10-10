package cga.exercise.components.light

import cga.exercise.components.geometry.transformable.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.*

public open class PointLight(position: Vector3f, var lightColor : Vector3f, parent: Transformable? = null) : Transformable(parent = parent), IPointLight {

    open val Kc = 1.0f
    open val Kl = 0.5f
    open val Kq = 0.1f

    init {
        translateGlobal(position);
    }

    override fun bind(shaderProgram: ShaderProgram, name: String) {
        shaderProgram.use()
        shaderProgram.setUniform(name + "Color", lightColor)
        shaderProgram.setUniform(name + "Position", getWorldPosition())
        shaderProgram.setUniform(name + "Attenuation", Vector3f(Kc,Kl,Kq))
    }
}