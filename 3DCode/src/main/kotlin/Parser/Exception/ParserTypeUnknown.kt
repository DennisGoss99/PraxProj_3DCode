package Parser.Exception

import Lexer.LexerToken

class ParserTypeUnknown(val invalidToken : LexerToken, fileName : String) : ParserBaseException(invalidToken.LineOfCode, fileName, "Unkown type <$invalidToken>")
{
}