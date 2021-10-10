package cga.exercise.components.geometry.material

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import org.joml.Vector2f

class OverlayMaterial(diff: Texture2D,
    emit: Texture2D,
    specular : Texture2D,
    var overlay : Texture2D,
    shininess: Float = 50.0f,
    tcMultiplier: Vector2f = Vector2f(1.0f) ) : Material(diff, emit, specular, shininess, tcMultiplier) {

    override fun bind(shaderProgram: ShaderProgram) {
        super.bind(shaderProgram)

        shaderProgram.setUniform("overlay", 3)
        overlay.bind(3)

        shaderProgram.setUniform("useOverlay", 1)
    }

    override fun cleanup() {
        super.cleanup()
        overlay.cleanup()
    }

}