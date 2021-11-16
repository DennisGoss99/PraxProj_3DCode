package openGLOutput.exercise.codeObjects

import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.DynamicValue

class Object {

    companion object{

        val file = File(
            "Object",
            hashMapOf("Vector3f" to null),
            hashMapOf(
                "Object" to Declaration.ClassDeclare(
                    "Object",
                    ClassBody(
                        hashMapOf(
                            "Object" to mutableListOf(
                                Declaration.FunctionDeclare(
                                    Type.Void, "Object",
                                    Body(
                                        listOf(
                                            Statement.ProcedureCall(
                                                "_integratedLoadObjectCube",
                                                listOf(Expression.UseVariable("pathToObject")),
                                                null
                                            ),
                                            Statement.AssignValue("path",Expression.UseVariable("pathToObject"))
                                        )
                                    ),
                                    listOf(Parameter("pathToObject", Type.String)),
                                    null
                                ),
                                Declaration.FunctionDeclare(
                                    Type.Void, "Object",
                                    Body(
                                        listOf(
                                            Statement.ProcedureCall(
                                                "_integratedLoadObjectCube",
                                                listOf(Expression.UseVariable("pathToObject")),
                                                null
                                            ),
                                            Statement.AssignValue("path", Expression.UseVariable("pathToObject")),
                                            Statement.AssignValue("position", Expression.UseVariable("tempPosition"))
                                        )
                                    ),
                                    listOf(Parameter("pathToObject", Type.String),Parameter("tempPosition", Type.Custom("Vector3f"))),
                                    null
                                ),
                                Declaration.FunctionDeclare(
                                    Type.Void, "Object",
                                    Body(
                                        listOf(
                                            Statement.ProcedureCall(
                                                "_integratedLoadObjectCube",
                                                listOf(Expression.UseVariable("pathToObject")),
                                                null
                                            ),
                                            Statement.AssignValue("path", Expression.UseVariable("pathToObject")),
                                            Statement.AssignValue("position", Expression.UseVariable("tempPosition")),
                                            Statement.AssignValue("scale", Expression.UseVariable("tempScale"))
                                        )
                                    ),
                                    listOf(Parameter("pathToObject", Type.String),Parameter("tempPosition", Type.Custom("Vector3f")),Parameter("tempScale", Type.Custom("Vector3f"))),
                                    null
                                ),
                            )
                        ),
                        listOf(
                            Declaration.VariableDeclaration( Type.Custom("_Object"),"_object", Expression.Value(ConstantValue.Null()), true),
                            Declaration.VariableDeclaration( Type.String,"path", Expression.Value(ConstantValue.Null())),
                            Declaration.VariableDeclaration( Type.Custom("Vector3f"),"position", Expression.Value(DynamicValue.Class(
                                hashMapOf("x" to Expression.Value(ConstantValue.Float(0f)), "y" to Expression.Value(ConstantValue.Float(0f)), "z" to Expression.Value(ConstantValue.Float(0f)))
                                , Type.Custom("Vector3f")))),
                            Declaration.VariableDeclaration( Type.Custom("Vector3f"),"scale", Expression.Value(DynamicValue.Class(
                                hashMapOf("x" to Expression.Value(ConstantValue.Float(1f)), "y" to Expression.Value(ConstantValue.Float(1f)), "z" to Expression.Value(ConstantValue.Float(1f)))
                                , Type.Custom("Vector3f")))),
                            Declaration.VariableDeclaration( Type.Custom("Vector3f"),"rotation", Expression.Value(DynamicValue.Class(
                                hashMapOf("x" to Expression.Value(ConstantValue.Float(0f)), "y" to Expression.Value(ConstantValue.Float(0f)), "z" to Expression.Value(ConstantValue.Float(0f)))
                                , Type.Custom("Vector3f")))),
                            Declaration.VariableDeclaration( Type.Custom("Vector3f"),"color", Expression.Value(DynamicValue.Class(
                                hashMapOf("x" to Expression.Value(ConstantValue.Float(1f)), "y" to Expression.Value(ConstantValue.Float(1f)), "z" to Expression.Value(ConstantValue.Float(1f)))
                                , Type.Custom("Vector3f"))))
                        )
                    ), null
                )
            ), hashMapOf(), hashMapOf(), hashMapOf()
        )
    }
}