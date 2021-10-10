package cga.exercise.components.geometry.gui

import cga.exercise.components.geometry.RenderCategory
import cga.exercise.components.geometry.mesh.Mesh
import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.geometry.material.SimpleMaterial
import cga.exercise.components.geometry.transformable.Transformable2D
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import org.joml.*
import org.lwjgl.opengl.AMDSamplePositions
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*


open class GuiElement(path: String,
                      val zAxisPosition : Int,
                      val shouldRender : List<RenderCategory>,
                      protected var scale: Vector2f = Vector2f(1f),
                      protected var translate: Vector2f  = Vector2f(0f),
                      protected var roll: Float = 0f, parent: GuiElement? = null) : Transformable2D(parent = parent ){

    private val mesh : Mesh
    private var tex : Texture2D

    init{

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

        tex = Texture2D(path,false).setTexParams(GL_CLAMP_TO_EDGE,GL_CLAMP_TO_EDGE, GL_LINEAR, GL_LINEAR)


        var material = SimpleMaterial(tex)

        mesh = Mesh(VBO, IBO, VAO, material)

        translateLocal(translate)
        rotateLocal(roll)
        scaleLocal(scale)

    }

    open fun beforeRender(){
    }

    fun render(shaderProgram: ShaderProgram){
        beforeRender()
        shaderProgram.setUniform("transformationMatrix" , getWorldModelMatrix(),false)
        mesh.render(shaderProgram)
    }

    /**
     * Deletes the previously allocated OpenGL objects for this mesh
     */
    fun cleanup() {
        mesh.cleanup()
    }

}