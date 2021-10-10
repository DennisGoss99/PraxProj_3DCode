package openGLOutput.exercise.components.light

import openGLOutput.exercise.components.shader.ShaderProgram
import org.joml.Matrix4f

interface ISpotLight {
    fun bind(shaderProgram: ShaderProgram, name: String, viewMatrix: Matrix4f)
}