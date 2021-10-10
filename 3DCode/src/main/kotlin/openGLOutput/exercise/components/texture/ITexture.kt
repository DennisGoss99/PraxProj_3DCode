package openGLOutput.exercise.components.texture

import java.nio.ByteBuffer

interface ITexture {
    fun processTexture(imageData: ByteBuffer, width: Int, height: Int, genMipMaps: Boolean)

    fun setTexParams(wrapS: Int, wrapT: Int, minFilter: Int, magFilter: Int) : ITexture

    fun bind(textureUnit: Int)
    fun unbind()

    fun cleanup()
}