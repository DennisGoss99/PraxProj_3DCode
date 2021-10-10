package cga.exercise.components.geometry.mesh

import cga.exercise.components.geometry.IRenderable
import cga.exercise.components.geometry.transformable.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Matrix4f


open class RenderableBase(var meshes: MutableList<Mesh>, modelMatrix: Matrix4f = Matrix4f(), parent: Transformable? = null) : Transformable(modelMatrix, parent),
    IRenderable {

    override fun render(shaderProgram: ShaderProgram) {
        shaderProgram.use()
        shaderProgram.setUniform("model_matrix" , getWorldModelMatrix(),false)
        meshes.forEach { m -> m.render(shaderProgram) }
    }

    override fun cleanup() {
        meshes.forEach { it.cleanup() }
    }
}