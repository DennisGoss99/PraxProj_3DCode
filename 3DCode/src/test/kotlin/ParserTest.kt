import Lexer.TestLexer
import Parser.*
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParserTest
{
    private val _debugOutPut = false

    fun CallMain(statementList: List<Statement>, localVariables: List<Declaration.VariableDeclaration>? = null, parameters: List<Parameter>? = null, returnType : Type = Type.Integer): List<Declaration>
    {
        return listOf<Declaration>(Declaration.FunctionDeclare(
            returnType,
            "Main",
            Body(statementList, localVariables),
            parameters
        ))
    }

    fun TestIfTreeIsAsExpected(code : String, declaration: List<Declaration>)
    {
        val lexer = TestLexer(code)
        val parser = Parser(lexer)
        val parserTokenTree = parser.ParsingStart()

        assertEquals(declaration.toString(), parserTokenTree.toString())
    }


    @Test
    fun ReturnDirectTest()
    {
        val code = """
            Int Main()
            {
                return 5
            }
        """.trimIndent()

        val statementList = listOf<Statement>(
            Statement.AssignValue("return", Expression.Value(ConstantValue.Integer(5)))
        )

        val tree = CallMain(statementList,null, null)

        TestIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun ReturnWithAdditionTest()
    {
        val code = """
            Int Main()
            {
                return 5 + 5
            }
        """.trimIndent()

        val statementList = listOf<Statement>(
            Statement.AssignValue(
                "return",
                Expression.Operation(
                    Operator.Plus,
                    Expression.Value(ConstantValue.Integer(5)),
                    Expression.Value(ConstantValue.Integer(5))
                ))
        )

        val tree = CallMain(statementList, null, null)

        TestIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun ReturnWithDeclarationTest()
    {
        val code = """
            Int Main()
            {
                Int a = 0
                
                return a
            }
        """.trimIndent()

        val localVariables = listOf<Declaration.VariableDeclaration>(
            Declaration.VariableDeclaration(Type.Integer, "a", Expression.Value(ConstantValue.Integer(0)))
        )

        val statementList = listOf<Statement>(
            Statement.AssignValue(
                "return",
                Expression.UseVariable("a")
            )
        )

        val tree = CallMain(statementList, localVariables, null)

        TestIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun ReturnWithLoopTest()
    {
        val code = """
            Int Main()
            {
                Int a = 1
                
                while(a == 14)
                {
                    a = a + 1
                }
                
                return a
            }
        """.trimIndent()

        val localVariables = listOf<Declaration.VariableDeclaration>(
            Declaration.VariableDeclaration(Type.Integer, "a", Expression.Value(ConstantValue.Integer(1)))
        )

        val statementList = listOf<Statement>(
            Statement.While(
                Expression.Operation(
                    Operator.DoubleEquals,
                    Expression.UseVariable("a"),
                    Expression.Value(ConstantValue.Integer(14))
                ),
                Body(
                    listOf<Statement>(
                    Statement.AssignValue(
                        "a",
                        Expression.Operation(
                            Operator.Plus,
                            Expression.UseVariable("a"),
                            Expression.Value(ConstantValue.Integer(1))
                        )
                    )
                 )
                )
            ),
            Statement.AssignValue(
                "return",
                Expression.UseVariable("a")
            )
        )

        val tree = CallMain(statementList, localVariables, null)

        TestIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun ReturnWithParametersTest()
    {
        val code = """
            Int Main(Int a, Int b)
            {
                return a * b
            }
        """.trimIndent()

        val parameters = listOf<Parameter>(
            Parameter("a", Type.Integer),
            Parameter("b", Type.Integer)
        )

        val statementList = listOf<Statement>(
            Statement.AssignValue(
                "return",
                Expression.Operation(
                    Operator.Multiply,
                    Expression.UseVariable("a"),
                    Expression.UseVariable("b")
                )
            )
        )

        val tree = CallMain(statementList,null, parameters)

        TestIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun ReturnWithFuncitonCallTest()
    {
        val code = """
            Int Main()
            {
                return 2 + A(3,5)
            }
        """.trimIndent()

        val statementList = listOf<Statement>(
            Statement.AssignValue(
                "return",
                Expression.Operation(
                    Operator.Plus,
                    Expression.Value(ConstantValue.Integer(2)),
                    Expression.FunctionCall(
                        "A",
                        listOf<Expression>(
                            Expression.Value(ConstantValue.Integer(3)),
                            Expression.Value(ConstantValue.Integer(5))
                        )
                    )
                )
            )
        )

        val tree = CallMain(statementList, null, null)

        TestIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun ReturnWithIFTest()
    {
        val code = """
            Int Main()
            {
                Int w = 3
                Bool f = w <= 3
                
                if(f)
                {
                    return 1
                }
            
                return 0
            }
        """.trimIndent()

        val localVariables = listOf<Declaration.VariableDeclaration>(
            Declaration.VariableDeclaration(Type.Integer, "w", Expression.Value(ConstantValue.Integer(3))),
            Declaration.VariableDeclaration(Type.Boolean, "f", Expression.Operation(Operator.LessEqual, Expression.UseVariable("w"), Expression.Value(
                ConstantValue.Integer(3))))
        )

        val statementList = listOf<Statement>(
            Statement.If(
                Expression.UseVariable("f"),
                Body(listOf<Statement>(
                    Statement.AssignValue(
                        "return",
                        Expression.Value(ConstantValue.Integer(1))
                    )
                )),
                null
            ),
            Statement.AssignValue(
                "return",
                Expression.Value(ConstantValue.Integer(0))
            )

        )

        val tree = CallMain(statementList, localVariables, null)

        TestIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun FloatReturnWithLoopTest()
    {
        val code = """
            Float Main()
            {
                Float a = 1.0
                
                while(a == 14.0)
                {
                    a = a + 1.0
                }
                
                return a
            }
        """.trimIndent()

        val localVariables = listOf<Declaration.VariableDeclaration>(
            Declaration.VariableDeclaration(Type.Float, "a", Expression.Value(ConstantValue.Float(1.0f)))
        )

        val statementList = listOf<Statement>(
            Statement.While(
                Expression.Operation(
                    Operator.DoubleEquals,
                    Expression.UseVariable("a"),
                    Expression.Value(ConstantValue.Float(14.0f))
                ),
                Body(
                    listOf<Statement>(
                        Statement.AssignValue(
                            "a",
                            Expression.Operation(
                                Operator.Plus,
                                Expression.UseVariable("a"),
                                Expression.Value(ConstantValue.Float(1.0f))
                            )
                        )
                    )
                )
            ),
            Statement.AssignValue(
                "return",
                Expression.UseVariable("a")
            )
        )

        val tree = CallMain(statementList, localVariables, null, Type.Float)

        TestIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun classTest(){
        val code = """   
            
            class OpenGL{           
                String name = ""
                
                Void A(){
                    Println("Hallo")
                }
            }
                             
            Void Main(){
                OpenGL b = OpenGL()
            }
            
        """.trimIndent()

        val declarations = listOf<Declaration>(
            Declaration.ClassDeclare("OpenGL", ClassBody(
                hashMapOf(
                    "A" to mutableListOf(Declaration.FunctionDeclare(Type.Void,"A",
                        Body(
                            listOf<Statement>(
                                Statement.ProcedureCall(
                                    "Println",
                                    listOf<Expression>(
                                        Expression.Value(ConstantValue.String("Hallo"))
                                    )
                                )
                            )
                        ),null))),
                listOf(
                    Declaration.VariableDeclaration(Type.String,"name",Expression.Value(ConstantValue.String("")))
                )
            )),
            Declaration.FunctionDeclare(
                Type.Void,
                "Main",
                Body(
                    listOf<Statement>(),
                    listOf<Declaration.VariableDeclaration>(Declaration.VariableDeclaration(Type.Custom("OpenGL"),"b",Expression.FunctionCall("OpenGL",null)))
                ),
                null
            )
        )

        TestIfTreeIsAsExpected(code, declarations)
    }

    @Test
    fun class2Test(){
        val code = """   
            
            class OpenGL{           
                String name = ""
                
                Void A(){
                    Println("Hallo")
                }
            }
            
            class Aaaa{           
                String a = ""
                Float b = 0.0
            }
                             
            Void Main(){
                OpenGL b = OpenGL()
            }
            
        """.trimIndent()

        val declarations = listOf<Declaration>(
            Declaration.ClassDeclare("OpenGL", ClassBody(
                hashMapOf(
                    "A" to mutableListOf(Declaration.FunctionDeclare(Type.Void,"A",
                        Body(
                            listOf<Statement>(
                                Statement.ProcedureCall(
                                    "Println",
                                    listOf<Expression>(
                                        Expression.Value(ConstantValue.String("Hallo"))
                                    )
                                )
                            )
                        ),null))),
                listOf(
                    Declaration.VariableDeclaration(Type.String,"name",Expression.Value(ConstantValue.String("")))
                )
            )),
            Declaration.ClassDeclare("Aaaa", ClassBody(
                hashMapOf(),
                listOf(
                    Declaration.VariableDeclaration(Type.String,"a",Expression.Value(ConstantValue.String(""))),
                    Declaration.VariableDeclaration(Type.Float,"b",Expression.Value(ConstantValue.Float(0.0f)))
                )
            )),
            Declaration.FunctionDeclare(
                Type.Void,
                "Main",
                Body(
                    listOf<Statement>(),
                    listOf<Declaration.VariableDeclaration>(Declaration.VariableDeclaration(Type.Custom("OpenGL"),"b",Expression.FunctionCall("OpenGL",null)))
                ),
                null
            )
        )

        TestIfTreeIsAsExpected(code, declarations)
    }

    @Test
    fun class3Test() {
        val code = """   
            
            class OpenGL{           
                String name = ""
            
                Void A(){
                    Println("Hallo")
                }
                
                Void B(Int a){
                    Println(ToString(a) + "Hallo" + name)
                }
            }
                             
            Void Main(){
                OpenGL b = OpenGL()
                String a = b.name
                
                b.B(5)
            }
            
        """.trimIndent()

        val declarations = listOf<Declaration>(
            Declaration.ClassDeclare("OpenGL", ClassBody(
                hashMapOf(
                    "A" to mutableListOf(Declaration.FunctionDeclare(Type.Void,"A",
                        Body(
                            listOf<Statement>(
                                Statement.ProcedureCall(
                                    "Println",
                                    listOf<Expression>(
                                        Expression.Value(ConstantValue.String("Hallo"))
                                    )
                                )
                            )
                        ),null)),
                    "B" to mutableListOf(Declaration.FunctionDeclare(Type.Void,"B",
                    Body(
                        listOf<Statement>(
                            Statement.ProcedureCall(
                                "Println",
                                listOf<Expression>(
                                    Expression.Operation(Operator.Plus,
                                        Expression.FunctionCall("ToString", listOf(Expression.UseVariable("a"))),
                                        Expression.Operation(Operator.Plus,
                                            Expression.Value(ConstantValue.String("Hallo")),
                                            Expression.UseVariable("name")
                                        ),
                                    )
                                )
                            )
                        )
                    ), listOf(Parameter("a",Type.Integer))))
                ),
                listOf(
                    Declaration.VariableDeclaration(Type.String,"name",Expression.Value(ConstantValue.String("")))
                )
            )),
            Declaration.FunctionDeclare(
                Type.Void,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.UseClass("b", Statement.ProcedureCall("B", listOf(Expression.Value(ConstantValue.Integer(5)))))
                    ),
                    listOf<Declaration.VariableDeclaration>(
                        Declaration.VariableDeclaration(Type.Custom("OpenGL"),"b",Expression.FunctionCall("OpenGL",null)),
                        Declaration.VariableDeclaration(Type.String,"a", Expression.UseDotVariable("b",Expression.UseVariable("name"))),
                    )
                ),
                null
            )
        )

        TestIfTreeIsAsExpected(code, declarations)

    }

    @Test
    fun operationEqualsTest() {


        val code = """
            Void Main(){
                Int b = 5
                b += 5
                b *= 5
                b -= 5
                b /= 5
            }
        """.trimIndent()

        val localVariables = listOf<Declaration.VariableDeclaration>(
            Declaration.VariableDeclaration(Type.Integer, "b", Expression.Value(ConstantValue.Integer(5)))
        )

        val statementList = listOf<Statement>(
            Statement.AssignValue("b",Expression.Operation(Operator.Plus,Expression.UseVariable("b"), Expression.Value(ConstantValue.Integer(5)))),
            Statement.AssignValue("b",Expression.Operation(Operator.Multiply,Expression.UseVariable("b"), Expression.Value(ConstantValue.Integer(5)))),
            Statement.AssignValue("b",Expression.Operation(Operator.Minus,Expression.UseVariable("b"), Expression.Value(ConstantValue.Integer(5)))),
            Statement.AssignValue("b",Expression.Operation(Operator.Divide,Expression.UseVariable("b"), Expression.Value(ConstantValue.Integer(5)))),

        )

        val tree = CallMain(statementList, localVariables, null,Type.Void)

        TestIfTreeIsAsExpected(code, tree)

    }
}