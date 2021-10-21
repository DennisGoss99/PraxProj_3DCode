package Parser.Exception

import Lexer.LexerToken

class ParserValueUnknown(val invalidToken : LexerToken, fileName : String) : ParserBaseException(invalidToken.LineOfCode, fileName, "Unkown value (literal). Token:<$invalidToken>")
{
}