package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector3f

class PointLightHolder(private val PointLightList: MutableList<PointLight>) : IPointLight{

    val Kc = 1.0f
    val Kl = 0.5f
    val Kq = 0.1f

    private var pointLightPositions : FloatArray = FloatArray(PointLightList.size * 3)
    private var pointLightColors : FloatArray = FloatArray(PointLightList.size * 3)

    init {
        for (i in 0 until PointLightList.size * 3) {
                pointLightPositions[i] = PointLightList[i/3].getWorldPosition()[i % 3]
                pointLightColors[i] = PointLightList[i/3].lightColor[i % 3]
        }
    }

    fun add(pointLight: PointLight){
        var newLightPositions : FloatArray = FloatArray(pointLightPositions.size +3)

        newLightPositions = pointLightPositions;
        newLightPositions[newLightPositions.size-3] = pointLight.getWorldPosition().x
        newLightPositions[newLightPositions.size-2] = pointLight.getWorldPosition().y
        newLightPositions[newLightPositions.size-1] = pointLight.getWorldPosition().z

        pointLightPositions = newLightPositions;

        var newLightColor : FloatArray = FloatArray(pointLightPositions.size +3)

        newLightColor = pointLightPositions;
        newLightColor[newLightColor.size-3] = pointLight.lightColor.x
        newLightColor[newLightColor.size-2] = pointLight.lightColor.y
        newLightColor[newLightColor.size-1] = pointLight.lightColor.z
        pointLightColors = newLightColor;
    }


    override fun bind(shaderProgram: ShaderProgram, name: String) {

        shaderProgram.use()

        for (i in 0 until PointLightList.size * 3) {
            pointLightPositions[i] = PointLightList[i/3].getWorldPosition()[i % 3]
            pointLightColors[i] = PointLightList[i/3].lightColor[i % 3]
        }

        shaderProgram.setUniform(name + "Size", PointLightList.size)

        shaderProgram.setUniform(name + "Positions", pointLightPositions)
        shaderProgram.setUniform(name + "Colors", pointLightColors)

        shaderProgram.setUniform(name + "Attenuation", Vector3f(Kc,Kl,Kq))
    }
}