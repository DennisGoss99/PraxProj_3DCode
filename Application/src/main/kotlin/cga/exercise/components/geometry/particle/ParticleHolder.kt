package cga.exercise.components.geometry.particle

import cga.exercise.components.camera.IPerspective
import cga.exercise.components.geometry.IRenderable
import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.geometry.material.*
import cga.exercise.components.geometry.mesh.Mesh
import cga.exercise.components.geometry.transformable.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.*
import org.joml.Math.toRadians
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30

open class ParticleHolder(var particles: MutableList<Particle>, val material: AtlasMaterial , var parent: Transformable? = null) : IRenderable, IPerspective {

    private val mesh : Mesh

    private var viewMatrix = Matrix4f()
    private var viewMatrixTranspose = Matrix4f()
    private var projectionMatrix = Matrix4f()

    init {

        val VBO = floatArrayOf(
            -1f, 1f,
            -1f,-1f,
            1f, 1f,
            1f,-1f)

        val VAO = arrayOf(
            VertexAttribute(2, GL11.GL_FLOAT,8,0)
        )

        val IBO= intArrayOf(
            3, 2, 1,
            0, 1, 2
        )

        mesh = Mesh(VBO, IBO, VAO, material)
    }

    override fun render(shaderProgram: ShaderProgram) {

        shaderProgram.use()

        GL30.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE)
//        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)


        shaderProgram.setUniform("projection_matrix", projectionMatrix,false)

        particles.forEach {
            bindParticleMatrix(shaderProgram, getViewModelMatrix4f(it, viewMatrix))
            bindTextureOffset(shaderProgram, it)
            mesh.render(shaderProgram)
        }


        GL11.glDepthMask(true)
//        GL30.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL30.glDisable(GL11.GL_BLEND)

    }

    private fun bindTextureOffset(shaderProgram: ShaderProgram, particle: Particle){
        val lifeFactor = particle.elapsedTime / particle.lifeLength
        val atlasProgression = lifeFactor * material.imageCount

        val image = Math.floor(atlasProgression)
        val blend = atlasProgression % 1


        shaderProgram.setUniform("offsetImage", image)
        shaderProgram.setUniform("blend", blend)
    }

    private fun setTextureOffset(offset: Float){
        //setzen wir einmal die Texturcoordinate und den Offset Piartikel? Ich denke wir setzen nur die Texturkoordinaten
    }
    private fun getViewModelMatrix4f(particle: Particle, viewMatrix: Matrix4f) : Matrix4f{
        var modelMatrix = Matrix4f()

        modelMatrix.translate(particle.position)
        modelMatrix.set3x3(viewMatrixTranspose)

        modelMatrix.rotate(toRadians(particle.rotation),Vector3f(0f,0f,1f))
        modelMatrix.scale(particle.scale)

        viewMatrix.mul(modelMatrix,modelMatrix)

        return modelMatrix
    }

    private fun bindParticleMatrix(shaderProgram: ShaderProgram, viewModelMatrix: Matrix4f){
        shaderProgram.setUniform("viewModel_matrix", viewModelMatrix,false)
    }

    override fun bind(shader: ShaderProgram, projectionMatrix: Matrix4f, viewMatrix: Matrix4f) {
        this.viewMatrix = Matrix4f(viewMatrix)
        this.viewMatrixTranspose = Matrix4f(viewMatrix).transpose()
        this.projectionMatrix = Matrix4f(projectionMatrix)
    }

    override fun cleanup() {
        mesh.cleanup()

    }




}