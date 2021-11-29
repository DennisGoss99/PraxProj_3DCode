import Evaluator.Evaluator
import Parser.ParserManager
import Parser.ParserToken.Expression
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.IValue
import TypeChecker.TypeChecker
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LibTest {

    private fun executeCode(code : String, args: List<Expression.Value>? = null): IValue? {
        return executeCode(mutableListOf("App" to code), args)
    }

    private fun executeCode(code : MutableList<Pair<String, String>>, args: List<Expression.Value>? = null): IValue? {

        val mainFile = ParserManager.loadFromString(code)
        TypeChecker().check(mainFile, args)

        return Evaluator().eval(mainFile,args)?.value
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
    fun listTest() {
        val codeList = "List" to """
        include "Array"
                    
        class <T> List{
            private Array values<T> = null
            
            Int addPosition = 0
            Int size = 0
            
            List(Int initialSize){
                values = Array<T>(initialSize)
            }
        
            Void Add(T value){
                if(addPosition >= values.size){
                    if(size == 0){
                        values = Array<T>(1)
                    }else{
                        Array tempValues<T> = Array<T>(size  * 2)
                        Int i = values.size - 1
                        
                        while(i >=0){
                            tempValues.Set(i,values.Get(i))
                            i -= 1
                        }
                        
                        values = tempValues
                    }
                }
        
                values.Set(addPosition, value)
                size += 1
                addPosition += 1
            }
            
            Void InsertAt(Int index, T value){
                values.Set(index, value)
            }
            
            T Get(Int index){
                return values.Get(index)
            }
            
            Void Remove(Int index){
                values.Set(index, null)
            }
        }
        """.trimIndent()

        val code = "App" to """
            include "List"
            include "Array"
                        
            List a<Int> = List<Int>(0)
                        
            Int Main(){        
                   
                a.Add(5)
                //Println("Main:" + ToString(a.values.Get(0)))
                a.Add(10)
                a.Add(100)
                a.Add(1000)
                a.InsertAt(2,200)
                   
                return a.Get(0) + a.Get(1) + a.Get(2) + a.Get(3)
            }
        """.trimIndent()

        assertEquals(ConstantValue.Integer(1215), executeCode(mutableListOf(code,codeList)))
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
                    if(a != null){
                        a.Add(tempValue)
                    } else {
                    a = A<T>(tempValue)
                    }
                }
                
                T Get(Int index){
                    if(index == 0){
                        return value
                    }
                    index -= 1
                    return a.Get(index)
                }
                
                Void InsertAt(Int index, T tempValue){
                    if(index == 0){
                        A newA<T> = A<T>(value)
                        newA.a = a
                        value = tempValue
                        a = newA
                    }
                    else
                    {
                    index -= 1
                    a.InsertAt(index, tempValue)
                    }
                }
            }              
                             
            Int Main(){
                A a<Int> = A<Int>(5)
                a.Add(10)
                a.Add(100)
                a.Add(1000)
                a.InsertAt(0,0)
                a.InsertAt(2,1)
                return a.Get(0) + a.Get(1) + a.Get(2) + a.Get(3) + a.Get(4) + a.Get(5)
            }
            
        """.trimIndent()

        assertEquals(ConstantValue.Integer(1116) , executeCode(code))
    }

    @Test
    fun genericsClass5Test() {

        val code = """
            class <A,B,C> Triple{
            
                A x = null
                B y = null
                C z = null
                            
                Triple(A tempX, B tempY, C tempZ){
                    x = tempX
                    y = tempY
                    z = tempZ
                }
                
                A GetFirst(){
                    return x
                }
                
                B GetSecond(){
                    return y
                }
                
                C GetThird(){
                    return z
                }
                
                String ToString(){
                    return x.ToString() + y.ToString() + z.ToString()
                }	
               
            }
           
            class <A,B> Pair{
            
                A x = null
                B y = null
                    
                Pair(A tempX, B tempY){
                    x = tempX
                    y = tempY
                }
                
                A GetFirst(){
                    return x
                }
                
                B GetSecond(){
                    return y
                }
                
                String ToString(){
                    return x.ToString() + y.ToString()
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
    fun mathTest() {
        val codeMath = "Math" to """
                    
        class Math{
            
            Math(){
            }
        
            Int Abs(Int value){
                if(value >= 0){ return value }else{ return value * -1 }
            }
            
            Float Abs(Float value){
                if(value >= 0){ return value }else{ return value * -1.0 }
            }
            
            Float Pow(Float value, Int power){
                Float returnValue = 0.0
                
                if(power == 0){return 1.0}else{
                    if(power > 0){
                        power -= 2
                        returnValue = value
                        while(power >= 0){
                            returnValue *= value
                            power -= 1 
                        }
                    }else{
                        returnValue = value
                        while(power <= 0){
                            returnValue /= value
                            power += 1 
                        }
                    }
                }
                return returnValue
            }
            
            Float Pow(Int value, Int power){
                return Pow(value + 0.0, power)
            }
            
            Int Mod(Int value, Int divider){
                if(divider == 0){
                    return value
                }
                while(value >= divider){
                    value -= divider
                }
                return value
            }
            
            Float Mod(Float value, Float divider){
                if(divider == 0){
                    return value
                }
                while(value >= divider){
                    value -= divider
                }
                return value
            }
        
        }
        """.trimIndent()

        val codeAbs = "App" to """
            include "Math" 
            Math m = Math()
                        
            Float Main(){        
                return m.Abs(5) + m.Abs(-5) + m.Abs(5.0) + m.Abs(-5.0)
            }
        """.trimIndent()

        val codePow = "App" to """
            include "Math" 
            Math m = Math()
                        
            Float Main(){    
                return m.Pow(4,0) + m.Pow(2,4) + m.Pow(2,1) + m.Pow(2,-4)
            }
        """.trimIndent()

        val codeMod = "App" to """
            include "Math" 
            Math m = Math()
            
            Float Main(){               
                return m.Mod(4,0) + m.Mod(4,3) + m.Mod(8,3) + m.Mod(3213.312, 21.34)
            }
        """.trimIndent()

        assertEquals(ConstantValue.Float(20f), executeCode(mutableListOf(codeAbs,codeMath)))
        assertEquals(ConstantValue.Float(19.0625f), executeCode(mutableListOf(codePow,codeMath)))
        assertEquals(ConstantValue.Float(19.308338f), executeCode(mutableListOf(codeMod,codeMath)))
    }

}