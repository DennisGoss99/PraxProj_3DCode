package openGLOutput.exercise.components.camera

import openGLOutput.exercise.components.shader.ShaderProgram
import org.joml.Matrix4f

interface IPerspective {

    fun bind(shader: ShaderProgram, projectionMatrix : Matrix4f, viewMatrix : Matrix4f)

}