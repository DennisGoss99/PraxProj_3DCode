package Parser.Exception

import Lexer.LexerToken

class ParserInvalidName(val token : LexerToken, fileName : String) : ParserBaseException(token.LineOfCode, fileName, "Invalid name. Expected name but got <$token>")
{

}