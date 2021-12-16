import Evaluator.Evaluator
import Lexer.Lexer
import Parser.Parser
import Parser.ParserManager
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.IValue
import TypeChecker.TypeChecker
import kotlin.system.measureTimeMillis

private fun executeCode(code : String, args: List<Expression.Value>? = null): IValue? {
    return executeCode(mutableListOf("App" to code), args)
}

private fun executeCode(code : MutableList<Pair<String, String>>, args: List<Expression.Value>? = null): IValue? {

    val mainFile = ParserManager.loadFromString(code)
    TypeChecker().check(mainFile, args)

    return Evaluator().eval(mainFile,args)?.value
}

private fun prime3DCode(limit : Int){

    val code = """
        Int Mod(Int n, Int k)
        {
            while(n >= k){
                n -= k
            }
            return n
        }
            
        Int Main()
        {
            Int x = 2
            Int i = 2
            Bool quitFlag = true
            Int foundPrimes = 0
                  
            while(x <= $limit)
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
                            foundPrimes += 1
                        }
                    }     
                                   
                    i += 1   
                }
                quitFlag = true
                i = 2
                x += 1
            }                  
            return foundPrimes
        }
        """.trimIndent()
    executeCode(code)
    //println()
}
private fun primeKotlin(limit : Int){

        fun Mod( n : Int, k : Int ) : Int
        {
            var nn = n
            while(nn >= k)
                nn -= k
            return nn
        }

            var x : Int = 2
            var i : Int= 2
            var quitFlag : Boolean= true
            var foundPrimes : Int= 0

            while(x <= limit)
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
                            foundPrimes += 1
                        }
                    }

                    i += 1
                }
                quitFlag = true
                i = 2
                x += 1
            }
            //println(foundPrimes)

}

fun main(){

    val testValues = listOf(30000,40000,50000)

    testValues.forEach {
        println("------")
        println("Count  : $it")
        println("Kotlin : ${measureTimeMillis {primeKotlin(it)}} ms")
        println("3D-Code: ${measureTimeMillis {prime3DCode(it)}} ms")
        println("------")
    }

}