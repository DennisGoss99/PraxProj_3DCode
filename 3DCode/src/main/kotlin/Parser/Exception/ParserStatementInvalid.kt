package Parser.Exception

import Lexer.LexerToken

class ParserStatementInvalid(val token : LexerToken, fileName : String) : ParserBaseException(token.LineOfCode, fileName, "Invalid statement. <$token>")
{
}