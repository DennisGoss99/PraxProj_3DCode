package Parser.ParserToken.Values

import Parser.ParserToken.Type
import kotlin.jvm.internal.Intrinsics

sealed class ConstantValue : IValue {

    override fun toString(): kotlin.String
    {
        return this.javaClass.simpleName
    }

    data class Boolean(override val value : kotlin.Boolean, val type: Type.Boolean = Type.Boolean) : ConstantValue()
    {
        override fun toString(): kotlin.String
        {
            return "$value : $type"
        }

        override fun getValueAsString() : kotlin.String {
            return value.toString()
        }

        override fun getType(): Type = type
    }

    data class Char(override val value : kotlin.Char, val type: Type.Char = Type.Char) : ConstantValue()
    {
        override fun toString(): kotlin.String
        {
            return "$value : $type"
        }

        override fun getValueAsString() : kotlin.String {
            return value.toString()
        }

        override fun getType(): Type = type
    }

    data class Integer(override val value : Int, val type: Type.Integer = Type.Integer) : ConstantValue()
    {
        override fun toString(): kotlin.String
        {
            return "$value : $type"
        }

        override fun getValueAsString() : kotlin.String {
            return value.toString()
        }

        override fun getType(): Type = type
    }

    data class String(override val value : kotlin.String, val type: Type.String = Type.String) : ConstantValue()
    {
        override fun toString(): kotlin.String
        {
            return "$value : $type"
        }

        override fun getValueAsString() : kotlin.String {
            return value.toString()
        }

        override fun getType(): Type = type
    }

    data class Float(override val value : kotlin.Float, val type: Type.Float = Type.Float) : ConstantValue()
    {
        override fun toString(): kotlin.String
        {
            return "$value : $type"
        }

        override fun getValueAsString() : kotlin.String {
            return value.toString()
        }

        override fun getType(): Type = type
    }

    data class Null(val type: Type.Null = Type.Null ): ConstantValue()
    {
        override val value: Nothing
            get() {
                throw Exception("Can't get Value of Type <Null>")
            }

        override fun toString(): kotlin.String
        {
            return "null"
        }

        override fun getValueAsString() : kotlin.String {
            return "null"
        }

        override fun getType(): Type = type
    }

}