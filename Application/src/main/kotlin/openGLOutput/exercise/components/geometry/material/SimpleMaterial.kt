package openGLOutput.exercise.components.geometry.material

import openGLOutput.exercise.components.shader.ShaderProgram
import openGLOutput.exercise.components.texture.Texture2D

open class SimpleMaterial(private val texture: Texture2D) : IMaterial {

    override fun bind(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("texture2D", 0)
        texture.bind(0)
    }

    override fun cleanup() {
        texture.cleanup()
    }

}