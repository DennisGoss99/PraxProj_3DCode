package openGLOutput.exercise.components.geometry.skybox

import openGLOutput.exercise.components.camera.IPerspective
import openGLOutput.exercise.components.shader.ShaderProgram
import org.joml.*

class SkyboxPerspective {
    companion object : IPerspective {
        override fun bind(shader: ShaderProgram, projectionMatrix: Matrix4f, viewMatrix: Matrix4f) {

            shader.use()
            // Viewmatrix doesnt contain transformations!
            viewMatrix.setColumn(3, Vector4f(0.0f, 0.0f, 0.0f, viewMatrix.get(3,3)))

            shader.setUniform("view_matrix", viewMatrix,false);
            shader.setUniform("projection_matrix", projectionMatrix,false);
        }
    }

}