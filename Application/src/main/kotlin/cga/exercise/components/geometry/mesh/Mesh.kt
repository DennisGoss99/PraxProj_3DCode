package cga.exercise.components.geometry.mesh

import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.geometry.material.IMaterial
import cga.exercise.components.shader.ShaderProgram
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30.*

/**
 * Creates lastTime Mesh object from vertexdata, intexdata and lastTime given set of vertex attributes
 *
 * @param vertexdata plain float array of vertex data
 * @param indexdata  index data
 * @param attributes vertex attributes contained in vertex data
 * @throws Exception If the creation of the required OpenGL objects fails, an exception is thrown
 *
 * Created by Fabian on 16.09.2017.
 */
class Mesh(vertexdata: FloatArray, indexdata: IntArray, attributes: Array<VertexAttribute>, var material: IMaterial?) {
    //private data
    private var vao = 0
    private var vbo = 0
    private var ibo = 0
    private var indexcount = 0


    init {

        indexcount = indexdata.size;

        // generate IDs
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertexdata, GL_STATIC_DRAW)


        ibo = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,indexdata, GL_STATIC_DRAW)


        for(i in 0 until attributes.size){
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, attributes[i].size, attributes[i].type, false, attributes[i].stride, attributes[i].offset.toLong())
        }

        //Unbind
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * renders the mesh
     */
    private fun render() {
        // activate VAO
        glBindVertexArray(vao);

        // render call
        glDrawElements(GL_TRIANGLES, indexcount, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
        // call the rendering method every frame
    }

    fun render(shaderProgram: ShaderProgram) {
        shaderProgram.use()
        material?.bind(shaderProgram)
        render()
    }

    /**
     * Deletes the previously allocated OpenGL objects for this mesh
     */
    fun cleanup() {
        if (ibo != 0) GL15.glDeleteBuffers(ibo)
        if (vbo != 0) GL15.glDeleteBuffers(vbo)
        if (vao != 0) GL30.glDeleteVertexArrays(vao)

        material?.cleanup()
    }
}