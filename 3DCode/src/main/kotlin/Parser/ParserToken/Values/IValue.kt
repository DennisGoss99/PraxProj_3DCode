package Parser.ParserToken.Values

import Parser.ParserToken.Type

interface IValue {
    val value: Any
    override fun toString(): String
    fun getValueAsString(): String
    fun getType(): Type
}