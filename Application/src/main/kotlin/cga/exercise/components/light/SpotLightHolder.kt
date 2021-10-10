package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram
import org.joml.Math
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f

class SpotLightHolder(private val SpotLightList: MutableList<SpotLight>) : ISpotLight {

    val Kc = 0.5f
    val Kl = 0.05f
    val Kq = 0.01f

    private var spotLightPositions : FloatArray = FloatArray(SpotLightList.size * 3)
    private var spotLightColors : FloatArray = FloatArray(SpotLightList.size * 3)

    private var spotLightDirections : FloatArray = FloatArray(SpotLightList.size * 3)
    private var spotLightInnerAngles : FloatArray = FloatArray(SpotLightList.size)
    private var spotLightOuterAngles : FloatArray = FloatArray(SpotLightList.size)

    init {

    }

    override fun bind(shaderProgram: ShaderProgram, name: String, viewMatrix: Matrix4f) {

        shaderProgram.use()

        for (i in 0 until SpotLightList.size * 3) {
            spotLightPositions[i] = SpotLightList[i/3].getWorldPosition()[i % 3]
            spotLightColors[i] = SpotLightList[i/3].lightColor[i % 3]

            spotLightDirections[i] = SpotLightList[i/3].getWorldZAxis().negate().mul( Matrix3f(viewMatrix))[i % 3]

            if(i % 3 == 0){
                spotLightInnerAngles[i/3] = Math.toRadians(SpotLightList[i/3].innerAngle)
                spotLightOuterAngles[i/3] = Math.toRadians(SpotLightList[i/3].outerAngle)
            }
        }

        shaderProgram.setUniform(name + "Size", spotLightPositions.size / 3)

        shaderProgram.setUniform(name + "Positions", spotLightPositions)
        shaderProgram.setUniform(name + "Colors", spotLightColors)

        shaderProgram.setUniform(name + "Attenuation", Vector3f(Kc,Kl,Kq))

        shaderProgram.setUniform(name + "Directions", spotLightDirections)
        shaderProgram.setUniformFloatArray(name + "InnerAngles", spotLightInnerAngles)
        shaderProgram.setUniformFloatArray(name + "OuterAngles", spotLightOuterAngles)


    }


}