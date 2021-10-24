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
                            "Array" to mutableListOf(
                            Declaration.FunctionDeclare(
                                Type.Void, "Array",
                                Body(
                                    listOf(
                                        Statement.ProcedureCall(
                                            "_integratedFunctionInitializeArray",
                                            listOf(Expression.UseVariable("arraySize")),
                                            null
                                        ),
                                        Statement.AssignValue("size", Expression.UseVariable("arraySize"))
                                    )
                                ),
                                listOf(Parameter("arraySize",Type.Integer)),
                                null
                            )
                            ),
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
                            Declaration.VariableDeclaration( Type.CustomWithGenerics("T", listOf()),"array", Expression.Value(ConstantValue.Null())),
                            Declaration.VariableDeclaration( Type.Integer, "size", Expression.Value(ConstantValue.Integer(0)))
                        )
                    ),
                    listOf("T")
                )
            ), hashMapOf(), hashMapOf(), hashMapOf()
        )
    }
}