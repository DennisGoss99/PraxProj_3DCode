package Parser

import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.DynamicValue

class ArrayImplementation {
    companion object{

        val file = File(
            "Array",
            hashMapOf(),
            hashMapOf(
                "Array" to Declaration.ClassDeclare(
                    "Array",
                    ClassBody(
                        hashMapOf(
                            "Set" to mutableListOf(
                                Declaration.FunctionDeclare(
                                    Type.Void, "Set",
                                    Body(
                                        listOf(
                                            Statement.ProcedureCall(
                                                "_integratedFunctionSetArray",
                                                listOf(Expression.UseVariable("index"),Expression.UseVariable("value")),
                                                null
                                            )
                                        )
                                    ),
                                    listOf(Parameter("index",Type.Integer),Parameter("value",Type.Custom("T"))),
                                    null
                                )
                            ),
                            "Get" to mutableListOf(
                                Declaration.FunctionDeclare(
                                    Type.Custom("T"), "Get",
                                    Body(
                                        listOf(
                                            Statement.AssignValue(
                                                "return",
                                                Expression.FunctionCall(
                                                    "_integratedFunctionGetArray",
                                                    listOf(Expression.UseVariable("index")),
                                                    null
                                                )
                                            )
                                        )
                                    ),
                                    listOf(Parameter("index",Type.Integer)),
                                    null
                                )
                            )
                        ),
                        listOf(
                            Declaration.VariableDeclaration(
                                Type.CustomWithGenerics("T", listOf()),"array", Expression.Value(ConstantValue.Null())
                            )
                        )
                    ),
                    listOf("T")
                )
            ), hashMapOf(), hashMapOf(), hashMapOf()
        )
    }
}