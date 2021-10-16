package Parser.ParserToken.Values

import Parser.ParserToken.Type

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
}