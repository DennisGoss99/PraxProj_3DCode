package openGLOutput.exercise.components.geometry.material

import openGLOutput.exercise.components.shader.ShaderProgram

interface IMaterial {
    fun bind(shaderProgram: ShaderProgram)

    fun cleanup()
}