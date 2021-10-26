import Evaluator.Evaluator
import Parser.ParserManager
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.IValue
import TypeChecker.Exceptions.TypeCheckerVariableNotFoundException
import TypeChecker.TypeChecker
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeepTest {

    private fun executeCode(code : String, args: List<Expression.Value>? = null): IValue? {
        return executeCode(mutableListOf("App" to code), args)
    }

    private fun executeCode(code : MutableList<Pair<String, String>>, args: List<Expression.Value>? = null): IValue? {

        val mainFile = ParserManager.loadFromString(code)
        TypeChecker().check(mainFile, args)

        return Evaluator().eval(mainFile,args)?.value
    }

    private fun withoutTypeCheckerExecuteCode(code : String, args: List<Expression.Value>? = null): IValue? {
        return withoutTypeCheckerExecuteCode(mutableListOf("App" to code), args)
    }
    private fun withoutTypeCheckerExecuteCode(code : MutableList<Pair<String, String>>, args: List<Expression.Value>? = null): IValue? {

        val mainFile = ParserManager.loadFromString(code)
        return Evaluator().eval(mainFile,args)?.value
    }

    @Test
    fun simpleTest(){

        val code = """
            
            Int Main(){
                return 5
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(5) ,executeCode(code))

    }

    @Test
    fun simpleTest2(){

        val code = """
            
            Int Main(){
                return 5+5
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code))

    }

    @Test
    fun helloWeltTest(){

        val code = """
            
            Void Main(){
                Println("Hallo Welt")            
            }
            
        """.trimIndent()

        assertEquals(null ,executeCode(code))

    }

    @Test
    fun variableTest(){

        val code = """
            
            Int Main(){
                Int a = 5
                return a + 5
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code))
    }

    @Test
    fun globalVariableTest(){

        val code = """
            Int a = 5
                        
            Int Main(){
                Int b = 5
                return a * b
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(25) ,executeCode(code))
    }

    @Test
    fun whileLoopTest(){

        val code = """
            Int Main()
            {
                Int a = 1
                
                while(a != 14)
                {
                    a = a + 1
                }
                
                return a
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(14), executeCode(code))
    }

    @Test
    fun ifTest(){

        val code = """
            Int Main()
            {
                if(true){
                    return 5
                }else{
                    return 4
                }  
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(5), executeCode(code))
    }

    @Test
    fun simpleFuncTest(){

        val code = """
            
            Int A(){
                return 4*3
            }
            
            Int Main()
            {
                return A()
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(12) ,executeCode(code))
    }

    @Test
    fun funcTest(){

        val code = """
            
            Int A(Int a){
                return a+5
            }
            
            Int Main()
            {
                return A(10)
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(15) ,executeCode(code))
    }

    @Test
    fun advancedFuncTest(){

        val code = """
            
            Int A(Int a){
                return a+5
            }
            
            Int Main()
            {
                return A(A(A(10)))
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(25) ,executeCode(code))
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
                return A(b)
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(5)))))

        val code2 = """
            
            Bool A(Bool a){
                return a
            }
            
            Int A(Int a){
                return a+5
            }
                        
            Int Main(Int b)
            {
                return A(b)
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code2, listOf(Expression.Value(ConstantValue.Integer(5)))))
    }

    @Test
    fun overloadedFunc2Test() {

        val code = """

            class C{
                
                Void C(){
                }
                
                Int A(Int a){
                    return a + 10
                }
                
                Int A(Bool a){
                    if(a){
                        return 100
                    }
                    return 0                  
                }
                
                Int A(String a){
                    if(a == "Hallo"){
                        return 1000
                    }
                    return 0                  
                }
            }

            Int A(Int a){
                return a+1
            }

            Bool A(Bool a){
                return a
            }

            Int Main(Int b)
            {
                C zzz = C()
                return A(b) + zzz.A(b) + zzz.A(true) + zzz.A("Hallo")
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(1111), executeCode(code, listOf(Expression.Value(ConstantValue.Integer(0)))))
    }

    @Test
    fun simpleMainParameterTest(){

        val code = """
                        
            Int Main(Int b)
            {
                return b
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(10)))))
    }

    @Test
    fun mainParameterTest(){

        val code = """
            
            Int A(Int a){
                return a+5
            }
            
            Int Main(Int b)
            {
                return A(b)
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(5)))))
    }


    @Test
    fun mathTest(){

        val code = """
            
            Int Main(Int b)
            {
                return  4 * ((3 * 4) + 4) - (4 * 5 - 20)
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(64) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(5)))))
    }

    @Test
    fun mathTest2(){

        val code = """
            
            Int Main(Int b)
            {
                return  -(4+2) * (-4)
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(24) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(5)))))
    }

    @Test
    fun mathTest3(){

        val code = """            
            Int Main(){
                // 2576
                return -1 *( - (3*5) - ( 4 + ( 5 * (30-43)))) * (-56)  
            }          
        """.trimIndent()

        assertEquals(ConstantValue.Integer(2576), executeCode(code))
    }

    @Test
    fun mathTest4()
    {
        val code = """            
            Int Main()
            {
                // -200 * 30 * 3
                return -(20 * 10) * (10 + 20) * 3 
            }          
        """.trimIndent()

        assertEquals(ConstantValue.Integer(-18000), executeCode(code))
    }

    @Test
    fun boolTest(){

        val code = """            
            Bool Main(){
                return !(!((5 != 6) == true) || !( 6 < 7 || ( true != false)))
            }                 
        """.trimIndent()

        assertEquals(ConstantValue.Boolean(true), executeCode(code))
    }

    @Test
    fun recursionTest(){

        val code = """
            
            Int F(Int n){
                if(n == 1){
                    return n
                }else{
                    if(n == 0){
                        return n
                    }else{
                        return F(n - 1) + F(n - 2)
                    }                   
                }
            }
            
            Int Main(Int b)
            {
                return F(b)
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(34) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(9)))))
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

        assertEquals(ConstantValue.Integer(34) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(9)))))
    }

    @Test
    fun shadowingTest(){

        val code = """
            
            
            Int Main(){
            
                Int a = 45
                
                {
                    Int a = a + 3 
                    return a               
                }
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(48) ,executeCode(code))

    }

    @Test
    fun shadowingTest2(){

        val code = """
            
            
            Int Main(){
            
                Int a = 45
                
                {
                    Int a = 3        
                }
                return a 
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(45) ,executeCode(code))

    }

    @Test
    fun shadowingTest3(){

        val code = """
            
            
            Int Main(){
            
                Int a = 45
                
                {
                    Int a = 3        
                    Int b = 5
                }
                return b 
            }
            
        """.trimIndent()

        assertFailsWith<TypeCheckerVariableNotFoundException> {executeCode(code)}

    }

    @Test
    fun shadowingTest4(){

        val code = """
            
            
            Int Main(){
            
                Int a = 1
                
                {
                    Int a = a + a        
                    Int b = a + 3
                    {
                        Int a = b - 3
                        return a    
                    }
                }
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(2) ,executeCode(code))

    }

    @Test
    fun moduloTest4(){

        val code = """
            // Pow = n^k
            Int Pow(Int n, Int k){
                Int returnValue = 1
                
                if(k == 0){
                    return 1
                }
                
                while(k >= 1){
                    returnValue = returnValue * n
                    k = k - 1
                }
                
                return returnValue           
            }
            
            // ModResult = n % k 
            Int Mod(Int n, Int k){
                while(n - k > 0){
                    n = n - k
                }
                return n
            }
            
            Int Main(){
                return Mod(Pow(5,10),7)
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(2) ,executeCode(code))
    }

    @Test
    fun primeHowManyTest()
    {
        val code = """
         Int Mod(Int n, Int k)
         {
            while(n >= k)
            {
                n = n - k
            }
            return n
        }
            
        Int Main()
        {
            Int x = 2
            Int i = 2
            Bool quitFlag = true
            Int foundPrimes = 0
                  
            while(x <= 1000)
            {                  
                while(i <= x && quitFlag)
                {
                    if((Mod(x,i) == 0) && (x != i))
                    {
                        quitFlag = false // break
                    }
                    else
                    {
                        if(i == x)
                        {
                            foundPrimes = foundPrimes + 1
                            //Println(i)
                        }
                    }     
                                   
                    i = i + 1   
                }
                quitFlag = true
                i = 2
                x = x + 1
            }                  
            return foundPrimes
        }
        
        """.trimIndent()

        assertEquals(ConstantValue.Integer(168) ,executeCode(code))
    }

    @Test
    fun toStringPiTest(){

        val code = """
         String DivToString(Int n, Int k, Int decimalPlaces)
         {
            String returnValue = ""
            Int i = decimalPlaces + 1
            Int tempNumber = n
            
            decimalPlaces = decimalPlaces + 1
                        
            while(i > 0)
            {
                Int counter = 0
                
                if(i != decimalPlaces){
                    tempNumber = tempNumber * 10
                }
                
                //Println(ToString(tempNumber))   
                
                while(tempNumber - k >= 0){
                    counter = counter + 1 
                    tempNumber = tempNumber - k
                }
                
                //Println("Value:" + ToString(counter))
                //Println("Rest:" + ToString(tempNumber))
                //Println("---")
                
                if(i == 1){
                    Int b = counter
                    counter = 0
                    
                    tempNumber = tempNumber * 10
                
                    while(tempNumber - k >= 0){
                        counter = counter + 1 
                        tempNumber = tempNumber - k
                    }
                                        
                    if(counter < 5){
                        returnValue = returnValue + ToString(b)
                    }else{
                        returnValue = returnValue + ToString(b + 1)
                    }
               
                }else{
                    returnValue = returnValue + ToString(counter)
                }
                
                
                if(i == decimalPlaces){
                    returnValue = returnValue + "."
                }
                
                i = i - 1
            }
            return returnValue
        }
            
        String Main(Int dividend, Int divisor)
        {
            Int decimalPlaces = 14
                    
            return DivToString(dividend, divisor, decimalPlaces)
        }
        
        """.trimIndent()

        assertEquals(
            ConstantValue.String("64.00000000000000") ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(1024)),Expression.Value(
                ConstantValue.Integer(16)))))

        assertEquals(
            ConstantValue.String("0.33928571428571") ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(19)),Expression.Value(
                ConstantValue.Integer(56)))))

        assertEquals(
            ConstantValue.String("22.40000000000000") ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(1232)),Expression.Value(
                ConstantValue.Integer(55)))))

        assertEquals(
            ConstantValue.String("183.09195402298851") ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(79645)),Expression.Value(
                ConstantValue.Integer(435)))))

    }

    @Test
    fun floatTest(){
        val code = """
            Float a = 5.56;
                        
            Float Main(){
                Float b = 7.34;
                return ((a * b + 4) + 5.27) * a;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Float(278.44702f) ,executeCode(code))
    }

    @Test
    fun float2Test(){
        val code = """   
            
                             
            Float Main(){
            
                Float a = 0.0
                Int i = 0
                
                while(i != 10){
                    a = a + (0.1 * i)  
                    i = i + 1
                }
                
                return a
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Float(4.5f) ,executeCode(code))

    }

    @Test
    fun float3Test(){
        val code = """   
            
                             
            Bool Main(){
                
                Bool c = true
                
                Float a = 1.0001
                Float b = 1.0000
                
                if(a < b){
                    c = false
                }
                
                if(a == b){
                    c = false
                }
                                
                if(a != b + 0.0001){
                    c = false
                }
                    
                return c
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Boolean(true) ,executeCode(code))

    }

    @Test
    fun float4Test(){
        val code = """   
            
                             
            Int Main(){
            
                Float a = 0.0
                Int i = 0
                
                while(a < 10){
                    i = i + 1  
                    a = a + 0.01
                }
                
                return i
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(1000) ,executeCode(code))

    }


    @Test
    fun bugfix1Test(){
        val code = """   
            
                             
            Int Main(){
                Int a = 2
                return a * 13 + 4
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(30) ,executeCode(code))

        val code2 = """   
            
                             
            Int Main(){
                Int a = 2
                return 2 * 13 + 4
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(30) ,executeCode(code2))
    }

    @Test
    fun bugfix2Test(){
        val code = """   
            Int P(){
                return = 4
            }
                             
            Int Main(){
                Int a = P()
                return a
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(4) ,executeCode(code))
    }

    @Test
    fun bugfix3Test(){
        val code = """                               
            Int Main(){
                Int a = 5
                
                if(a > 6){}
                if(a > 6){}
                while(a == 0){}
                
                return a
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(5) ,executeCode(code))
    }

    @Test
    fun classTest(){
        val code = """   
            
            class OpenGL{     
                Int gg = 69
                String name = gg.ToString()
            
                OpenGL(){
                }
            
                Void A(String name){
                    String name = gg.ToString()
                }
                
                String B(Int a){
                    String c = (" " + a.ToString() + " Hallo " + name)
                    gg = gg + 1
                    
                    return c
                }
                
                Int C(Int a){
                    return a + 3
                }
            }
                             
            String Main(){
                OpenGL b = OpenGL()
                Int r = b.C(5) + 5
                     
                return r.ToString() + " " + b.gg.ToString() + " " + b.name + b.B(57) + " " + b.gg.ToString() 
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.String("13 69 69 57 Hallo 69 70") ,executeCode(code))
    }

    @Test
    fun class2Test(){
        val code = """   
            class A{
                Int aa = 0
            
                A(){
                }
            
                Void B(Int c){
                    aa = aa + c
                }
            
                Int C(Int number){
                    return aa + 3 + number
                }
            }
            
            class OpenGL{     
                A aOBj = A()
                
                Int bb = 4000
                
                Void OpenGL(){
                }
                
                Int B(Int cc){
                    aOBj.aa = aOBj.aa + 10
                    return aOBj.C(cc + 10000)
                }
            }
            
            Int Add10(Int value){
                return value + 10
            }
                             
            Int Main(){
                OpenGL b = OpenGL()
                b.aOBj.B(6)
                return Add10(b.B(100) + b.bb)
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(14129) ,executeCode(code))
    }

    @Test
    fun class3Test(){
        val code = """   
            class A{
                String a = "1"
                Void A(){
                }
            }
            
            class OpenGL{     
                A aOBj = A()
                                
                Void OpenGL(){
                }
                
                Void A(){
                    aOBj.a = "2"
                }
            }
                             
            String Main(){
                OpenGL b = OpenGL()
                String returnValue = b.aOBj.a
                b.A()
                {
                    A c = b.aOBj
                    returnValue = returnValue + b.aOBj.a
                    b.aOBj.a = "3"
                    returnValue = returnValue + c.a
                }
                return returnValue
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.String("123") ,executeCode(code))
    }

    @Test
    fun classConstructorTest(){
        val code = """   
            
            class OpenGL{     
                String name = ""
            
                Void OpenGL(String a){
                    name = a
                }
            }
                             
            String Main(){
                OpenGL b = OpenGL("Hallo")
                                     
                return b.name 
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.String("Hallo") ,executeCode(code))
    }

    @Test
    fun classOverloadingConstructorTest(){
        val code = """   
            
            class OpenGL{     
                String name = ""
            
                Void OpenGL(String a){
                    name = a
                }
                
                Void OpenGL(){
                    Int b = 12
                    + 13
                    name = "Test"
                }
                
                Void OpenGL(Int a){
                    name = a.ToString()
                }
            }
                             
            String Main(){
                OpenGL b = OpenGL("Hallo")
                OpenGL c = OpenGL()
                OpenGL d = OpenGL(15)
                                     
                return b.name + c.name + d.name
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.String("HalloTest15") ,executeCode(code))
    }

    @Test
    fun operationEqualsTest() {
        val code = """
            Float Main(){
                Int b = 5
                Float a = 0.0
                b += 5
                b *= 5
                b -= 5
                a = b + 0.0
                a /= 5
                return a
            }
        """.trimIndent()

        assertEquals(ConstantValue.Float(9f) ,executeCode(code))

    }

    @Test
    fun divisionTest() {
        val code = """
            Float Main(){
                return (6/3) + (5/3) + 0.0
            }
        """.trimIndent()

        assertEquals(ConstantValue.Float((6/3) + (5/3) + 0.0f) ,executeCode(code))

    }

    @Test
    fun division2Test() {
        val code = """
            Float Main(){
                return ((6.0 + 34 * 6) / 3) + (5/3) + 0.6 / 89)
            }
        """.trimIndent()

        assertEquals(ConstantValue.Float(((6.0f + 34 * 6) / 3) + (5/3) + 0.6f / 89 ),executeCode(code))
    }

    @Test
    fun multipleFileTest() {
        val code = mutableListOf<Pair<String,String>>()

        code.add("App" to
            """
                import "B"
                
                Void App(){
                
                }
                
                Int Main(){
                    B b = B();
                    Int a = 5 + b.Bb()
                    App()
                    b.b = 10;
                    return a + b.b
                }
        """.trimIndent())

        code.add("B" to
        """
            import "App"
            import "C"
            
            class B{
            
                C c = C()
            
                Int b = 0
            
                B(){
                }
            
                Int Bb(){
                    return c.GetF();
                }
            }
        """.trimIndent())

        code.add("C" to
        """
            import "D"
            import "F"
            
            class C{
            
                F f = F()
            
                C(){
                }
                
                Int GetF(){
                    return f.f;
                }
            
            
            }
        """.trimIndent())

        code.add("D" to
                """
        """.trimIndent())

        code.add("F" to
            """
            class F{
            
                Int f = 0
            
                F(){
                    f = 12
                }
            
            }
        """.trimIndent())

        assertEquals(ConstantValue.Integer(27 ),executeCode(code))
    }

    @Test
    fun multipleFile2Test() {
        val code = mutableListOf<Pair<String,String>>()

        code.add("App" to
                """
                import "B"
                
                Int Main(){
                    B b = B();
                    B bb = B(5);
                
                    return b.c.c + bb.c.c;
                }
        """.trimIndent())

        code.add("B" to
                """
            import "C"
            
            class B{
                C c = C();
            
                B(){
                }
                
                B(Int a){
                    c = C(a);
                }
            }
        """.trimIndent())

        code.add("C" to
                """            
            class C{ 
                Int c = 10
                C(Int a){
                    c = a
                }
                
                C(){
                }

            }
        """.trimIndent())

        assertEquals(ConstantValue.Integer(15 ),executeCode(code))
    }

    @Test
    fun genericsFunctionTest() {

        val code = """
            String Fun<A>(A a){
                A b = a
                return a.ToString()
            }
            
            String Main()
            {
                String s = ToString(Fun<Int>(5)) + Fun<String>("Hallo")
                return s
            }
        """.trimIndent()

        assertEquals(ConstantValue.String("5Hallo" ),executeCode(code))
    }

    @Test
    fun genericsFunction2Test() {

        val code = """
            A Fun<A>(A a){
                return Fun<A>(a, 5)
            }
            
            B Fun<B>(B a, Int b){
                return a
            }
            
            String Main()
            {
                Int i = Fun<Int>(5)
                String s = Fun<String>("Hallo")
                
                return i.ToString() + s
            }
        """.trimIndent()

        assertEquals(ConstantValue.String("5Hallo" ),executeCode(code))
    }

    @Test
    fun genericsFunction3Test() {

        val code = """
            B Fun<A,B>(A a, B b){
                return Fun<B>(b)
            }

            A Fun<A>(A a){
                return a
            }

            String Main()
            {
                Int i = Fun<Int>(5)
                String s = Fun<Float, String>(12.0, "Hallo")
                
                return i.ToString() + s
            }
        """.trimIndent()

        assertEquals(ConstantValue.String("5Hallo" ),executeCode(code))
    }

    @Test
    fun genericsClassTest() {

        val code = """
            class <T> A{
                
                A(T t){}  
                        
                X B<X>(X a){
                    return a
                }
            }
            
            String Main()
            {
                A objA<String> = A<String>("Hallo")
                String s = ToString(objA.B<Int>(5))
                return s
            }
        """.trimIndent()

        assertEquals(ConstantValue.String("5" ),executeCode(code))
    }

    @Test
    fun genericsClass2Test() {

        val code = """
            
            class <T> List{
                T a = null
                
                List(T b){
                    a = b
                }  
                
            }
            
            Int Main()
            {
                List objA<Int> = List<Int>(3)
                objA.a = 4 + 1
                return objA.a
                
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(5 ),executeCode(code))
    }

    @Test
    fun genericsClass3Test() {

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
                Pair p<Int,String> = Pair<Int,String>(5, "70")
                String a = "A"
               
                a = p.Test<String>(a)
               
                p.x += 5
                p.y = p.x.ToString() + p.y
                return p.GetSecond() + a
            }
        """.trimIndent()

        assertEquals(ConstantValue.String("1070A" ),executeCode(code))
    }

    @Test
    fun genericsClass4Test() {

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
                String a = "A"
               
                a = p.Test<String>(a)
                return a
            }
        """.trimIndent()

        assertEquals(ConstantValue.String("A" ),executeCode(code))
    }

    @Test
    fun genericsClass5Test() {

        val code = """
            class <X,Y,Z> Triple{
                Z z = null
                Pair p<X,Y> = null
                            
                Triple(X xx, Y yy, Z zz){
                    p = Pair<X,Y>(xx, yy)
                    z = zz
                }
                
                X GetFirst(){
                    return p.GetFirst()
                }
                
                Y GetSecond(){
                    return p.GetSecond()
                }
                
                Z GetThird(){
                    return z
                }
                
                String ToString(){
                    return GetFirst().ToString() + GetSecond().ToString() + z.ToString()
                }
            }
           
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
               
            }
            
            String Main()
            {
                Triple p<Int, Float, String> = Triple<Int, Float, String>(5, 4.0, "Test")
                Int x = p.GetFirst()
                Float xx = p.GetSecond()
                String xxx = p.GetThird()
                
                return x.ToString() + xx.ToString() + xxx + p.ToString()
            }
        """.trimIndent()

        assertEquals(ConstantValue.String("54.0Test54.0Test" ), executeCode(code))
    }

    @Test
    fun toStringTest(){
        val code = """   
            class A{
                Int a = 0
                
                A(){}
                
                Int B(){
                    return a
                }
                
                String C(){
                    return B().ToString()
                }
                            
            }              
                             
            String Main(){
                A a = A()
                return a.C()
                
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.String("0") , executeCode(code))
    }

    @Test
    fun toString2Test(){
        val code = """   
            class A{
                Int a = 0
                
                A(){}
               
                String ToString(){
                    return a.ToString()
                }            
            }              
                             
            String Main(){
                A a = A()
                return a.ToString()
                
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.String("0") , executeCode(code))

    }

    @Test
    fun linkedListTest(){
        val code = """   
            class <T> A{
                
                A a<T> = null
                T value = null
                
                A(T tempValue){
                    value = tempValue
                }
                       
                Void Add(T tempValue){
//                    if(a != null){
//                        a.Add(tempValue)
//                    }
                
                    a = A<T>(tempValue)
                }
            }              
                             
            Int Main(){
                A a<Int> = A<Int>(5)
                a.Add(10)
                a.a.Add(100)
                return a.value + a.a.value + a.a.a.value
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(115) , executeCode(code))
    }

    @Test
    fun procedureClassTest(){
        val code = """   
            class A{
                Int a = 0
                
                A(){
                    B()
                }
               
                Void B(){
                    a = 78
                }            
            }              
                             
            Int Main(){
                A a = A()
                return a.a
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(78) , executeCode(code))

    }

    @Test
    fun arrayTest(){

        val code = """   
            include "Array"
            
            Int Main(){
                Array a<Int> = Array<Int>(5)
                a.Set(0,5)
                a.Set(1,6)
                a.Set(2,7)
                a.Set(3,8)
                a.Set(4,9)
                
                return a.Get(0) + a.Get(1) + a.Get(2) + a.Get(3) + a.Get(4) + a.size          
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(40) , executeCode(code))
    }

    @Test
    fun listTest(){

        val listFile =
            "List" to """   
            include "Array"
            
            class <T> List{
                Array values<T> = null
                
                Int addPosition = 0
                Int size = 0
                
                List(Int initialSize){
                    values = Array<T>(initialSize)
                }
            
                Void Add(T value){
                    if(addPosition >= values.size){
                        Array tempValues<T> = Array<T>(size * 2)
                        Int i = values.size - 1
                        
                        while(i >=0){
                            tempValues.Set(i,values.Get(i))
                            i -= 1
                        }
                        
                        values = tempValues
                    }
                   
                    values.Set(addPosition, value)
                    size += 1
                    addPosition += 1
                }
                
                T Get(Int index){
                    values.Get(index)
                }
                
                Void Remove(Int index){
                    values.Set(index, null)
                }
            }
        """.trimIndent()

        val code =
            "App" to """ include "List"
            
            Int Main(){
                List a<String> = List<String>(5)     
                
                a.Add("A")
                a.Add("B")
                a.Add("C")
                a.Add("D")
                                
                                
                return a.values.size
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(20) ,executeCode(mutableListOf(code,listFile)))
    }

    @Test
    fun isNullTest(){

        val code = """   
            
            Int Main(){
                Int a = null  
                Bool b = 5 != null
                
                if(a == null && b){
                    a = 5
                }
                                
                return a    
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(5), executeCode(code))
    }

}