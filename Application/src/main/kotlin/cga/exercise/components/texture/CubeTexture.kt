package cga.exercise.components.texture

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer

class CubeTexture(imageData: List<ByteBuffer>, val width: Int,val height : Int, genMipMaps: Boolean) {

    public var texID: Int = -1
        private set

    init {
        try {
            processTexture(imageData, genMipMaps)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }
    companion object {
        //create texture from file
        //don't support compressed textures for now
        //instead stick to pngs
        operator fun invoke(paths: List<String>, genMipMaps: Boolean): CubeTexture {
            val buffers = mutableListOf<ByteBuffer>()
            var width : Int = -1
            var height : Int = -1

            for (path in paths){
                val x = BufferUtils.createIntBuffer(1)
                val y = BufferUtils.createIntBuffer(1)
                val readChannels = BufferUtils.createIntBuffer(1)
                //flip y coordinate to make OpenGL happy
                STBImage.stbi_set_flip_vertically_on_load(true)
                val imageData = STBImage.stbi_load(path, x, y, readChannels, 4)
                    ?: throw Exception("Image file \"" + path + "\" couldn't be read:\n" + STBImage.stbi_failure_reason())
                buffers.add(imageData)

                if(width != -1 && height != -1) {
                    if (width != x.get() || height != y.get())
                        throw Exception("All cube lengths must be equal")
                }
                else
                {
                    width = x.get()
                    height = y.get()

                    if(width != height)
                        throw Exception("All cube lengths must be equal")
                }
            }

            try {
                return CubeTexture(buffers, height, width, genMipMaps)
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                throw ex
            } finally {
                buffers.forEach { b -> STBImage.stbi_image_free(b)}
            }
        }
    }


    fun processTexture(imageData: List<ByteBuffer>, genMipMaps: Boolean) {
        texID = glGenTextures()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_CUBE_MAP, texID)

        for(i in 0 until imageData.size){
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData[i])
        }

        glTexParameterIi(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameterIi(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)
        unbind()
    }

//    override fun processTexture(imageData: ByteBuffer, width: Int, height: Int, genMipMaps: Boolean) {
//        TODO("Not yet implemented")
//    }
//
//    override fun setTexParams(wrapS: Int, wrapT: Int, minFilter: Int, magFilter: Int) {
//        TODO("Not yet implemented")
//    }

    fun bind(textureUnit: Int) {
        glActiveTexture(GL_TEXTURE0+textureUnit)
        glBindTexture(GL_TEXTURE_CUBE_MAP, texID)
    }

    fun unbind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0)
    }

    fun cleanup() {
        unbind()
        if (texID != 0) {
            GL11.glDeleteTextures(texID)
            texID = 0
        }
    }
}