package Lexer.Exceptions

open class LexerBaseException(lineOfCode: Int, fileName : String, message: String) : Exception("[Error at 'line:$lineOfCode' in '$fileName'] $message")