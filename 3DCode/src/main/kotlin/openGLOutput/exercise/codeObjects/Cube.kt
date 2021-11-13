package openGLOutput.exercise.codeObjects

import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue

class Cube {

    companion object{

        val file = File(
            "Cube",
            hashMapOf(),
            hashMapOf(
                "Cube" to Declaration.ClassDeclare(
                    "Cube",
                    ClassBody(
                        hashMapOf(
                            "Cube" to mutableListOf(
                                Declaration.FunctionDeclare(
                                    Type.Void, "Cube",
                                    Body(
                                        listOf(
                                            Statement.ProcedureCall(
                                              "Println", listOf(Expression.Value(ConstantValue.String("SpawnCube"))),null
                                            ),
                                            Statement.ProcedureCall(
                                                "_integratedSpawnCube",
                                                listOf(Expression.UseVariable("arraySize")),
                                                null
                                            ),
                                            Statement.AssignValue("size", Expression.UseVariable("arraySize"))
                                        )
                                    ),
                                    listOf(Parameter("arraySize", Type.Integer)),
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
                                                listOf(Expression.UseVariable("index"), Expression.UseVariable("value")),
                                                null
                                            )
                                        )
                                    ),
                                    listOf(Parameter("index", Type.Integer), Parameter("value", Type.Custom("T"))),
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
                                    listOf(Parameter("index", Type.Integer)),
                                    null
                                )
                            )
                        ),
                        listOf(
                            Declaration.VariableDeclaration( Type.CustomWithGenerics("T", listOf()),"array", Expression.Value(
                                ConstantValue.Null())),
                            Declaration.VariableDeclaration( Type.Integer, "size", Expression.Value(ConstantValue.Integer(0)))
                        )
                    ),
                    listOf("T")
                )
            ), hashMapOf(), hashMapOf(), hashMapOf()
        )
    }
}