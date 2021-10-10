package cga.exercise.components.texture

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.EXTTextureFilterAnisotropic
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer


/**
 * Created by Fabian on 16.09.2017.
 */
open class Texture2D(imageData: ByteBuffer, width: Int, height : Int, genMipMaps: Boolean): ITexture{
    public var texID: Int = -1
        private set

    init {
        try {
            processTexture(imageData, width, height, genMipMaps)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }
    companion object {
        //create texture from file
        //don't support compressed textures for now
        //instead stick to pngs
        operator fun invoke(path: String, genMipMaps: Boolean): Texture2D {
            val x = BufferUtils.createIntBuffer(1)
            val y = BufferUtils.createIntBuffer(1)
            val readChannels = BufferUtils.createIntBuffer(1)
            //flip y coordinate to make OpenGL happy
            STBImage.stbi_set_flip_vertically_on_load(true)
            val imageData = STBImage.stbi_load(path, x, y, readChannels, 4)
                    ?: throw Exception("Image file \"" + path + "\" couldn't be read:\n" + STBImage.stbi_failure_reason())

            try {
                return Texture2D(imageData, x.get(), y.get(), genMipMaps)
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                throw ex
            } finally {
                STBImage.stbi_image_free(imageData)
            }
        }
    }

    override fun processTexture(imageData: ByteBuffer, width: Int, height: Int, genMipMaps: Boolean) {

        texID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texID)

        glTexImage2D(GL_TEXTURE_2D,0, GL_RGBA8, width, height, 0, GL_RGBA,GL_UNSIGNED_BYTE, imageData)

        if (genMipMaps) {
            glGenerateMipmap(GL_TEXTURE_2D)
        }

        unbind()
    }

    override fun setTexParams(wrapS: Int, wrapT: Int, minFilter: Int, magFilter: Int) : Texture2D {

        glBindTexture(GL_TEXTURE_2D,texID)
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,wrapS)
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,wrapT)
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter)
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter)

        glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16.0f)
        unbind()

        return this
    }

    override fun bind(textureUnit: Int) {
        glActiveTexture(GL_TEXTURE0+textureUnit)
        glBindTexture(GL_TEXTURE_2D, texID)
    }


    override fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun cleanup() {
        unbind()
        if (texID != 0) {
            GL11.glDeleteTextures(texID)
            texID = 0
        }
    }
}