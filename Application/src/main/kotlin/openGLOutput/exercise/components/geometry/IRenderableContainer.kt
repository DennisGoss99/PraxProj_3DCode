package openGLOutput.exercise.components.geometry

import openGLOutput.exercise.components.shader.ShaderProgram

interface IRenderableContainer {

    fun render(cameraModes: List<RenderCategory>, shaderProgram: ShaderProgram)

    fun cleanup()
}