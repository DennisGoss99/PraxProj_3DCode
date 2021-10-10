package openGLOutput.exercise.components.light

import openGLOutput.exercise.components.shader.ShaderProgram

interface IPointLight {
    fun bind(shaderProgram: ShaderProgram, name: String)
}