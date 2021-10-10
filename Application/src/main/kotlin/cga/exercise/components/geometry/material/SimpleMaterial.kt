package cga.exercise.components.geometry.material

import cga.exercise.components.geometry.material.IMaterial
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D

open class SimpleMaterial(private val texture: Texture2D) : IMaterial {

    override fun bind(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("texture2D", 0)
        texture.bind(0)
    }

    override fun cleanup() {
        texture.cleanup()
    }

}