import Evaluator.Evaluator
import Lexer.Lexer
import Parser.Parser
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

        val parserOutput = Parser(Lexer(code)).ParsingStart()

        TypeChecker(parserOutput, args).check()

        return Evaluator().eval(parserOutput,args)?.value

    }

    private fun withoutTypeCheckerExecuteCode(code : String, args: List<Expression.Value>? = null): IValue? {

        val parserOutput = Parser(Lexer(code)).ParsingStart()
        return Evaluator().eval(parserOutput,args)?.value
    }

    @Test
    fun simpleTest(){

        val code = """
            
            int Main(){
                return 5;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(5) ,executeCode(code))

    }

    @Test
    fun simpleTest2(){

        val code = """
            
            int Main(){
                return 5+5;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code))

    }

    @Test
    fun helloWeltTest(){

        val code = """
            
            void Main(){
                Println("Hallo Welt");            
            }
            
        """.trimIndent()

        assertEquals(null ,executeCode(code))

    }

    @Test
    fun variableTest(){

        val code = """
            
            int Main(){
                int §a = 5;
                return §a + 5;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code))
    }

    @Test
    fun globalVariableTest(){

        val code = """
            int §a = 5;
                        
            int Main(){
                int §b = 5;
                return §a * §b;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(25) ,executeCode(code))
    }

    @Test
    fun whileLoopTest(){

        val code = """
            int Main()
            {
                int §a = 1;
                
                while(§a != 14)
                {
                    §a = §a + 1;
                }
                
                return §a;
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(14), executeCode(code))
    }

    @Test
    fun ifTest(){

        val code = """
            int Main()
            {
                if(true){
                    return 5;
                }else{
                    return 4;
                }  
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(5), executeCode(code))
    }

    @Test
    fun simpleFuncTest(){

        val code = """
            
            int A(){
                return 4*3;
            }
            
            int Main()
            {
                return A();
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(12) ,executeCode(code))
    }

    @Test
    fun funcTest(){

        val code = """
            
            int A(int §a){
                return §a+5;
            }
            
            int Main()
            {
                return A(10);
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(15) ,executeCode(code))
    }

    @Test
    fun advancedFuncTest(){

        val code = """
            
            int A(int §a){
                return §a+5;
            }
            
            int Main()
            {
                return A(A(A(10)));
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(25) ,executeCode(code))
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
                return A(§b);
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(5)))))

        val code2 = """
            
            bool A(bool §a){
                return §a;
            }
            
            int A(int §a){
                return §a+5;
            }
                        
            int Main(int §b)
            {
                return A(§b);
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code2, listOf(Expression.Value(ConstantValue.Integer(5)))))
    }

    @Test
    fun overloadedFunc2Test() {

        val code = """

            class C{
            
                int A(int §a){
                    return §a + 10;
                }
                
                int A(bool §a){
                    if(§a){
                        return 100;
                    }
                    return 0;                  
                }
                
                int A(string §a){
                    if(§a == "Hallo"){
                        return 1000;
                    }
                    return 0;                  
                }
            }

            int A(int §a){
                return §a+1;
            }

            bool A(bool §a){
                return §a;
            }

            int Main(int §b)
            {
                c §zzz = C();
                return A(§b) + §zzz.A(§b) + §zzz.A(true) + §zzz.A("Hallo");
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(1111), withoutTypeCheckerExecuteCode(code, listOf(Expression.Value(ConstantValue.Integer(0)))))
    }

    @Test
    fun simpleMainParameterTest(){

        val code = """
                        
            int Main(int §b)
            {
                return §b;
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(10)))))
    }

    @Test
    fun mainParameterTest(){

        val code = """
            
            int A(int §a){
                return §a+5;
            }
            
            int Main(int §b)
            {
                return A(§b);
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(10) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(5)))))
    }


    @Test
    fun mathTest(){

        val code = """
            
            int Main(int §b)
            {
                return  4 * ((3 * 4) + 4) - (4 * 5 - 20);
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(64) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(5)))))
    }

    @Test
    fun mathTest2(){

        val code = """
            
            int Main(int §b)
            {
                return  -(4+2) * (-4);
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(24) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(5)))))
    }

    @Test
    fun mathTest3(){

        val code = """            
            int Main(){
                // 2576
                return -1 *( - (3*5) - ( 4 + ( 5 * (30-43)))) * (-56)  ;
            }          
        """.trimIndent()

        assertEquals(ConstantValue.Integer(2576), executeCode(code))
    }

    @Test
    fun mathTest4()
    {
        val code = """            
            int Main()
            {
                // -200 * 30 * 3
                return -(20 * 10) * (10 + 20) * 3 ;
            }          
        """.trimIndent()

        assertEquals(ConstantValue.Integer(-18000), executeCode(code))
    }

    @Test
    fun boolTest(){

        val code = """            
            bool Main(){
                return !(!((5 != 6) == true) || !( 6 < 7 || ( true != false)));
            }                 
        """.trimIndent()

        assertEquals(ConstantValue.Boolean(true), executeCode(code))
    }

    @Test
    fun recursionTest(){

        val code = """
            
            int F(int §n){
                if(§n == 1){
                    return §n;
                }else{
                    if(§n == 0){
                        return §n;
                    }else{
                        return F(§n - 1) + F(§n - 2);
                    }                   
                }
            }
            
            int Main(int §b)
            {
                return F(§b);
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(34) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(9)))))
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

        assertEquals(ConstantValue.Integer(34) ,executeCode(code, listOf(Expression.Value(ConstantValue.Integer(9)))))
    }

    @Test
    fun shadowingTest(){

        val code = """
            
            
            int Main(){
            
                int §a = 45;
                
                {
                    int §a = §a + 3 
                    return §a;               
                }
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(48) ,executeCode(code))

    }

    @Test
    fun shadowingTest2(){

        val code = """
            
            
            int Main(){
            
                int §a = 45;
                
                {
                    int §a = 3;        
                }
                return §a; 
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(45) ,executeCode(code))

    }

    @Test
    fun shadowingTest3(){

        val code = """
            
            
            int Main(){
            
                int §a = 45;
                
                {
                    int §a = 3;        
                    int §b = 5;
                }
                return §b; 
            }
            
        """.trimIndent()

        assertFailsWith<TypeCheckerVariableNotFoundException> {executeCode(code)}

    }

    @Test
    fun shadowingTest4(){

        val code = """
            
            
            int Main(){
            
                int §a = 1;
                
                {
                    int §a = §a + §a;        
                    int §b = §a + 3;
                    {
                        int §a = §b - 3;
                        return §a;    
                    }
                }
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(2) ,executeCode(code))

    }

    @Test
    fun moduloTest4(){

        val code = """
            // Pow = n^k;
            int Pow(int §n, int §k){
                int §returnValue = 1;
                
                if(§k == 0){
                    return 1;
                }
                
                while(§k >= 1){
                    §returnValue = §returnValue * §n;
                    §k = §k - 1;
                }
                
                return §returnValue;           
            }
            
            // ModResult = n % k; 
            int Mod(int §n, int §k){
                while(§n - §k > 0){
                    §n = §n - §k;
                }
                return §n;
            }
            
            int Main(){
                return Mod(Pow(5,10),7);
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(2) ,executeCode(code))
    }

    @Test
    fun primeHowManyTest()
    {
        val code = """
         int Mod(int §n, int §k)
         {
            while(§n >= §k)
            {
                §n = §n - §k;
            }
            return §n;
        }
            
        int Main()
        {
            int §x = 2;
            int §i = 2;
            bool §quitFlag = true;
            int §foundPrimes = 0;
                  
            while(§x <= 1000)
            {                  
                while(§i <= §x && §quitFlag)
                {
                    if((Mod(§x,§i) == 0) && (§x != §i))
                    {
                        §quitFlag = false; // break
                    }
                    else
                    {
                        if(§i == §x)
                        {
                            §foundPrimes = §foundPrimes + 1;
                            //Println(§i);
                        }
                    }     
                                   
                    §i = §i + 1;   
                }
                §quitFlag = true;
                §i = 2;
                §x = §x + 1;
            }                  
            return §foundPrimes;
        }
        
        """.trimIndent()

        assertEquals(ConstantValue.Integer(168) ,executeCode(code))
    }

    @Test
    fun toStringPiTest(){

        val code = """
         string DivToString(int §n, int §k, int §decimalPlaces)
         {
            string §returnValue = "";
            int §i = §decimalPlaces + 1;
            int §tempNumber = §n;
            
            §decimalPlaces = §decimalPlaces + 1;
                        
            while(§i > 0)
            {
                int §counter = 0;
                
                if(§i != §decimalPlaces){
                    §tempNumber = §tempNumber * 10;
                }
                
                //Println(ToString(§tempNumber));   
                
                while(§tempNumber - §k >= 0){
                    §counter = §counter + 1; 
                    §tempNumber = §tempNumber - §k;
                }
                
                //Println("Value:" + ToString(§counter));
                //Println("Rest:" + ToString(§tempNumber));
                //Println("---");
                
                if(§i == 1){
                    int §b = §counter;
                    §counter = 0;
                    
                    §tempNumber = §tempNumber * 10;
                
                    while(§tempNumber - §k >= 0){
                        §counter = §counter + 1; 
                        §tempNumber = §tempNumber - §k;
                    }
                                        
                    if(§counter < 5){
                        §returnValue = §returnValue + ToString(§b);
                    }else{
                        §returnValue = §returnValue + ToString(§b + 1);
                    }
               
                }else{
                    §returnValue = §returnValue + ToString(§counter);
                }
                
                
                if(§i == §decimalPlaces){
                    §returnValue = §returnValue + ".";
                }
                
                §i = §i - 1;
            }
            return §returnValue;
        }
            
        string Main(int §dividend, int §divisor)
        {
            int §decimalPlaces = 14;
                    
            return DivToString(§dividend, §divisor, §decimalPlaces);
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
            float §a = 5.56;
                        
            float Main(){
                float §b = 7.34;
                return ((§a * §b + 4) + 5.27) * §a;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Float(278.44702f) ,executeCode(code))
    }

    @Test
    fun float2Test(){
        val code = """   
            
                             
            float Main(){
            
                float §a = 0.0;
                int §i = 0;
                
                while(§i != 10){
                    §a = §a + (0.1 * §i);  
                    §i = §i + 1;
                }
                
                return §a;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Float(4.5f) ,executeCode(code))

    }

    @Test
    fun float3Test(){
        val code = """   
            
                             
            bool Main(){
                
                bool §c = true;
                
                float §a = 1.0001;
                float §b = 1.0000;
                
                if(§a < §b){
                    §c = false;
                }
                
                if(§a == §b){
                    §c = false;
                }
                                
                if(§a != §b + 0.0001){
                    §c = false;
                }
                    
                return §c;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Boolean(true) ,executeCode(code))

    }

    @Test
    fun float4Test(){
        val code = """   
            
                             
            int Main(){
            
                float §a = 0.0;
                int §i = 0;
                
                while(§a < 10){
                    §i = §i + 1;  
                    §a = §a + 0.01;
                }
                
                return §i;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(1000) ,executeCode(code))

    }


    @Test
    fun bugfix1Test(){
        val code = """   
            
                             
            int Main(){
                int §a = 2;
                return §a * 13 + 4;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(30) ,executeCode(code))

        val code2 = """   
            
                             
            int Main(){
                int §a = 2;
                return 2 * 13 + 4;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(30) ,executeCode(code2))
    }

    @Test
    fun bugfix2Test(){
        val code = """   
            int P(){
                return = 4;
            }
                             
            int Main(){
                int §a = P();
                return §a;
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(4) ,executeCode(code))
    }

    @Test
    fun classTest(){
        val code = """   
            
            class OpenGL{     
                int §GG = 69;
                string §name = §GG.ToString();
            
                void A(string §name){
                    string §name = §GG.ToString();
                }
                
                string B(int §a){
                    string §c = (" " + §a.ToString() + " Hallo " + §name);
                    §GG = §GG + 1;
                    
                    return §c;
                }
                
                int C(int §a){
                    return §a + 3;
                }
            }
                             
            string Main(){
                openGL §b = OpenGL();
                int §r = §b.C(5) + 5;
                     
                return §r.ToString() + " " + §b.§GG.ToString() + " " + §b.§name + §b.B(57) + " " + §b.§GG.ToString(); 
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.String("13 69 69 57 Hallo 69 70") ,withoutTypeCheckerExecuteCode(code))
    }

    @Test
    fun class2Test(){
        val code = """   
            class A{
                int §a = 0;
            
                void B(int §c){
                    §a = §a + §c
                }
            
                int A(int §number){
                    return §a + 3 + §number;
                }
            }
            
            class OpenGL{     
                a §aOBj = A();
                
                int §b = 4000;
                
                int B(int §cc){
                    §aOBj.§a = §aOBj.§a + 10;
                    return §aOBj.A(§cc + 10000);
                }
            }
            
            int Add10(int §value){
                return §value + 10;
            }
                             
            string Main(){
                openGL §b = OpenGL();
                §b.§aOBj.B(6);
                return Add10(§b.B(100) + §b.§b);
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(14129) ,withoutTypeCheckerExecuteCode(code))
    }

}