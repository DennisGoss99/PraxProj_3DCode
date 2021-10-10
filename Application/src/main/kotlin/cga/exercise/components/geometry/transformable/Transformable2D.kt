package cga.exercise.components.geometry.transformable

import org.joml.*
import org.joml.Math.toRadians
import java.text.FieldPosition

open class Transformable2D (var modelMatrix : Matrix4f = Matrix4f(), var parent: Transformable2D? = null) {

    fun rotateLocal( roll: Float) = modelMatrix.rotateXYZ(0f,0f,toRadians(roll));

    fun translateLocal(deltaPos: Vector2f){
        modelMatrix.translate(Vector3f(deltaPos,0f))
    }

    fun translateGlobal(deltaPos: Vector2f){
        val tempMatrix = Matrix4f().translate(Vector3f(deltaPos,0f))
        modelMatrix = tempMatrix.mul(modelMatrix);
    }

    fun scaleLocal(scale: Vector2f) = modelMatrix.scale(Vector3f(scale,1f))

    fun getWorldModelMatrix(): Matrix4f {
        // transformable parent abfragen, wenn ja links multiplikation der partent matrix wenn nein dann selbst world matrix, kein Patent kopie von Modelmatrix
        val world = Matrix4f(modelMatrix)
        if (parent != null) {
            parent!!.getWorldModelMatrix().mul(modelMatrix, world)
        }
        return world
    }

    fun getLocalModelMatrix(): Matrix4f {
        return Matrix4f(modelMatrix)
    }

    fun getPosition(): Vector2f {
        val tempVector = Vector3f();
        modelMatrix.getColumn(3,tempVector)
        return Vector2f(tempVector.x,tempVector.y);
    }

    fun setPosition(position: Vector2f){
        modelMatrix.set(3,0, position.x)
        modelMatrix.set(3,1, position.y)
    }

    fun getWorldPosition(): Vector2f {
        val tempVector = Vector3f();
        getWorldModelMatrix().getColumn(3, tempVector)
        return Vector2f(tempVector.x,tempVector.y);
    }

}