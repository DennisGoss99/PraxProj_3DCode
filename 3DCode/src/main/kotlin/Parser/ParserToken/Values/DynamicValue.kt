package Parser.ParserToken.Values

import Parser.ParserToken.ClassBody
import Parser.ParserToken.Expression
import Parser.ParserToken.Type
import openGLOutput.exercise.components.geometry.mesh.RenderableBase

sealed class DynamicValue : IValue  {

    override fun toString(): kotlin.String
    {
        return this.javaClass.simpleName
    }

    data class Class(override val value : HashMap<String, Expression.Value>, val type: Type.Custom) : DynamicValue(){

        override fun toString(): kotlin.String
        {
            return "$value : $type"
        }

        override fun getValueAsString() : kotlin.String {
            return value.toString()
        }

        override fun getType(): Type = type
    }

    data class Array(override val value : kotlin.Array<Expression.Value>, val type: Type.CustomWithGenerics) : DynamicValue(){
        override fun getValueAsString(): String {
            return "[" + value.fold(""){acc, value ->  acc + value.value.getValueAsString() } + "]"
        }

        override fun getType(): Type = type

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Array

            if (!value.contentEquals(other.value)) return false
            if (type != other.type) return false

            return true
        }

        override fun hashCode(): Int {
            var result = value.contentHashCode()
            result = 31 * result + type.hashCode()
            return result
        }
    }

    data class Object(override val value : RenderableBase, val type: Type.Custom) : DynamicValue(){

        override fun toString(): kotlin.String
        {
            return "$value : $type"
        }

        override fun getValueAsString() : kotlin.String {
            return value.toString()
        }

        override fun getType(): Type = type
    }
}