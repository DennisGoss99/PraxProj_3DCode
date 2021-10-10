package cga.exercise.components.geometry.skybox

import cga.exercise.components.geometry.material.IMaterial
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.CubeTexture

class SkyboxMaterial(private val cubeTexture: CubeTexture) : IMaterial {

    override fun bind(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("cubeMap",0)
        cubeTexture.bind(0)
    }

    override fun cleanup() {
        cubeTexture.cleanup()
    }

}