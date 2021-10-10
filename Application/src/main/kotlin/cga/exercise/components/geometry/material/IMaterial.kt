package cga.exercise.components.geometry.material

import cga.exercise.components.shader.ShaderProgram

interface IMaterial {
    fun bind(shaderProgram: ShaderProgram)

    fun cleanup()
}