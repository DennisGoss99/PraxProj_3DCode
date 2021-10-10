package openGLOutput.exercise.components.geometry.skybox

import openGLOutput.exercise.components.geometry.material.IMaterial
import openGLOutput.exercise.components.shader.ShaderProgram
import openGLOutput.exercise.components.texture.CubeTexture

class SkyboxMaterial(private val cubeTexture: CubeTexture) : IMaterial {

    override fun bind(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("cubeMap",0)
        cubeTexture.bind(0)
    }

    override fun cleanup() {
        cubeTexture.cleanup()
    }

}