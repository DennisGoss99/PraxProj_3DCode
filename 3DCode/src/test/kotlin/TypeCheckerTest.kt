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
            
            Int Main(){
                return 5;
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun simpleTest2(){

        val code = """
            
            Int Main(){
                return 5 + 2;
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun simpleTest3(){

        val code = """
                   
            String Main(){
                return "5" + "2";
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun variableTest(){

        val code = """  
            Int Main(){
                Int a = 56 * 2;
                return -(a + a);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun variableTest2(){

        val code = """  
            Int Main(){
                Int a = 56 * 2;
                {
                    Int a = 4;
                    return a;
                }
                return -(a + a);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun globalVariableTest(){

        val code = """  
            Int b = 5;            
            Int Main(){
                Int a = 56 * 2;
                return -(a + b);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun globalVariableTest2(){

        val code = """  
            Int b = 5;   
                     
            Int A(Int a, Int b){
             return a + b;
            } 
                     
            Int Main(){
                Int a = 56 * 2;
                return -(a + b);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun functionTest(){

        val code = """
            
            Int A(){
                return 5;
            }       
                   
            Int Main(){
                return A();
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun functionParameterTest(){

        val code = """
            
            Int A(Int a, Int b){
                return a + b;
            }       
                   
            Int Main(){
                return A(45, 34);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun functionParameterTest2(){

        val code = """
            
            Int A(Int a, Int b){
                return "test";
            }       
                   
            Int Main(){
                return A(45, 34);
            }
            
        """.trimIndent()

        assertFailsWith<TypeCheckerReturnTypeException> { TypeChecker(parseCode(code)).check() }
    }

    @Test
    fun advancedFunctionTest(){

        val code = """
            
            Int Mod(Int n, Int k){
                while(n - k > 0){
                    n = n - k;
                }
                return n;
            }       
                   
            Int Main(){
                return Mod(45,16);
            }
            
        """.trimIndent()

        TypeChecker(parseCode(code)).check()
    }

    @Test
    fun overloadedFuncTest(){

        val code = """
            
            Int A(Int a){
                return a+5;
            }
            
            Bool A(Bool a){
                return a;
            }
            
            Int Main(Int b)
            {
                Bool a = A(false);
                return A(b);
            }
        """.trimIndent()

        TypeChecker(parseCode(code), listOf(Expression.Value(ConstantValue.Integer(9)))).check()

        val code2 = """
            
            Bool A(Bool a){
                return a;
            }
            
            Int A(Int a){
                return a+5;
            }
                        
            Int Main(Int b)
            {
                Bool a = A(false);
                return A(b);
            }
        """.trimIndent()

        TypeChecker(parseCode(code2), listOf(Expression.Value(ConstantValue.Integer(9)))).check()
    }

    @Test
    fun overloadedFunc2Test(){

        val code = """
            
            Int A(Int a){
                return a+5;
            }
            
            Bool A(Bool a){
                return a;
            }
            
            Int Main(Int b)
            {
                Bool a = A(false) && A(b) == 1;
                return A("b");
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerFunctionParameterException::class) { TypeChecker(parseCode(code), listOf(Expression.Value(ConstantValue.Integer(9)))).check() }
    }

    @Test
    fun advancedRecursionTest(){

        val code = """
            
            Int F(Int n){
                if(n == 0 || n == 1){
                    return n;
                }else{
                    return F(n - 1) + F(n - 2)
                }
            }
            
            Int Main(Int b)
            {
                return F(b);
            }
        """.trimIndent()


        TypeChecker(parseCode(code), listOf(Expression.Value(ConstantValue.Integer(9)))).check()
    }

    @Test
    fun classTest(){

        val code = """
            
            class A{
                Int a = 1;
                Int b = b + a;
                Int c = c + b + a;
                
                Void A(){}
            }

            Void Main()
            {
            }
        """.trimIndent()


        TypeChecker(parseCode(code), null).check()
    }

    @Test
    fun classConstructorTest(){

        val code = """
            
            class A{
                Int a = 1;
                Int b = b + a;
                Int c = c + b + a;
                
                Void A(){
                    Int b = 1293 + c;                
                    a = 2 + b;
                }
                
            }

            Void Main()
            {
            }
        """.trimIndent()


        TypeChecker(parseCode(code), null).check()
    }

    @Test
    fun classUseTest(){

        val code = """
            
            class A{
                Int a = 1;
                Int b = b + a;
                Int c = c + b + a;
                
                Void A(){
                    Int b = 1293 + c;                
                    a = 2 + b;
                }
                
            }

            Void Main()
            {
                A a = A();
            }
        """.trimIndent()


        TypeChecker(parseCode(code), null).check()
    }

    @Test
    fun classAdvancedTest(){

        val code = """
            
            class A{
                Int a = 1;
                Int b = b + a;
                Int c = c + b + a;
                
                Void A(){
                    Int b = 1293 + c;                
                    a = 2 + b;
                }
                             
            }

            Int Main()
            {
                A a = A();
                return a.a + a.b;
            }
        """.trimIndent()


        TypeChecker(parseCode(code), null).check()
    }

    @Test
    fun classDuplicateTest(){

        val code = """
            class A{                
                Void A(){
                }
                             
            }
            
            class A{
                Int a = 1;
                Int b = b + a;
                Int c = c + b + a;
                
                Void A(){
                    Int b = 1293 + c;                
                    a = 2 + b;
                }
                             
            }
            
            Int Main()
            {
                A a = A();
                return a.a + a.b;
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateClassException::class){TypeChecker(parseCode(code), null).check()}

    }

    @Test
    fun functionDuplicateTest(){

        val code = """
            Void A(){
            }
            Void A(){
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code), null).check()}

        val code2 = """
            Void A(Int a){
            }
            Void A(Int a){
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code2), null).check()}

        val code3 = """
            Void A(String b, Int a){
            }
            Void A(Int a, String b){
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        TypeChecker(parseCode(code3), null).check()

        val code4 = """
            Void A(String b, Int a){
            }
            Void A(String b, Int a){
            }
            
            Int Main()
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
                Void A(){
                }
                Void A(){
                }
            }
            Int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code), null).check()}

    }

    @Test
    fun methodeDuplicate2Test(){

        val code = """
            class A{
                Void A(Int a){
                }
                Void A(Int a){
                }
            }
            Int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code), null).check()}

        val code2 = """
            class A{
                Void A(String b, Int a){
                }
                Void A(String b, Int a){
                }
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){TypeChecker(parseCode(code2), null).check()}

        val code3 = """
            class A{
                Void A(String b, Int a){
                }
                Void A(Int a, String b){
                }
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        TypeChecker(parseCode(code3), null).check()
    }
}