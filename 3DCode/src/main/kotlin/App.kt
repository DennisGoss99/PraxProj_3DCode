import Evaluator.Evaluator
import Lexer.Lexer
import Parser.Parser
import Parser.ParserManager
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.IValue
import TypeChecker.TypeChecker
import java.io.File

private fun executeCode(code : String, args: List<Expression.Value>? = null): IValue? {

    val parserOutput = Parser(Lexer(code)).ParsingStart()

    TypeChecker(parserOutput, args).check()

    return Evaluator().eval(parserOutput,args)?.value

}
//
//private fun executePath(path : String, args: List<Expression.Value>? = null): IValue? {

//    val appCode = ParserManager(path).getApp()
//
//    return Evaluator().eval(appCode,args)?.value
//
//}

fun main(){

//    val code = """
//
//        """.trimIndent()
//
//    println(executeCode(code))

      ParserManager("C:/Users/Merdo/Desktop/TEST/App.c3d")
//    println(executePath("C:/Users/Merdo/Desktop/TEST/App.c3d", listOf()))

}