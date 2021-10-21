package Parser.Exception

import Lexer.LexerToken

class ParserTokenUnexpected(val token : LexerToken, fileName : String) : ParserBaseException(token.LineOfCode, fileName, "Token <$token> was not expected")
{
}