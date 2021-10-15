package Parser.ParserToken.Values

import Parser.ParserToken.ClassBody
import Parser.ParserToken.Expression
import Parser.ParserToken.Type

sealed class DynamicValue : IValue  {

    override fun toString(): kotlin.String
    {
        return this.javaClass.simpleName
    }

    data class Class(override val value : HashMap<String, Expression.Value>, val type: Type.Custom) : DynamicValue()
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