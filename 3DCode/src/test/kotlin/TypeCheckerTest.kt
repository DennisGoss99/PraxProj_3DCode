import Lexer.Lexer
import Parser.Parser
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import TypeChecker.Exceptions.*
import TypeChecker.TypeChecker
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class TypeCheckerTest {

    private fun parseCode(code : String): List<Declaration> {
        return Parser(Lexer(code)).ParsingStart()
    }

    @Test
    fun simpleTest(){

        val code = """
            
            int Main(){
                return 5;
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun simpleTest2(){

        val code = """
            
            int Main(){
                return 5 + 2;
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun simpleTest3(){

        val code = """
                   
            string Main(){
                return "5" + "2";
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun variableTest(){

        val code = """  
            int Main(){
                int §a = 56 * 2;
                return -(§a + §a);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun variableTest2(){

        val code = """  
            int Main(){
                int §a = 56 * 2;
                {
                    int §a = 4;
                    return §a;
                }
                return -(§a + §a);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun globalVariableTest(){

        val code = """  
            int §b = 5;            
            int Main(){
                int §a = 56 * 2;
                return -(§a + §b);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun globalVariableTest2(){

        val code = """  
            int §b = 5;   
                     
            int A(int §a, int §b){
             return §a + §b;
            } 
                     
            int Main(){
                int §a = 56 * 2;
                return -(§a + §b);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun functionTest(){

        val code = """
            
            int A(){
                return 5;
            }       
                   
            int Main(){
                return A();
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun functionParameterTest(){

        val code = """
            
            int A(int §a, int §b){
                return §a + §b;
            }       
                   
            int Main(){
                return A(45, 34);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun functionParameterTest2(){

        val code = """
            
            int A(int §a, int §b){
                return "test";
            }       
                   
            int Main(){
                return A(45, 34);
            }
            
        """.trimIndent()

        assertFailsWith<TypeCheckerReturnTypeException> { TypeChecker(parseCode(code)).check() }
    }

    @Test
    fun advancedFunctionTest(){

        val code = """
            
            int Mod(int §n, int §k){
                while(§n - §k > 0){
                    §n = §n - §k;
                }
                return §n;
            }       
                   
            int Main(){
                return Mod(45,16);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun overloadedFuncTest(){

        val code = """
            
            int A(int §a){
                return §a+5;
            }
            
            bool A(bool §a){
                return §a;
            }
            
            int Main(int §b)
            {
                bool §a = A(false);
                return A(§b);
            }
        """.trimIndent()

        TypeChecker(parseCode(code), listOf(Expression.Value(ConstantValue.Integer(9)))).check()

        val code2 = """
            
            bool A(bool §a){
                return §a;
            }
            
            int A(int §a){
                return §a+5;
            }
                        
            int Main(int §b)
            {
                bool §a = A(false);
                return A(§b);
            }
        """.trimIndent()

        TypeChecker(parseCode(code2), listOf(Expression.Value(ConstantValue.Integer(9)))).check()
    }

    @Test
    fun overloadedFunc2Test(){

        val code = """
            
            int A(int §a){
                return §a+5;
            }
            
            bool A(bool §a){
                return §a;
            }
            
            int Main(int §b)
            {
                bool §a = A(false) && A(§b) == 1;
                return A("§b");
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerFunctionParameterException::class) { TypeChecker(parseCode(code), listOf(Expression.Value(ConstantValue.Integer(9)))).check() }
    }

    @Test
    fun advancedRecursionTest(){

        val code = """
            
            int F(int §n){
                if(§n == 0 || §n == 1){
                    return §n;
                }else{
                    return F(§n - 1) + F(§n - 2)
                }
            }
            
            int Main(int §b)
            {
                return F(§b);
            }
        """.trimIndent()


        TypeChecker(parseCode(code), listOf(Expression.Value(ConstantValue.Integer(9)))).check()
    }

    @Test
    fun classTest(){

        val code = """
            
            class A{
                int §a = 1;
                int §b = §b + §a;
                int §c = §c + §b + §a;
            }

            void Main()
            {
            }
        """.trimIndent()


        TypeChecker(parseCode(code), null).check()
    }

    @Test
    fun classConstructorTest(){

        val code = """
            
            class A{
                int §a = 1;
                int §b = §b + §a;
                int §c = §c + §b + §a;
                
                void A(){
                    int §b = 1293 + §c;                
                    §a = 2 + §b;
                }
                
            }

            void Main()
            {
            }
        """.trimIndent()


        TypeChecker(parseCode(code), null).check()
    }

    @Test
    fun classUseTest(){

        val code = """
            
            class A{
                int §a = 1;
                int §b = §b + §a;
                int §c = §c + §b + §a;
                
                void A(){
                    int §b = 1293 + §c;                
                    §a = 2 + §b;
                }
                
            }

            void Main()
            {
                a §a = A();
            }
        """.trimIndent()


        TypeChecker(parseCode(code), null).check()
    }

    @Test
    fun classAdvancedTest(){

        val code = """
            
            class A{
                int §a = 1;
                int §b = §b + §a;
                int §c = §c + §b + §a;
                
                void A(){
                    int §b = 1293 + §c;                
                    §a = 2 + §b;
                }
                             
            }

            int Main()
            {
                a §a = A();
                return §a.§a + §a.§b;
            }
        """.trimIndent()


        TypeChecker(parseCode(code), null).check()
    }

    @Test
    fun classDuplicateTest(){

        val code = """
            class A{                
                void A(){
                }
                             
            }
            
            class A{
                int §a = 1;
                int §b = §b + §a;
                int §c = §c + §b + §a;
                
                void A(){
                    int §b = 1293 + §c;                
                    §a = 2 + §b;
                }
                             
            }
            
            int Main()
            {
                a §a = A();
                return §a.§a + §a.§b;
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateClassException::class){TypeChecker(parseCode(code), null).check()}

    }

    @Test
    fun functionDuplicateTest(){

        val code = """
            void A(){
            }
            void A(){
            }
            
            int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code), null).check()}

        val code2 = """
            void A(int §a){
            }
            void A(int §a){
            }
            
            int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code2), null).check()}

        val code3 = """
            void A(string §b, int §a){
            }
            void A(int §a, string §b){
            }
            
            int Main()
            {
            }
        """.trimIndent()

        TypeChecker(parseCode(code3), null).check()

        val code4 = """
            void A(string §b, int §a){
            }
            void A(string §b, int §a){
            }
            
            int Main()
            {
            }
        """.trimIndent()

        TypeChecker(parseCode(code3), null).check()
        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code4), null).check()}
    }

    @Test
    fun methodeDuplicateTest(){

        val code = """
            class A{
                void A(){
                }
                void A(){
                }
            }
            int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code), null).check()}

    }

    @Test
    fun methodeDuplicate2Test(){

        val code = """
            class A{
                void A(int §a){
                }
                void A(int §a){
                }
            }
            int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code), null).check()}

        val code2 = """
            class A{
                void A(string §b, int §a){
                }
                void A(string §b, int §a){
                }
            }
            
            int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code2), null).check()}

        val code3 = """
            class A{
                void A(string §b, int §a){
                }
                void A(int §a, string §b){
                }
            }
            
            int Main()
            {
            }
        """.trimIndent()

        TypeChecker(parseCode(code3), null).check()
    }
}