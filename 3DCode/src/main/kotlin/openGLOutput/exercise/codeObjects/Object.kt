package openGLOutput.exercise.codeObjects

import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue

class Object {

    companion object{

        val file = File(
            "Object",
            hashMapOf(),
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
                                                "Println", listOf(Expression.Value(ConstantValue.String("SpawnObject"))),null
                                            ),
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
                                )
                            )
                        ),
                        listOf(
                            Declaration.VariableDeclaration( Type.Custom("_Object"),"_object", Expression.Value(ConstantValue.Null())),
                            Declaration.VariableDeclaration( Type.String,"path", Expression.Value(ConstantValue.Null()))
                        )
                    ), null
                )
            ), hashMapOf(), hashMapOf(), hashMapOf()
        )
    }
}