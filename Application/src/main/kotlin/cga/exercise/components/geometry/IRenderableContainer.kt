package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram

interface IRenderableContainer {

    fun render(cameraModes: List<RenderCategory>, shaderProgram: ShaderProgram)

    fun cleanup()
}