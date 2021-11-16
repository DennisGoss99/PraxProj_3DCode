package openGLOutput.exercise.components.geometry.mesh

import openGLOutput.exercise.components.geometry.IRenderable
import openGLOutput.exercise.components.geometry.transformable.Transformable
import openGLOutput.exercise.components.shader.ShaderProgram
import org.joml.Matrix4f
import org.joml.Vector3f


open class RenderableBase(var meshes: MutableList<Mesh>, modelMatrix: Matrix4f = Matrix4f(), parent: Transformable? = null) : Transformable(modelMatrix, parent),
    IRenderable {

    var emitColor = Vector3f(1f ,1f ,1f)

    override fun render(shaderProgram: ShaderProgram) {
        shaderProgram.use()

        shaderProgram.setUniform("model_matrix" , getWorldModelMatrix(),false)
        shaderProgram.setUniform("emitColor", emitColor)

        meshes.forEach { m -> m.render(shaderProgram) }
    }

    override fun cleanup() {
        meshes.forEach { it.cleanup() }
    }
}