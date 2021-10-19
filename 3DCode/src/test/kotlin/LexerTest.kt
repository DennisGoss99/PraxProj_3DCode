import Lexer.LexerToken
import Lexer.TestLexer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LexerTest : BaseLexerTest() {

    @Test
    fun simpleLexerTest(){

        val code = """
            int §b = 5;
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("int"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(5),
            LexerToken.Semicolon(),
            LexerToken.EOF,
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList,lexer)

    }

    @Test
    fun simpleCommentLexerTest(){

        val code = """
            //int §b = 5;
        """.trimIndent()

        val lexer = TestLexer(code);

        assertEquals(LexerToken.EOF,lexer.next())
    }

    @Test
    fun ifLexerTest(){

        val code = """
            if(true){
                int §b = 123;
            }else{
                int §baaaFF = 523;
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
            // int b = 123;
            LexerToken.TypeIdent("int"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(123),
            LexerToken.Semicolon(),
            // }else{
            LexerToken.RCurlyBrace(),
            LexerToken.Else(),
            LexerToken.LCurlyBrace(),
            // int baaaFF = 523;
            LexerToken.TypeIdent("int"),
            LexerToken.NameIdent("baaaFF"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(523),
            LexerToken.Semicolon(),
            // }
            LexerToken.RCurlyBrace(),
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList,lexer)
    }

    @Test
    fun advancedIfLexerTest(){

        val code = """
            int §i = 5;
            bool §b = 5<8;
            if(§i >= 7 && §b || true ){
                // NICE
            }
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            // int i = 5;
            LexerToken.TypeIdent("int"),
            LexerToken.NameIdent("i"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(5),
            LexerToken.Semicolon(),
            // int i = 5;
            LexerToken.TypeIdent("bool"),
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

        assertEqualLexerList(expectedLexerTokenList,lexer)
    }

    @Test
    fun typeLexerTest(){

        val code = """
            int §i = 54534;
            char[] §s = "Test";
            bool §b = true;
            char §c = 'h';
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("int"),
            LexerToken.NameIdent("i"),
            LexerToken.AssignEquals(),
            LexerToken.Number_Literal(54534),
            LexerToken.Semicolon(),

            LexerToken.TypeIdent("char[]"),
            LexerToken.NameIdent("s"),
            LexerToken.AssignEquals(),
            LexerToken.String_Literal("Test"),
            LexerToken.Semicolon(),

            LexerToken.TypeIdent("bool"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.Boolean_Literal(true),
            LexerToken.Semicolon(),

            LexerToken.TypeIdent("char"),
            LexerToken.NameIdent("c"),
            LexerToken.AssignEquals(),
            LexerToken.Char_Literal('h'),
            LexerToken.Semicolon(),
            LexerToken.EOF,
        )

        assertEqualLexerList(expectedLexerTokenList,lexer)

    }

    @Test
    fun advancedCommentLexerTest(){

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

        assertEqualLexerList(expectedLexerTokenList,lexer)

    }

    @Test
    fun functionLexerTest(){

        val code = """
            int Hallo(int §a, char[] §b){
            return 0;
            }
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("int"),
            LexerToken.UpperCaseIdent("Hallo"),
            LexerToken.Lparen(),
            LexerToken.TypeIdent("int"),
            LexerToken.NameIdent("a"),
            LexerToken.Comma(),
            LexerToken.TypeIdent("char[]"),
            LexerToken.NameIdent("b"),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.Return(),
            LexerToken.Number_Literal(0),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),
            LexerToken.EOF,
        )

        assertEqualLexerList(expectedLexerTokenList,lexer)

    }

    @Test
    fun functionLexerTest2(){

        val code = """
            void Hallo(char[] §b){
                §b = "TEST";
            }
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("void"),
            LexerToken.UpperCaseIdent("Hallo"),
            LexerToken.Lparen(),
            LexerToken.TypeIdent("char[]"),
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

        assertEqualLexerList(expectedLexerTokenList,lexer)

    }

    @Test
    fun simpleFloatLexerTest(){

        val code = """
            float §b = 5.0;
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.TypeIdent("float"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.Float_Literal(5.0f),
            LexerToken.Semicolon(),
            LexerToken.EOF,
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList,lexer)

    }

    @Test
    fun classTest(){
        val code = """   
            
            class OpenGL{           
                string §name = "";
            
                A(){
                    Println("Hallo");
                }
                
                B(int §a){
                    Println(ToString(§a) + "Hallo" + §name);
                }
            }
                             
            void Main(){
                openGL §b = OpenGL();
            }
            
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.Class(),
            LexerToken.UpperCaseIdent("OpenGL"),
            LexerToken.LCurlyBrace(),
            LexerToken.TypeIdent("string"),
            LexerToken.NameIdent("name"),
            LexerToken.AssignEquals(),
            LexerToken.String_Literal(""),
            LexerToken.Semicolon(),

            LexerToken.UpperCaseIdent("A"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.UpperCaseIdent("Println"),
            LexerToken.Lparen(),
            LexerToken.String_Literal("Hallo"),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),

            LexerToken.UpperCaseIdent("B"),
            LexerToken.Lparen(),
            LexerToken.TypeIdent("int"),
            LexerToken.NameIdent("a"),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.UpperCaseIdent("Println"),
            LexerToken.Lparen(),
            LexerToken.UpperCaseIdent("ToString"),
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

            LexerToken.TypeIdent("void"),
            LexerToken.UpperCaseIdent("Main"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.TypeIdent("openGL"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.UpperCaseIdent("OpenGL"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),

            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList,lexer)
    }

    @Test
    fun class2Test(){
        val code = """   
            
            class OpenGL{           
                string §name = "";
            
                A(){
                    Println("Hallo");
                }
                
                B(int §a){
                    Println(ToString(§a) + "Hallo" + §name);
                }
            }
                             
            void Main(){
                openGL §b = OpenGL();
                
                string §a = §b.§name;
                §b.B(5);
            }
            
        """.trimIndent()

        val lexer = TestLexer(code);

        val expectedLexerTokenList = listOf<LexerToken>(
            LexerToken.Class(),
            LexerToken.UpperCaseIdent("OpenGL"),
            LexerToken.LCurlyBrace(),
            LexerToken.TypeIdent("string"),
            LexerToken.NameIdent("name"),
            LexerToken.AssignEquals(),
            LexerToken.String_Literal(""),
            LexerToken.Semicolon(),

            LexerToken.UpperCaseIdent("A"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.UpperCaseIdent("Println"),
            LexerToken.Lparen(),
            LexerToken.String_Literal("Hallo"),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),

            LexerToken.UpperCaseIdent("B"),
            LexerToken.Lparen(),
            LexerToken.TypeIdent("int"),
            LexerToken.NameIdent("a"),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.UpperCaseIdent("Println"),
            LexerToken.Lparen(),
            LexerToken.UpperCaseIdent("ToString"),
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

            LexerToken.TypeIdent("void"),
            LexerToken.UpperCaseIdent("Main"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.LCurlyBrace(),
            LexerToken.TypeIdent("openGL"),
            LexerToken.NameIdent("b"),
            LexerToken.AssignEquals(),
            LexerToken.UpperCaseIdent("OpenGL"),
            LexerToken.Lparen(),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.TypeIdent("string"),
            LexerToken.NameIdent("a"),
            LexerToken.AssignEquals(),
            LexerToken.NameIdent("b"),
            LexerToken.Dot(),
            LexerToken.NameIdent("name"),
            LexerToken.Semicolon(),
            LexerToken.NameIdent("b"),
            LexerToken.Dot(),
            LexerToken.UpperCaseIdent("B"),
            LexerToken.Lparen(),
            LexerToken.Number_Literal(5),
            LexerToken.Rparen(),
            LexerToken.Semicolon(),
            LexerToken.RCurlyBrace(),
            LexerToken.EOF
        )

        assertEqualLexerList(expectedLexerTokenList,lexer)
    }

}