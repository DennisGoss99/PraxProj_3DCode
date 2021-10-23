package Parser.Exception

import Lexer.LexerToken

class ParserTokenUnexpected : ParserBaseException
{
    constructor(token : LexerToken, fileName : String) : super(token.LineOfCode, fileName, "Token <$token> was not expected")
    constructor(expectedType : String, next : LexerToken, fileName : String) : super(next.LineOfCode, fileName, "Unexpected token: expected $expectedType, but got $next")

}