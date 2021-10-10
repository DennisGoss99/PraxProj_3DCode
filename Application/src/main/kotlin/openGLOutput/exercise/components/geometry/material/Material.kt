package openGLOutput.exercise.components.geometry.material

import openGLOutput.exercise.components.shader.ShaderProgram
import openGLOutput.exercise.components.texture.Texture2D
import org.joml.Vector2f

open class Material(var diff: Texture2D,
                    var emit: Texture2D,
                    var specular: Texture2D,
                    var shininess: Float = 50.0f,
                    var tcMultiplier : Vector2f = Vector2f(1.0f)) : IMaterial {

    override fun bind(shaderProgram: ShaderProgram) {

        shaderProgram.setUniform("shininess", shininess)
        shaderProgram.setUniform("tcMultiplier", tcMultiplier)

        shaderProgram.setUniform("diff",0)
        diff.bind(0)

        shaderProgram.setUniform("emit", 1)
        emit.bind(1)

        shaderProgram.setUniform("spec", 2)
        specular.bind(2)

        shaderProgram.setUniform("useOverlay", 0)
    }

    override fun cleanup() {
        diff.cleanup()
        emit.cleanup()
        specular.cleanup()
    }
}