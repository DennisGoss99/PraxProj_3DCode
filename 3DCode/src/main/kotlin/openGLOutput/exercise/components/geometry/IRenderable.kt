package openGLOutput.exercise.components.geometry

import openGLOutput.exercise.components.shader.ShaderProgram

interface IRenderable {
    fun render(shaderProgram: ShaderProgram)

    fun cleanup()
}