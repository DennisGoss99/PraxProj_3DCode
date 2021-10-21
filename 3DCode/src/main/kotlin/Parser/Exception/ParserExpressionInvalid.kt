package Parser.Exception

import Lexer.LexerToken

class ParserExpressionInvalid(val token : LexerToken, fileName : String) : ParserBaseException(token.LineOfCode, fileName, "Expected expression but got <$token>")
{

}