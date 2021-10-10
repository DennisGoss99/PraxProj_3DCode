package cga.exercise.components.geometry.transformable

import org.joml.Math.toRadians
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f


open class Transformable(var modelMatrix: Matrix4f = Matrix4f(), var parent: Transformable? = null) {

    /**
     * Rotates object around its own origin.
     * @param pitch radiant angle around x-axis ccw
     * @param yaw radiant angle around y-axis ccw
     * @param roll radiant angle around z-axis ccw
     */
    fun rotateLocal(pitch: Float, yaw: Float, roll: Float) = modelMatrix.rotateXYZ(toRadians(pitch),toRadians(yaw),toRadians(roll));

    /**
     * Rotates object around given rotation center.
     * @param pitch radiant angle around x-axis ccw
     * @param yaw radiant angle around y-axis ccw
     * @param roll radiant angle around z-axis ccw
     * @param altMidpoint rotation center
     */
    fun rotateAroundPoint(pitch: Float, yaw: Float, roll: Float, altMidpoint: Vector3f) {
        val tempMatrix = Matrix4f().translate(altMidpoint)
        tempMatrix.rotateXYZ(pitch,yaw,roll)
        tempMatrix.translate(altMidpoint.negate())
        modelMatrix = tempMatrix.mul(modelMatrix)
    }

    /**
     * Translates object based on its own coordinate system.
     * @param deltaPos delta positions
     */
    fun translateLocal(deltaPos: Vector3f){
        modelMatrix.translate(deltaPos)
    }

    /**
     * Translates object based on its parent coordinate system.
     * Hint: global operations will be left-multiplied
     * @param deltaPos delta positions (x, y, z)
     */
    fun translateGlobal(deltaPos: Vector3f){
        val tempMatrix = Matrix4f().translate(deltaPos)
        modelMatrix = tempMatrix.mul(modelMatrix);
    }

    /**
     * Scales object related to its own origin
     * @param scale scale factor (x, y, z)
     */
    fun scaleLocal(scale: Vector3f) = modelMatrix.scale(scale)

    fun setPosition(position: Vector3f){
        modelMatrix.set(3,0, position.x)
        modelMatrix.set(3,1, position.y)
        modelMatrix.set(3,2, position.z)
    }
    /**
     * Returns position based on aggregated translations.
     * Hint: last column of model matrix
     * @return position
     */
    fun getPosition(): Vector3f {
        val tempVector = Vector3f();
        modelMatrix.getColumn(3,tempVector)
        return tempVector;
    }

    /**
     * Returns position based on aggregated translations incl. parents.
     * Hint: last column of world model matrix
     * @return position
     */
    fun getWorldPosition(): Vector3f {
        val tempVector = Vector3f();
        getWorldModelMatrix().getColumn(3, tempVector)
        return tempVector;
    }

    /**
     * Returns x-axis of object coordinate system
     * Hint: first normalized column of model matrix
     * @return x-axis
     */
    fun getXAxis(): Vector3f {
        val tempVector = Vector3f();
        getLocalModelMatrix().getColumn(0, tempVector).normalize()
        return tempVector;
    }

    /**
     * Returns y-axis of object coordinate system
     * Hint: second normalized column of model matrix
     * @return y-axis
     */
    fun getYAxis(): Vector3f {
        val tempVector = Vector3f();
        getLocalModelMatrix().getColumn(1, tempVector).normalize()
        return tempVector;
    }

    /**
     * Returns z-axis of object coordinate system
     * Hint: third normalized column of model matrix
     * @return z-axis
     */
    fun getZAxis(): Vector3f {
        val tempVector = Vector3f();
        getLocalModelMatrix().getColumn(2, tempVector).normalize()
        return tempVector;
    }

    /**
     * Returns x-axis of world coordinate system
     * Hint: first normalized column of world model matrix
     * @return x-axis
     */
    fun getWorldXAxis(): Vector3f {
        val tempVector = Vector3f();
        getWorldModelMatrix().getColumn(0, tempVector).normalize()
        return tempVector;
    }

    /**
     * Returns y-axis of world coordinate system
     * Hint: second normalized column of world model matrix
     * @return y-axis
     */
    fun getWorldYAxis(): Vector3f {
        val tempVector = Vector3f();
        getWorldModelMatrix().getColumn(1, tempVector).normalize()
        return tempVector;
    }

    /**
     * Returns z-axis of world coordinate system
     * Hint: third normalized column of world model matrix
     * @return z-axis
     */
    fun getWorldZAxis(): Vector3f {
        val tempVector = Vector3f();
        getWorldModelMatrix().getColumn(2, tempVector).normalize()
        return tempVector;
    }

    /**
     * Returns multiplication of world and object model matrices.
     * Multiplication has to be recursive for all parents.
     * Hint: scene graph
     * @return world modelMatrix
     */
    fun getWorldModelMatrix(): Matrix4f {
        // transformable parent abfragen, wenn ja links multiplikation der partent matrix wenn nein dann selbst world matrix, kein Patent kopie von Modelmatrix
        val world = Matrix4f(modelMatrix)
        if (parent != null) {
            parent!!.getWorldModelMatrix().mul(modelMatrix, world)
        }
        return world
    }

    /**
     * Returns object model matrix
     * @return modelMatrix
     */
    fun getLocalModelMatrix(): Matrix4f {
        return Matrix4f(modelMatrix)
    }
}