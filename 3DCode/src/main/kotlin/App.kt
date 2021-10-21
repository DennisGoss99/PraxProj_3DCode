import Evaluator.Evaluator
import Lexer.Lexer
import Parser.Parser
import Parser.ParserManager
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.IValue
import TypeChecker.TypeChecker

private fun executeCode(file: File, args: List<Expression.Value>? = null): IValue? {


    TypeChecker().check(file, args)

    return Evaluator().eval(file,args)?.value
}

fun main(){

//    val code = """
//
//        """.trimIndent()
//
//    println(executeCode(code))

    println(executeCode(ParserManager.loadFromDisk("C:/Users/Merdo/Desktop/TEST/App.c3d"), listOf()))

}