package cga.exercise.components.light

import cga.exercise.components.geometry.transformable.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Math
import org.joml.*

public class SpotLight(position: Vector3f, lightColor: Vector3f, var innerAngle: Float, var outerAngle: Float, parent: Transformable? = null) : PointLight( position, lightColor, parent), ISpotLight{

    override val Kc = 0.5f
    override val Kl = 0.05f
    override val Kq = 0.01f


    override fun bind(shaderProgram: ShaderProgram, name: String, viewMatrix: Matrix4f) {

        super.bind(shaderProgram,name)
        shaderProgram.setUniform(name + "Direction", getWorldZAxis().negate().mul( Matrix3f(viewMatrix)))
        shaderProgram.setUniform(name + "InnerAngle", Math.toRadians(innerAngle))
        shaderProgram.setUniform(name + "OuterAngle", Math.toRadians(outerAngle))
    }


}