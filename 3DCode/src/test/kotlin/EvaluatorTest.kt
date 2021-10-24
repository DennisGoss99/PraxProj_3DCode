import Evaluator.Evaluator
import Evaluator.Exceptions.NotFound.VariableNotFoundRuntimeException
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EvaluatorTest{

    @Test
    fun simpleMainTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(Declaration.FunctionDeclare
                (
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>
                        (
                        Statement.AssignValue("return",Expression.Value(ConstantValue.Integer(5)))
                    )
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(5)),evaluator.eval(declarations,null))

    }

    @Test
    fun simpleAdditionMainTest(){

         val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(Declaration.FunctionDeclare
                (
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.Operation(
                                Operator.Plus,
                                Expression.Value(ConstantValue.Integer(5)),
                                Expression.Value(ConstantValue.Integer(5))
                                )
                        )
                    )
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(10)),evaluator.eval(declarations,null))

    }

    @Test
    fun globalVariableTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue("return",Expression.UseVariable("a")))
                ),
                null, null
            ))
            ), hashMapOf(
                "a" to Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(ConstantValue.Integer(5))),
            ), hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(5)),evaluator.eval(declarations,null))

    }

    @Test
    fun sameVariableNameTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue("return",Expression.UseVariable("a"))),
                    listOf(Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(
                        ConstantValue.Integer(15))))
                ),
                null, null
            ))
            ), hashMapOf("a" to Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(ConstantValue.Integer(5)))),
            hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(15)),evaluator.eval(declarations,null))

    }

    @Test
    fun variableAdditionTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.Operation(
                                Operator.Plus,
                                Expression.UseVariable("a"),
                                Expression.UseVariable("b")
                            )
                        )
                    ),
                    listOf(Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(
                        ConstantValue.Integer(15))))
                ),
                null, null
            ))
            ), hashMapOf("b" to Declaration.VariableDeclaration(Type.Integer,"b",Expression.Value(ConstantValue.Integer(5)))),
            hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(20)),evaluator.eval(declarations,null))

    }

    @Test
    fun reassignVariableAdditionTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "a",
                            Expression.Value(ConstantValue.Integer(20))
                        ),
                        Statement.AssignValue(
                            "return",
                            Expression.UseVariable("a")
                        )
                    ),
                    listOf(Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(
                        ConstantValue.Integer(15))))
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(20)),evaluator.eval(declarations,null))

    }

    @Test
    fun simpleBlockTest(){
        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf(
                        Statement.AssignValue(
                            "a",
                            Expression.Value(ConstantValue.Integer(20))
                        ),
                        Statement.Block(
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue(
                                        "return",
                                        Expression.UseVariable("a")
                                    )
                                )
                            )
                        )

                    ),
                    listOf(Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(
                        ConstantValue.Integer(15))))
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(20)),evaluator.eval(declarations,null))
    }

    @Test
    fun variableShadowingTest(){
        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf(
                        Statement.Block(
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue(
                                        "a",
                                        Expression.Value(ConstantValue.Integer(2))
                                    )
                                ),
                                listOf(Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(
                                    ConstantValue.Integer(3))))
                            )
                        ),
                        Statement.AssignValue(
                            "return",
                            Expression.UseVariable("a")
                        )
                    ),
                    listOf(Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(
                        ConstantValue.Integer(1))))
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(1)),evaluator.eval(declarations,null))
    }

    @Test
    fun block2Test(){
        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf(
                        Statement.AssignValue(
                            "a",
                            Expression.Value(ConstantValue.Integer(20))
                        ),
                        Statement.Block(
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue(
                                        "a",
                                        Expression.Value(ConstantValue.Integer(10))
                                    )
                                )
                            )
                        ),
                        Statement.AssignValue(
                            "return",
                            Expression.UseVariable("a")
                        )

                    ),
                    listOf(Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(
                        ConstantValue.Integer(15))))
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(10)),evaluator.eval(declarations,null))
    }


    @Test
    fun blockExternalVariableTest(){
        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf(
                        Statement.Block(
                            Body(
                                listOf(),
                                listOf(
                                    Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(ConstantValue.Integer(0)))
                                )
                            )
                        ),
                        Statement.AssignValue(
                            "return",
                            Expression.UseVariable("a")
                        )
                    ),null
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()

        assertFailsWith<VariableNotFoundRuntimeException>(block = {evaluator.eval(declarations,null)})
    }

    @Test
    fun functionExternalVariableTest(){
        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "A",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.UseVariable("a")
                        )
                    )
                ),
                null, null
            ),
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.FunctionCall(
                                "A",
                                null, null
                            )
                        )
                    ),
                    listOf(
                        Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(ConstantValue.Integer(0)))
                    )
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()

        assertFailsWith<VariableNotFoundRuntimeException>(block = {evaluator.eval(declarations,null)})
    }

    @Test
    fun simpleIfTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf(
                        Statement.If(
                            Expression.Value(ConstantValue.Boolean(true)),
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue("return",Expression.Value(ConstantValue.Integer(10)))
                                )
                            ),
                            null
                        ),
                        Statement.AssignValue("return",Expression.Value(ConstantValue.Integer(5)))
                    ),
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val declarations2 = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf(
                        Statement.If(
                            Expression.Value(ConstantValue.Boolean(false)),
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue("return",Expression.Value(ConstantValue.Integer(10)))
                                )
                            ),
                            null
                        ),
                        Statement.AssignValue("return",Expression.Value(ConstantValue.Integer(5)))
                    ),
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())


        val evaluator = Evaluator()
        assertEquals(Expression.Value(ConstantValue.Integer(10)),evaluator.eval(declarations,null))

        val evaluator2 = Evaluator()
        assertEquals(Expression.Value(ConstantValue.Integer(5)),evaluator2.eval(declarations2,null))
    }

    @Test
    fun ifTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Boolean,
                "Main",
                Body(
                    listOf(
                        Statement.AssignValue(
                            "cc",
                            Expression.Operation(Operator.Greater, Expression.Operation(Operator.Multiply,Expression.UseVariable("a"),Expression.Value(
                                ConstantValue.Integer(5))), Expression.UseVariable("b"))
                        ),
                        Statement.If(
                            Expression.UseVariable("cc"),
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue("return",Expression.Value(ConstantValue.Boolean(true)))
                                )
                            ),
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue("return",Expression.Value(ConstantValue.Boolean(false)))
                                )
                            )
                        ),
                        Statement.AssignValue("return",Expression.Value(ConstantValue.Integer(5)))
                    ),
                    listOf(
                        Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(ConstantValue.Integer(5))),
                        Declaration.VariableDeclaration(Type.Integer,"b",Expression.Value(ConstantValue.Integer(24))),
                        Declaration.VariableDeclaration(Type.Integer,"cc",Expression.Value(ConstantValue.Boolean(false))),
                    )
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()
        assertEquals(Expression.Value(ConstantValue.Boolean(true)),evaluator.eval(declarations,null))

    }

    @Test
    fun noParameterFunctionTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "A",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue("return",Expression.Value(ConstantValue.Integer(5))))
                ),
                null, null
            ),
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue("return",Expression.FunctionCall("A",null, null)))
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(5)),evaluator.eval(declarations,null))

    }

    @Test
    fun parameterFunctionTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf(
                "A" to mutableListOf( Declaration.FunctionDeclare(
                        Type.Integer,
                        "A",
                        Body(
                            listOf<Statement>(
                                Statement.AssignValue("return",Expression.UseVariable("hallo")))
                        ),
                        listOf(Parameter("hallo",Type.Integer)), null
                    )),
                "Main" to mutableListOf( Declaration.FunctionDeclare(
                    Type.Integer,
                    "Main",
                    Body(
                        listOf<Statement>(
                            Statement.AssignValue("return",Expression.FunctionCall("A", listOf(Expression.Value(
                                ConstantValue.Integer(5))), null)))
                    ),
                null, null))
            ), hashMapOf(), hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(5)),evaluator.eval(declarations,null))

    }

    @Test
    fun advancedParameterFunctionTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("A" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "A",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue("return",
                            Expression.Operation(
                            Operator.Multiply,
                            Expression.Value(ConstantValue.Integer(5)),
                            Expression.UseVariable("hallo"))
                    )
                    )
                ),
                listOf(Parameter("hallo",Type.Integer)), null
            )),"Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.FunctionCall(
                                "A",
                                listOf(
                                    Expression.FunctionCall(
                                        "A",
                                        listOf(Expression.Value(
                                            ConstantValue.Integer(
                                            5
                                        ))), null
                                    )
                                ), null
                            )
                        )
                    )
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(125)),evaluator.eval(declarations,null))

    }

    @Test
    fun parameterProcedureTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("A" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Void,
                "A",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue("a",Expression.Value(ConstantValue.Integer(10))))
                ),
                null, null
            )),"Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf(
                        Statement.ProcedureCall("A",null, null),
                        Statement.AssignValue("return",Expression.UseVariable("a"))
                    )
                ),
                null, null
            ))
            ), hashMapOf("a" to Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(ConstantValue.Integer(5)))),
            hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(10)),evaluator.eval(declarations,null))

    }

    @Test
    fun whileTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Boolean,
                "Main",
                Body(
                    listOf(
                        Statement.While(
                            Expression.Operation(Operator.Less,Expression.UseVariable("a"),Expression.Value(
                                ConstantValue.Integer(100))),
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue("a",Expression.Operation(Operator.Multiply,Expression.UseVariable("a"),Expression.UseVariable("a")))
                                )
                            )
                        ),
                        Statement.AssignValue("return",Expression.UseVariable("a"))
                    ),
                    listOf(
                        Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(ConstantValue.Integer(2))),
                    )
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()
        assertEquals(Expression.Value(ConstantValue.Integer(256)),evaluator.eval(declarations,null))

    }

    @Test
    fun fibonacciFunctionTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("F" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "F",
                Body(
                    listOf<Statement>(
                        Statement.If(
                            Expression.Operation(
                                Operator.Or,
                                Expression.Operation(Operator.DoubleEquals, Expression.UseVariable("n"),Expression.Value(
                                    ConstantValue.Integer(0))),
                                Expression.Operation(Operator.DoubleEquals, Expression.UseVariable("n"),Expression.Value(
                                    ConstantValue.Integer(1))),
                            ),
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue(
                                        "return",
                                        Expression.UseVariable("n")
                                    )
                                )
                            ),
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue(
                                        "return",
                                        Expression.Operation(
                                            Operator.Plus,
                                            Expression.FunctionCall("F", listOf(
                                                Expression.Operation(
                                                    Operator.Minus,
                                                    Expression.UseVariable("n"),
                                                    Expression.Value(ConstantValue.Integer(1))
                                                )
                                            ), null),
                                            Expression.FunctionCall("F", listOf(
                                                Expression.Operation(
                                                    Operator.Minus,
                                                    Expression.UseVariable("n"),
                                                    Expression.Value(ConstantValue.Integer(2))
                                                )
                                            ), null)
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                listOf(Parameter("n",Type.Integer)), null
            )),"Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.FunctionCall(
                                "F",
                                listOf(Expression.Value(ConstantValue.Integer(9))), null
                            )
                        )
                    )
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(34)),evaluator.eval(declarations,null))
    }

    @Test
    fun fibonacciFunctionTest2(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("F" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "F",
                Body(
                    listOf<Statement>(
                        Statement.If(
                            Expression.Operation(
                                Operator.DoubleEquals,
                                Expression.UseVariable("n"),
                                Expression.Value(ConstantValue.Integer(0))
                            ),
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue(
                                        "return",
                                        Expression.UseVariable("n")
                                    )
                                )
                            ),
                            Body(
                                listOf(
                                    Statement.If(
                                        Expression.Operation(
                                            Operator.DoubleEquals,
                                            Expression.UseVariable("n"),
                                            Expression.Value(ConstantValue.Integer(1))
                                        ),
                                        Body(
                                            listOf<Statement>(
                                                Statement.AssignValue(
                                                    "return",
                                                    Expression.UseVariable("n")
                                                )
                                            )
                                        ),
                                        Body(listOf<Statement>(
                                            Statement.AssignValue(
                                                "return",
                                                Expression.Operation(
                                                    Operator.Plus,
                                                    Expression.FunctionCall("F", listOf(
                                                        Expression.Operation(
                                                            Operator.Minus,
                                                            Expression.UseVariable("n"),
                                                            Expression.Value(ConstantValue.Integer(1))
                                                        )
                                                    ), null),
                                                    Expression.FunctionCall("F", listOf(
                                                        Expression.Operation(
                                                            Operator.Minus,
                                                            Expression.UseVariable("n"),
                                                            Expression.Value(ConstantValue.Integer(2))
                                                        )
                                                    ), null)
                                                )
                                            )
                                        ))
                                    )
                                )
                            )
                        )
                    )
                ),
                listOf(Parameter("n",Type.Integer)), null
            )),
                "Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.FunctionCall(
                                "F",
                                listOf(Expression.Value(ConstantValue.Integer(9))),
                                null
                            )
                        )
                    )
                ),
                null, null
            ))), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(34)),evaluator.eval(declarations,null))
    }

    @Test
    fun fibonacciLoopTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Main",
                Body(
                    listOf(
                        Statement.While(
                            Expression.Operation(Operator.Greater, Expression.UseVariable("n"), Expression.Value(
                                ConstantValue.Integer(0))),
                            Body(
                                listOf<Statement>(
                                    Statement.AssignValue("f",Expression.Operation(Operator.Plus,Expression.UseVariable("f1"),Expression.UseVariable("f2"))),
                                    Statement.AssignValue("f1",Expression.UseVariable("f2")),
                                    Statement.AssignValue("f2",Expression.UseVariable("f")),
                                    Statement.AssignValue("n",Expression.Operation(Operator.Minus,Expression.UseVariable("n"),Expression.Value(
                                        ConstantValue.Integer(1)))),
                            )
                            )
                        ),
                        Statement.AssignValue(
                            "return",
                            Expression.UseVariable("f")
                        )
                    )
                ),
                null, null
            ))
            ), hashMapOf(
                "n" to Declaration.VariableDeclaration(Type.Integer,"n",Expression.Value(ConstantValue.Integer(10))),
                "f" to Declaration.VariableDeclaration(Type.Integer,"f",Expression.Value(ConstantValue.Integer(0))),
                "f1" to Declaration.VariableDeclaration(Type.Integer,"f1",Expression.Value(ConstantValue.Integer(-1))),
                "f2" to Declaration.VariableDeclaration(Type.Integer,"f2",Expression.Value(ConstantValue.Integer(1))),
            ),
            hashMapOf())

        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(34)),evaluator.eval(declarations,null))
    }

    @Test
    fun mathMainTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare
                (
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.Operation(
                                Operator.Minus,
                                Expression.Operation(
                                    Operator.Multiply,
                                    Expression.Value(ConstantValue.Integer(4)),
                                    Expression.Operation(
                                        Operator.Plus,
                                        Expression.Operation(
                                            Operator.Multiply,
                                            Expression.Value(ConstantValue.Integer(3)),
                                            Expression.Value(ConstantValue.Integer(4))
                                        ),
                                        Expression.Value(ConstantValue.Integer(4))
                                    )
                                ),
                                Expression.Operation(
                                    Operator.Minus,
                                    Expression.Operation(
                                        Operator.Multiply,
                                        Expression.Value(ConstantValue.Integer(4)),
                                        Expression.Value(ConstantValue.Integer(5))
                                    ),
                                    Expression.Value(ConstantValue.Integer(20))
                                )
                            )
                        )
                    )
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Integer(64)),evaluator.eval(declarations,null))
    }

    @Test
    fun boolTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare
                (
                Type.Boolean,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.Operation(
                                Operator.Or,
                                Expression.Operation(
                                    Operator.Not,
                                    Expression.Operation(
                                        Operator.And,
                                        Expression.Value(ConstantValue.Boolean(true)),
                                        Expression.Value(ConstantValue.Boolean(false))
                                    )
                                    ,
                                    null
                                ),
                                Expression.Value(ConstantValue.Boolean(false))
                            )
                        )
                    )
                ),
                null, null
               ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Boolean(true)),evaluator.eval(declarations,null))

    }

    @Test
    fun stringTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare
                (
                Type.Integer,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.Operation(
                                Operator.Plus,
                                Expression.Value(ConstantValue.String("TEST:")),
                                Expression.Value(ConstantValue.String("OK"))
                            )
                        )
                    )
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())
        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.String("TEST:OK")),evaluator.eval(declarations,null))

    }

    @Test
    fun floatAdditionTest(){

        val declarations = File("demo", hashMapOf(),hashMapOf(),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Float,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "return",
                            Expression.Operation(
                                Operator.Plus,
                                Expression.UseVariable("a"),
                                Expression.UseVariable("b")
                            )
                        )
                    ),
                    listOf(Declaration.VariableDeclaration(Type.Float,"a",Expression.Value(
                        ConstantValue.Float(15.7f))))
                ),
                null, null
            ))
            ), hashMapOf("b" to Declaration.VariableDeclaration(Type.Float,"b",Expression.Value(ConstantValue.Float(5.5f)))),
            hashMapOf())


        val evaluator = Evaluator()

        assertEquals(Expression.Value(ConstantValue.Float(21.2f)),evaluator.eval(declarations,null))

    }

    @Test
    fun classTest(){

        val declarations = File("demo", hashMapOf(),
            hashMapOf(
                "OpenGL" to Declaration.ClassDeclare("OpenGL", ClassBody(
                    hashMapOf( "OpenGL" to mutableListOf(
                        Declaration.FunctionDeclare(Type.Void,"OpenGL",
                            Body(listOf(
                            )),null, null)),
                        "A" to mutableListOf(
                            Declaration.FunctionDeclare(Type.Void,"A",
                                Body(
                                    listOf<Statement>(
                                        Statement.ProcedureCall(
                                            "Println",
                                            listOf<Expression>(
                                                Expression.Value(ConstantValue.String("Hallo"))
                                            ), null
                                        )
                                    )
                                ),null, null)),
                        "B" to mutableListOf(Declaration.FunctionDeclare(Type.Void,"B",
                            Body(
                                listOf<Statement>(
                                    Statement.ProcedureCall(
                                        "Println",
                                        listOf<Expression>(
                                            Expression.Operation(Operator.Plus,
                                                Expression.FunctionCall("ToString", listOf(Expression.UseVariable("a")), null),
                                                Expression.Operation(Operator.Plus,
                                                    Expression.Value(ConstantValue.String("Hallo")),
                                                    Expression.UseVariable("name")
                                                ),
                                            )
                                        ), null
                                    )
                                )
                            ), listOf(Parameter("a",Type.Integer)), null))
                    ),
                    listOf(
                        Declaration.VariableDeclaration(Type.String,"name",Expression.Value(ConstantValue.String("")))
                    )
                ), null)
            ),
            hashMapOf("Main" to mutableListOf(
            Declaration.FunctionDeclare(
                Type.Void,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.UseClass("b", Statement.ProcedureCall("B", listOf(Expression.Value(ConstantValue.Integer(5))), null))
                    ),
                    listOf(
                        Declaration.VariableDeclaration(Type.Custom("openGl"),"b",Expression.FunctionCall("OpenGL",null, null)),
                        Declaration.VariableDeclaration(Type.String,"a", Expression.UseDotVariable("b",Expression.UseVariable("name"))),
                    )
                ),
                null, null
            ))
            ), hashMapOf(), hashMapOf())

        val evaluator = Evaluator()

        evaluator.eval(declarations,null)

    }

}