package Parser.ParserToken

import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.IValue

sealed class Expression : ILineOfCode
{
    // Used for parser internaly
    public var BlockDepth = -1

    override fun toString(): kotlin.String
    {
        return this.javaClass.simpleName
    }

    // Operator is a value
    data class Operation(val operator: Operator, val expressionA: Expression, val expressionB: Expression?, override val LineOfCode: Int = -1) : Expression()

    data class UseVariable(val variableName : String, override val LineOfCode: Int = -1) : Expression()

    data class UseDotVariable(val variableName : String, var expression: Expression, override val LineOfCode: Int = -1) : Expression()

    data class FunctionCall(val functionName : String, var parameterList : List<Expression>?, val generics : List<Type>?, override val LineOfCode: Int = -1) : Expression()

    data class Value(var value: IValue, override val LineOfCode: Int = -1) : Expression()
}