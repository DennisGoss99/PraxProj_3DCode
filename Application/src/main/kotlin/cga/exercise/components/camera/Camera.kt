package cga.exercise.components.camera

import cga.exercise.components.geometry.transformable.Transformable
import cga.exercise.components.shader.ShaderProgram

import org.joml.Math.toRadians
import org.joml.Matrix4f

open class Camera(
    val FieldofView: Float = 90f,
    val AspectRatio : Float = 16f/9f,
    val NearPlane: Float = 0.1f, val
    FarPlane: Float = 4000f,
    modelMatrix: Matrix4f = Matrix4f(),
    parent: Transformable? = null) : Transformable(modelMatrix, parent), ICamera, IPerspective{

    override fun getCalculateViewMatrix(): Matrix4f {
        return Matrix4f().lookAt(getWorldPosition(),getWorldPosition().sub(getWorldZAxis()), getWorldYAxis())
    }

    override fun getCalculateProjectionMatrix(): Matrix4f {
        return Matrix4f().perspective(toRadians(FieldofView),AspectRatio,NearPlane,FarPlane)
    }

    override fun bind(shader: ShaderProgram, projectionMatrix: Matrix4f, viewMatrix: Matrix4f) {
        shader.use()
        shader.setUniform("view_matrix", viewMatrix,false);
        shader.setUniform("projection_matrix", projectionMatrix,false);
    }
}