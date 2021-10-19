import Lexer.LexerToken
import Lexer.TestLexer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LexerTest : BaseLexerTest() {

    @Test
    fun simpleLexerTest() {

        val code = """
            Int b = 5;
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("Int"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(5),
            LexerToken.Semicolon(),
            LexerToken.EOF,
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)

    }

    @Test
    fun simpleCommentLexerTest() {

        val code = """
            //Int b = 5;
        """.trimIndent()

        val lexer = TestLexer(code);

        assertEquals(LexerToken.EOF, lexer.next())
    }

    @Test
    fun ifLexerTest() {

        val code = """
            if(true){
                Int b = 123;
            }else{
                Int baaaFF = 523;
            }
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            //if(true){
            LexerToken.If(),
            LexerToken.Lparen(),
            LexerToken.Boolean_Literal(true),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            // Int b = 123;
            LexerToken.TypeIdent("Int"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(123),
            LexerToken.Semicolon(),
            // }else{
            LexerToken.RCurlyBrace(),
            LexerToken.Else(),
            LexerToken.LCurlyBrace(),
            // Int baaaFF = 523;
            LexerToken.TypeIdent("Int"),
            LexerToken.NameIdent("baaaFF"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(523),
            LexerToken.Semicolon(),
            // }
            LexerToken.RCurlyBrace(),
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)
    }

    @Test
    fun advancedIfLexerTest() {

        val code = """
            Int i = 5;
            Bool b = 5<8;
            if(i >= 7 && b || true ){
                // NICE
            }
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            // Int i = 5;
            LexerToken.TypeIdent("Int"),
            LexerToken.NameIdent("i"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(5),
            LexerToken.Semicolon(),
            // Int i = 5;
            LexerToken.TypeIdent("Bool"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(5),
            LexerToken.Less(),
            LexerToken.Number_Literal(8),
            LexerToken.Semicolon(),
            //if(i >= 7 && b || true )
            LexerToken.If(),
            LexerToken.Lparen(),
            LexerToken.NameIdent("i"),
            LexerToken.GreaterEqual(),
            LexerToken.Number_Literal(7),
            LexerToken.And(),
            LexerToken.NameIdent("b"),
            LexerToken.Or(),
            LexerToken.Boolean_Literal(true),
            LexerToken.Rparen(),
            //{}
            LexerToken.LCurlyBrace(),
            LexerToken.RCurlyBrace(),
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)
    }

    @Test
    fun typeLexerTest() {

        val code = """
            Int i = 54534;
            Char[] s = "Test";
            Bool b = true;
            Char c = 'h';
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("Int"),
            LexerToken.NameIdent("i"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(54534),
            LexerToken.Semicolon(),

            LexerToken.TypeIdent("Char[]"),
            LexerToken.NameIdent("s"),
            LexerToken.AssignEquals(),
            LexerToken.String_Literal("Test"),
            LexerToken.Semicolon(),

            LexerToken.TypeIdent("Bool"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.Boolean_Literal(true),
            LexerToken.Semicolon(),

            LexerToken.TypeIdent("Char"),
            LexerToken.NameIdent("c"),
            LexerToken.AssignEquals(),
            LexerToken.Char_Literal('h'),
            LexerToken.Semicolon(),
            LexerToken.EOF,
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)

    }

    @Test
    fun advancedCommentLexerTest() {

        val code = """
        if (false){
            // Huge if true
        6 + 4312;
        // smol
        /* ich
        bin auch
        */
        /* Ich /* bin geschachtelt */ */
        }else{ 3 + (4 + 5);}
        // Test
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            //if(true){
            LexerToken.If(),
            LexerToken.Lparen(),
            LexerToken.Boolean_Literal(false),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            //6 + 4312;
            LexerToken.Number_Literal(6),
            LexerToken.Plus(),
            LexerToken.Number_Literal(4312),
            LexerToken.Semicolon(),
            // }else{
            LexerToken.RCurlyBrace(),
            LexerToken.Else(),
            LexerToken.LCurlyBrace(),
            // 3 + (4 + 5);
            LexerToken.Number_Literal(3),
            LexerToken.Plus(),
            LexerToken.Lparen(),
            LexerToken.Number_Literal(4),
            LexerToken.Plus(),
            LexerToken.Number_Literal(5),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            // }
            LexerToken.RCurlyBrace(),
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)

    }

    @Test
    fun functionLexerTest() {

        val code = """
            Int Hallo(Int a, Char[] b){
            return 0;
            }
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("Int"),
            LexerToken.FunctionIdent("Hallo"),
            LexerToken.Lparen(),
            LexerToken.TypeIdent("Int"),
            LexerToken.NameIdent("a"),
            LexerToken.Comma(),
            LexerToken.TypeIdent("Char[]"),
            LexerToken.NameIdent("b"),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.Return(),
            LexerToken.Number_Literal(0),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),
            LexerToken.EOF,
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)

    }

    @Test
    fun functionLexerTest2() {

        val code = """
            Void Hallo(Char[] b){
                b = "TEST";
            }
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("Void"),
            LexerToken.FunctionIdent("Hallo"),
            LexerToken.Lparen(),
            LexerToken.TypeIdent("Char[]"),
            LexerToken.NameIdent("b"),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.String_Literal("TEST"),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),
            LexerToken.EOF,
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)

    }

    @Test
    fun simpleFloatLexerTest() {

        val code = """
            Float b = 5.0;
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("Float"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.Float_Literal(5.0f),
            LexerToken.Semicolon(),
            LexerToken.EOF,
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)

    }

    @Test
    fun classTest() {
        val code = """   
            
            class OpenGL{           
                String name = "";
            
                A(){
                    Println("Hallo");
                }
                
                B(Int a){
                    Println(ToString(a) + "Hallo" + name);
                }
            }
                             
            Void Main(){
                OpenGL b = OpenGL();
            }
            
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.Class(),
            LexerToken.TypeIdent("OpenGL"),
            LexerToken.LCurlyBrace(),
            LexerToken.TypeIdent("String"),
            LexerToken.NameIdent("name"),
            LexerToken.AssignEquals(),
            LexerToken.String_Literal(""),
            LexerToken.Semicolon(),

            LexerToken.FunctionIdent("A"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.FunctionIdent("Println"),
            LexerToken.Lparen(),
            LexerToken.String_Literal("Hallo"),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),

            LexerToken.FunctionIdent("B"),
            LexerToken.Lparen(),
            LexerToken.TypeIdent("Int"),
            LexerToken.NameIdent("a"),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.FunctionIdent("Println"),
            LexerToken.Lparen(),
            LexerToken.FunctionIdent("ToString"),
            LexerToken.Lparen(),
            LexerToken.NameIdent("a"),
            LexerToken.Rparen(),
            LexerToken.Plus(),
            LexerToken.String_Literal("Hallo"),
            LexerToken.Plus(),
            LexerToken.NameIdent("name"),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),
            LexerToken.RCurlyBrace(),

            LexerToken.TypeIdent("Void"),
            LexerToken.FunctionIdent("Main"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.TypeIdent("OpenGL"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.FunctionIdent("OpenGL"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),

            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)
    }

    @Test
    fun class2Test() {
        val code = """   
            
            class OpenGL{           
                String name = "";
            
                A(){
                    Println("Hallo");
                }
                
                B(Int a){
                    Println(ToString(a) + "Hallo" + name);
                }
            }
                             
            Void Main(){
                OpenGL b = OpenGL();
                
                String a = b.name;
                b.B(5);
            }
            
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.Class(),
            LexerToken.TypeIdent("OpenGL"),
            LexerToken.LCurlyBrace(),
            LexerToken.TypeIdent("String"),
            LexerToken.NameIdent("name"),
            LexerToken.AssignEquals(),
            LexerToken.String_Literal(""),
            LexerToken.Semicolon(),

            LexerToken.FunctionIdent("A"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.FunctionIdent("Println"),
            LexerToken.Lparen(),
            LexerToken.String_Literal("Hallo"),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),

            LexerToken.FunctionIdent("B"),
            LexerToken.Lparen(),
            LexerToken.TypeIdent("Int"),
            LexerToken.NameIdent("a"),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.FunctionIdent("Println"),
            LexerToken.Lparen(),
            LexerToken.FunctionIdent("ToString"),
            LexerToken.Lparen(),
            LexerToken.NameIdent("a"),
            LexerToken.Rparen(),
            LexerToken.Plus(),
            LexerToken.String_Literal("Hallo"),
            LexerToken.Plus(),
            LexerToken.NameIdent("name"),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),
            LexerToken.RCurlyBrace(),

            LexerToken.TypeIdent("Void"),
            LexerToken.FunctionIdent("Main"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.TypeIdent("OpenGL"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.FunctionIdent("OpenGL"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.TypeIdent("String"),
            LexerToken.NameIdent("a"),
            LexerToken.AssignEquals(),
            LexerToken.NameIdent("b"),
            LexerToken.Dot(),
            LexerToken.NameIdent("name"),
            LexerToken.Semicolon(),
            LexerToken.NameIdent("b"),
            LexerToken.Dot(),
            LexerToken.FunctionIdent("B"),
            LexerToken.Lparen(),
            LexerToken.Number_Literal(5),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)
    }

    @Test
    fun operationEqualsLexerTest() {

        val code = """
            Int b = 5;
            b += 5;
            b *= 5;
            b -= 5;
            b /= 5;
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("Int"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(5),
            LexerToken.Semicolon(),
            LexerToken.NameIdent("b"),
            LexerToken.AssignPlusEquals(),
            LexerToken.Number_Literal(5),
            LexerToken.Semicolon(),
            LexerToken.NameIdent("b"),
            LexerToken.AssignMulEquals(),
            LexerToken.Number_Literal(5),
            LexerToken.Semicolon(),
            LexerToken.NameIdent("b"),
            LexerToken.AssignMinusEquals(),
            LexerToken.Number_Literal(5),
            LexerToken.Semicolon(),
            LexerToken.NameIdent("b"),
            LexerToken.AssignDivEquals(),
            LexerToken.Number_Literal(5),
            LexerToken.Semicolon(),
            LexerToken.EOF,
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)
    }

    @Test
    fun importLexerTest() {

        val code = """
            include "Test.A.Math"
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.Import(),
            LexerToken.String_Literal("Test.A.Math"),
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList, lexer)

    }
}