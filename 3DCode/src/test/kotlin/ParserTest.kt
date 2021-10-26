import Lexer.TestLexer
import Parser.*
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParserTest
{
    private fun callMain(statementList: List<Statement>, localVariables: List<Declaration.VariableDeclaration>? = null, parameters: List<Parameter>? = null, returnType : Type = Type.Integer): List<Declaration>
    {
        return listOf<Declaration>(Declaration.FunctionDeclare(
            returnType,
            "Main",
            Body(statementList, localVariables),
            parameters, null
        ))
    }

    private fun testIfTreeIsAsExpected(code : String, declaration: List<Declaration>)
    {
        val lexer = TestLexer(code)
        val parser = Parser(lexer, "Test")
        val parserTokenTree = parser.parsingStart()

        assertEquals(declaration.toString(), parserTokenTree.toString())
    }


    @Test
    fun returnDirectTest()
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

        val tree = callMain(statementList,null, null)

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun returnWithAdditionTest()
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

        val tree = callMain(statementList, null, null)

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun returnWithDeclarationTest()
    {
        val code = """
            Int Main()
            {
                Int a = 0
                
                return a
            }
        """.trimIndent()

        val localVariables = listOf(
            Declaration.VariableDeclaration(Type.Integer, "a", Expression.Value(ConstantValue.Integer(0)))
        )

        val statementList = listOf<Statement>(
            Statement.AssignValue(
                "return",
                Expression.UseVariable("a")
            )
        )

        val tree = callMain(statementList, localVariables, null)

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun returnWithLoopTest()
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

        val localVariables = listOf(
            Declaration.VariableDeclaration(Type.Integer, "a", Expression.Value(ConstantValue.Integer(1)))
        )

        val statementList = listOf(
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

        val tree = callMain(statementList, localVariables, null)

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun returnWithParametersTest()
    {
        val code = """
            Int Main(Int a, Int b)
            {
                return a * b
            }
        """.trimIndent()

        val parameters = listOf(
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

        val tree = callMain(statementList,null, parameters)

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun returnWithFunctionCallTest()
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
                        ),
                        null
                    )
                )
            )
        )

        val tree = callMain(statementList, null, null)

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun returnWithIFTest()
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

        val localVariables = listOf(
            Declaration.VariableDeclaration(Type.Integer, "w", Expression.Value(ConstantValue.Integer(3))),
            Declaration.VariableDeclaration(Type.Boolean, "f", Expression.Operation(Operator.LessEqual, Expression.UseVariable("w"), Expression.Value(
                ConstantValue.Integer(3))))
        )

        val statementList = listOf(
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

        val tree = callMain(statementList, localVariables, null)

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun floatReturnWithLoopTest()
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

        val localVariables = listOf(
            Declaration.VariableDeclaration(Type.Float, "a", Expression.Value(ConstantValue.Float(1.0f)))
        )

        val statementList = listOf(
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

        val tree = callMain(statementList, localVariables, null, Type.Float)

        testIfTreeIsAsExpected(code, tree)
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

        val declarations = listOf(
            Declaration.ClassDeclare("OpenGL", ClassBody(
                hashMapOf(
                    "A" to mutableListOf(Declaration.FunctionDeclare(Type.Void,"A",
                        Body(
                            listOf<Statement>(
                                Statement.ProcedureCall(
                                    "Println",
                                    listOf<Expression>(
                                        Expression.Value(ConstantValue.String("Hallo"))
                                    ),
                                    null
                                )
                            )
                        ),null, null))),
                listOf(
                    Declaration.VariableDeclaration(Type.String,"name",Expression.Value(ConstantValue.String("")))
                )
            ), null),
            Declaration.FunctionDeclare(
                Type.Void,
                "Main",
                Body(
                    listOf(),
                    listOf(Declaration.VariableDeclaration(Type.Custom("OpenGL"),"b",Expression.FunctionCall("OpenGL",null, null)))
                ),
                null, null
            )
        )

        testIfTreeIsAsExpected(code, declarations)
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
            
            class Aa{           
                String a = ""
                Float b = 0.0
            }
                             
            Void Main(){
                OpenGL b = OpenGL()
            }
            
        """.trimIndent()

        val declarations = listOf(
            Declaration.ClassDeclare("OpenGL", ClassBody(
                hashMapOf(
                    "A" to mutableListOf(Declaration.FunctionDeclare(Type.Void,"A",
                        Body(
                            listOf<Statement>(
                                Statement.ProcedureCall(
                                    "Println",
                                    listOf<Expression>(
                                        Expression.Value(ConstantValue.String("Hallo"))
                                    ),
                                    null
                                )
                            )
                        ),null, null))),
                listOf(
                    Declaration.VariableDeclaration(Type.String,"name",Expression.Value(ConstantValue.String("")))
                )
            ), null),
            Declaration.ClassDeclare("Aa", ClassBody(
                hashMapOf(),
                listOf(
                    Declaration.VariableDeclaration(Type.String,"a",Expression.Value(ConstantValue.String(""))),
                    Declaration.VariableDeclaration(Type.Float,"b",Expression.Value(ConstantValue.Float(0.0f)))
                )
            ), null),
            Declaration.FunctionDeclare(
                Type.Void,
                "Main",
                Body(
                    listOf(),
                    listOf(Declaration.VariableDeclaration(Type.Custom("OpenGL"),"b",Expression.FunctionCall("OpenGL",null, null)))
                ),
                null, null
            )
        )

        testIfTreeIsAsExpected(code, declarations)
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

        val declarations = listOf(
            Declaration.ClassDeclare("OpenGL", ClassBody(
                hashMapOf(
                    "A" to mutableListOf(Declaration.FunctionDeclare(Type.Void,"A",
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
                                ),
                                null
                            )
                        )
                    ), listOf(Parameter("a",Type.Integer)), null))
                ),
                listOf(
                    Declaration.VariableDeclaration(Type.String,"name",Expression.Value(ConstantValue.String("")))
                )
            ), null),
            Declaration.FunctionDeclare(
                Type.Void,
                "Main",
                Body(
                    listOf<Statement>(
                        Statement.UseClass("b", Statement.ProcedureCall("B", listOf(Expression.Value(ConstantValue.Integer(5))), null))
                    ),
                    listOf(
                        Declaration.VariableDeclaration(Type.Custom("OpenGL"),"b",Expression.FunctionCall("OpenGL",null, null)),
                        Declaration.VariableDeclaration(Type.String,"a", Expression.UseDotVariable("b",Expression.UseVariable("name"))),
                    )
                ),
                null, null
            )
        )

        testIfTreeIsAsExpected(code, declarations)

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

        val localVariables = listOf(
            Declaration.VariableDeclaration(Type.Integer, "b", Expression.Value(ConstantValue.Integer(5)))
        )

        val statementList = listOf<Statement>(
            Statement.AssignValue("b",Expression.Operation(Operator.Plus,Expression.UseVariable("b"), Expression.Value(ConstantValue.Integer(5)))),
            Statement.AssignValue("b",Expression.Operation(Operator.Multiply,Expression.UseVariable("b"), Expression.Value(ConstantValue.Integer(5)))),
            Statement.AssignValue("b",Expression.Operation(Operator.Minus,Expression.UseVariable("b"), Expression.Value(ConstantValue.Integer(5)))),
            Statement.AssignValue("b",Expression.Operation(Operator.Divide,Expression.UseVariable("b"), Expression.Value(ConstantValue.Integer(5)))),

        )

        val tree = callMain(statementList, localVariables, null,Type.Void)

        testIfTreeIsAsExpected(code, tree)

    }

    @Test
    fun genericFunctionTest() {

        val code = """
            Int Math<A>(A a){
            }
    """.trimIndent()

        val tree = listOf<Declaration>(
            Declaration.FunctionDeclare(
                Type.Integer,
                "Math",
                Body(listOf(),null),
                listOf(Parameter("a", Type.Custom("A"))),
                listOf("A")
            )
        )

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun genericFunctionCallTest() {

        val code = """
            Int Main()
            {
                return Fun<Int>()
            }
        """.trimIndent()

        val statementList = listOf<Statement>(
            Statement.AssignValue(
                "return",
                Expression.FunctionCall(
                    "Fun",
                    null,
                    listOf(Type.Integer)
                )
            )
        )

        val tree = callMain(statementList, null, null)

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun genericClassTest() {

        val code = """
            class <A>Math{
            }
        """.trimIndent()

        val tree = listOf<Declaration>(
            Declaration.ClassDeclare(
                "Math",
                ClassBody(hashMapOf(), listOf()),
                listOf("A")
            )
        )

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun genericClass2Test() {

        val code = """
            class <A>Math{
                Math<A>(A t){}
            }
        """.trimIndent()

        val tree = listOf<Declaration>(
            Declaration.ClassDeclare(
                "Math",
                ClassBody(hashMapOf(
                   "Math" to mutableListOf(Declaration.FunctionDeclare(
                       Type.Void, "Math", Body(listOf(),null),
                       listOf(Parameter("t", Type.Custom("A"))),
                       listOf("A")
                   )
                ))
                    , listOf()),
                listOf("A")
            )
        )

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun genericClass3Test() {

        val code = """
            Void Main()
            {
                A objA<String> = A<String>("Hallo")
            }
        """.trimIndent()

        val localVariables = listOf(
            Declaration.VariableDeclaration(
                Type.CustomWithGenerics("A", listOf(Type.String)),
                "objA",
                Expression.FunctionCall(
                    "A",
                    listOf(Expression.Value(ConstantValue.String("Hallo"))),
                    listOf(Type.String)
                )
            )
        )

        val tree = callMain(listOf(), localVariables, null,Type.Void)

        testIfTreeIsAsExpected(code, tree)
    }

    @Test
    fun genericClassPlusEqualsTest() {

        val code = """
            Void Main()
            {
                p.x += 5
            }
        """.trimIndent()

        val statements = listOf(
            Statement.UseClass(
                "p",
                Statement.AssignValue(
                    "x",
                    Expression.Operation(
                        Operator.Plus,
                        Expression.UseDotVariable("p",Expression.UseVariable("x")),
                        Expression.Value(ConstantValue.Integer(5))
                    )
                )
            )
        )

        val tree = callMain(statements, null, null,Type.Void)

        testIfTreeIsAsExpected(code, tree)
    }


    @Test
    fun privateTest() {

        val code = """
            private class Math{
                private Int a = 0
            
                private Math(){}
            }
        """.trimIndent()

        val tree = listOf<Declaration>(
            Declaration.ClassDeclare(
                "Math",
                ClassBody(hashMapOf(
                    "Math" to mutableListOf(Declaration.FunctionDeclare(
                        Type.Void, "Math", Body(listOf(),null),
                        null, null, true
                    )))
                    , listOf(
                            Declaration.VariableDeclaration(Type.Integer,"a",Expression.Value(ConstantValue.Integer(0)),true)
                    )), null, true
            )
        )

        testIfTreeIsAsExpected(code, tree)
    }
}