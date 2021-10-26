import Lexer.Lexer
import Parser.Parser
import Parser.ParserManager
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import TypeChecker.Exceptions.*
import TypeChecker.TypeChecker
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class TypeCheckerTest {

    private fun checkCode(code : String, args : List<Expression.Value>? = null){

        TypeChecker().check(ParserManager.loadFromString(mutableListOf("App" to code)) , args)
    }

    @Test
    fun simpleTest(){

        val code = """
            
            Int Main(){
                return 5
            }
            
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun simpleTest2(){

        val code = """
            
            Int Main(){
                return 5 + 2
            }
            
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun simpleTest3(){

        val code = """
                   
            String Main(){
                return "5" + "2"
            }
            
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun variableTest(){

        val code = """  
            Int Main(){
                Int a = 56 * 2
                return -(a + a)
            }
            
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun variableTest2(){

        val code = """  
            Int Main(){
                Int a = 56 * 2
                {
                    Int a = 4
                    return a
                }
                return -(a + a)
            }
            
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun globalVariableTest(){

        val code = """  
            Int b = 5            
            Int Main(){
                Int a = 56 * 2
                return -(a + b)
            }
            
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun globalVariableTest2(){

        val code = """  
            Int b = 5   
                     
            Int A(Int a, Int b){
             return a + b
            } 
                     
            Int Main(){
                Int a = 56 * 2
                return -(a + b)
            }
            
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun functionTest(){

        val code = """
            
            Int A(){
                return 5
            }       
                   
            Int Main(){
                return A()
            }
            
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun functionParameterTest(){

        val code = """
            
            Int A(Int a, Int b){
                return a + b
            }       
                   
            Int Main(){
                return A(45, 34)
            }
            
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun functionParameterTest2(){

        val code = """
            
            Int A(Int a, Int b){
                return "test"
            }       
                   
            Int Main(){
                return A(45, 34)
            }
            
        """.trimIndent()

        assertFailsWith<TypeCheckerReturnTypeException> { checkCode(code) }
    }

    @Test
    fun advancedFunctionTest(){

        val code = """
            
            Int Mod(Int n, Int k){
                while(n - k > 0){
                    n = n - k
                }
                return n
            }       
                   
            Int Main(){
                return Mod(45,16)
            }
            
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun overloadedFuncTest(){

        val code = """
            
            Int A(Int a){
                return a+5
            }
            
            Bool A(Bool a){
                return a
            }
            
            Int Main(Int b)
            {
                Bool a = A(false)
                return A(b)
            }
        """.trimIndent()

        checkCode(code, listOf(Expression.Value(ConstantValue.Integer(9))))

        val code2 = """
            
            Bool A(Bool a){
                return a
            }
            
            Int A(Int a){
                return a+5
            }
                        
            Int Main(Int b)
            {
                Bool a = A(false)
                return A(b)
            }
        """.trimIndent()

        checkCode(code, listOf(Expression.Value(ConstantValue.Integer(9))))
    }

    @Test
    fun overloadedFunc2Test(){

        val code = """
            
            Int A(Int a){
                return a+5
            }
            
            Bool A(Bool a){
                return a
            }
            
            Int Main(Int b)
            {
                Bool a = A(false) && A(b) == 1
                return A("b")
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerFunctionParameterException::class) { checkCode(code, listOf(Expression.Value(ConstantValue.Integer(9)))) }
    }

    @Test
    fun advancedRecursionTest(){

        val code = """
            
            Int F(Int n){
                if(n == 0 || n == 1){
                    return n
                }else{
                    return F(n - 1) + F(n - 2)
                }
            }
            
            Int Main(Int b)
            {
                return F(b)
            }
        """.trimIndent()


        checkCode(code, listOf(Expression.Value(ConstantValue.Integer(9))))
    }

    @Test
    fun classTest(){

        val code = """
            
            class A{
                Int a = 1
                Int b = b + a
                Int c = c + b + a
                
                Void A(){}
            }

            Void Main()
            {
            }
        """.trimIndent()


        checkCode(code)
    }

    @Test
    fun classConstructorTest(){

        val code = """
            
            class A{
                Int a = 1
                Int b = b + a
                Int c = c + b + a
                
                Void A(){
                    Int b = 1293 + c                
                    a = 2 + b
                }
                
            }

            Void Main()
            {
            }
        """.trimIndent()


        checkCode(code)
    }

    @Test
    fun classUseTest(){

        val code = """
            
            class A{
                Int a = 1
                Int b = b + a
                Int c = c + b + a
                
                Void A(){
                    Int b = 1293 + c                
                    a = 2 + b
                }
                
            }

            Void Main()
            {
                A a = A()
            }
        """.trimIndent()


        checkCode(code)
    }

    @Test
    fun classAdvancedTest(){

        val code = """
            
            class A{
                Int a = 1
                Int b = b + a
                Int c = c + b + a
                
                Void A(){
                    Int b = 1293 + c                
                    a = 2 + b
                }
                             
            }

            Int Main()
            {
                A a = A()
                return a.a + a.b
            }
        """.trimIndent()


        checkCode(code)
    }

    @Test
    fun classDuplicateTest(){

        val code = """
            class A{                
                Void A(){
                }
                             
            }
            
            class A{
                Int a = 1
                Int b = b + a
                Int c = c + b + a
                
                Void A(){
                    Int b = 1293 + c                
                    a = 2 + b
                }
                             
            }
            
            Int Main()
            {
                A a = A()
                return a.a + a.b
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateClassException::class){checkCode(code)}

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

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){checkCode(code)}

        val code2 = """
            Void A(Int a){
            }
            Void A(Int a){
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){checkCode(code2)}

        val code3 = """
            Void A(String b, Int a){
            }
            Void A(Int a, String b){
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        checkCode(code3)

        val code4 = """
            Void A(String b, Int a){
            }
            Void A(String b, Int a){
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){checkCode(code4)}

        val code5 = """
            Void A(String b){
            }
            
            Void A(String b, Int a){
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        checkCode(code5)
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

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){checkCode(code)}

    }

    @Test
    fun methodeDuplicate2Test(){

        val code = """
            class A{
                A(Int a){
                }
                A(Int a){
                }
            }
            Int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){checkCode(code)}

        val code2 = """
            class A{
                A(String b, Int a){
                }
                A(String b, Int a){
                }
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerDuplicateFunctionException::class){checkCode(code2)}

        val code3 = """
            class A{
                A(String b, Int a){
                }
                A(Int a, String b){
                }
            }
            
            Int Main()
            {
            }
        """.trimIndent()

        checkCode(code3)
    }

    @Test
    fun constructorReturnTest() {

        val code = """
            class A{
                A(Int a){
                }
                Int A(Int a, Float b){
                }
            }
            Int Main()
            {
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerReturnTypeException::class) { checkCode(code) }
    }

    @Test
    fun genericsFunctionTest() {

        val code = """
            String Fun<A>(A a){
                A b = a
                return a.ToString()
            }
            
            Void Main()
            {
                String s = ToString(Fun<Int>(5)) + Fun<String>("Hallo")
            }
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun genericsFunction2Test() {

        val code = """
            A Fun<A>(A a){
                return a
            }
            
            Void Main()
            {
                Int i = Fun<Int>(5)
                String s = Fun<String>("Hallo")
            }
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun genericsFunction3Test() {

        val code = """
            A Fun<A>(A a){
                return Fun<A>(a, 5)
            }
            
            B Fun<B>(B a, Int b){
                return a
            }
            
            Void Main()
            {
                Int i = Fun<Int>(5)
                String s = Fun<String>("Hallo")
            }
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun genericsFunction4Test() {

        val code = """
            B Fun<A,B>(A a, B b){
                return Fun<B>(b)
            }

            A Fun<A>(A a){
                return a
            }

            Void Main()
            {
                Int i = Fun<Int>(5)
                String s = Fun<Float, String>(12.0, "Hallo")
            }
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun genericsClassTest() {

        val code = """
            class <T> A{
                
                A(){}          
            }
            
            Void Main()
            {
                A objA<String> = A<String>()
            }
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun genericsClass2Test() {

        val code = """
            class <T> A{
                
                A(T t){}          
            }
            
            Void Main()
            {
                A objA<String> = A<String>("Hallo")
            }
        """.trimIndent()

        checkCode(code)

        val code2 = """
            class A{
                
                A<T>(T t){}          
            }
            
            Void Main()
            {
                A objA = A<String>("Hallo")
            }
        """.trimIndent()

        checkCode(code2)

        val code3 = """
            class A{
                
                A(T t){}          
            }
            
            Void Main()
            {
                A objA = A<String>("Hallo")
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerFunctionParameterException::class) { checkCode(code3) }
    }

    @Test
    fun genericsClass3Test() {

        val code = """
            class <T> A{
                
                A(T t){}  
                        
                T B<X>(X a){
                }
            }
            
            Void Main()
            {
                A objA<String> = A<String>("Hallo")
                String s = objA.B<Int>(5)
                
            }
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun genericsClass4Test() {

        val code = """
            
            class <T> List{
                T a = null
                
                List(T b){
                    a = b
                }  
                
            }
            
            Void Main()
            {
                List objA<Int> = List<Int>(3)
                objA.a = 4 + 1
                
                
            }
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun genericsClass5Test() {

        val code = """
            class <X> D{
                X a = null
                            
                D(X d){
                    a = d
                }
            }
            
            class <T> A{
                
                A(T t){}  
                        
                T B<X>(X a){
                }
            }
            
            Void Main()
            {
                A objA<String> = A<String>("Hallo")
                D objD<A> = D<A>(objA)
                String s = objA.B<Int>(5)
                
            }
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun genericsClass6Test() {

        val code = """
            class <X,Y> Pair{
                X x = null
                Y y = null
                            
                Pair(X xx, Y yy){
                    x = xx
                    y = yy
                }
                
                X GetFirst(){
                    return x
                }
                
                Y GetSecond(){
                    return y
                }
                
                A Test<A>(A a){
                    return a
                }
            }
            
            String Main()
            {
                Pair p<Int,String> = Pair<Int,String>(5, 70)
                Int i = p.x
                String a = ""
               
                a = p.Test<String>(a)
               
                p.x += 5
                p.y = p.x.ToString() + p.y
                return p.GetSecond()
            }
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun genericsClass7Test() {

        val code = """
            class Pair{
            
                Pair(){}
                
                A Test<A>(A a){
                    return a
                }
            }
            
            A Test<A>(A a){
                return a
            }
            
            String Main()
            {
                Pair p = Pair()
                String a = ""
               
                a = p.Test<String>(a)
                return a
            }
        """.trimIndent()

        checkCode(code)
    }

    @Test
    fun privateConstructorTest() {
        val code = """
            private class Math{
                private Int a = 0
            
                Math(){}
                private Math(Int b){}
            }
            
            Void Main(){
                Math a = Math()
                Math b = Math(5)
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerCantAccessPrivate::class) { checkCode(code) }

        val code2 = """
            private class Math{
                private Int a = 0
                Math c = null
            
                Math(){
                    c = Math(3)
                }
                private Math(Int b){}
            }
            
            Void Main(){
                Math a = Math()
            }
        """.trimIndent()

        checkCode(code2)
    }


    @Test
    fun privateClassVariableTest() {
        val code = """
            private class Math{
                private Int a = 0
                Math c = null
            
                Math(){
                    c = Math(3)
                }
                private Math(Int b){}
            }
            
            Void Main(){
                Math a = Math()
                Int b = a.a 
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerCantAccessPrivate::class) { checkCode(code) }

        val code2 = """
            private class Math{
                private Int a = 0
                Math c = null
            
                Math(){
                    c = Math(3)
                    {
                        Int x = c.a
                    }
                }
                private Math(Int b){}
            }
            
            Void Main(){
                Math a = Math()
            }
        """.trimIndent()

        checkCode(code2)


        val code3 = """
            private class Math{
                private Int a = 0
                Math c = null
            
                Math(){
                    c = Math(3)
                    {
                        Int x = c.a
                    }
                }
                private Math(Int b){}
            }
            
            Void Main(){
                Math a = Math()
                Int x = a.c.a
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerCantAccessPrivate::class) { checkCode(code3) }
    }

    @Test
    fun privateClassVoidMethodTest() {
        val code = """
            private class Math{
                private Int a = 0
            
                Math(){}
                private Math(Int b){}
                
                private Void A(){}
            }
            
            Void Main(){
                Math a = Math()
                a.A()
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerCantAccessPrivate::class) { checkCode(code) }

        val code2 = """
            private class Math{
                private Int a = 0
            
                Math(){
                    A()
                }
                private Math(Int b){}
                
                private Void A(){}
            }
            
            Void Main(){
                Math a = Math()
            }
        """.trimIndent()

        checkCode(code2)
    }

    @Test
    fun privateClassMethodTest() {
        val code = """
            private class Math{
                private Int a = 0
            
                Math(){}
                private Math(Int b){}
                
                private Int A(){
                    return 0
                }
            }
            
            Void Main(){
                Math a = Math()
                Int b = a.A()
            }
        """.trimIndent()

        assertFailsWith(TypeCheckerCantAccessPrivate::class) { checkCode(code) }

        val code2 = """
            private class Math{
                private Int a = 0
            
                Math(){
                    Int xx = A()
                }
                private Math(Int b){}
                
                private Int A(){
                    return 0
                }
            }
            
            Void Main(){
                Math a = Math()
            }
        """.trimIndent()

        checkCode(code2)
    }

    @Test
    fun privateFunVarTest() {
        val code = """
            private Int a = 6
            private Int A(){
                return 6
            }
            private Void B(){}
            Void Main(){
                Int b = A()
                Int c = a
                a = 6
                B()
            }
        """.trimIndent()

        checkCode(code)
    }

}