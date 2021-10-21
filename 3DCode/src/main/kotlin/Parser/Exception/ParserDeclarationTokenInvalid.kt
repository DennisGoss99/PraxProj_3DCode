package Parser.Exception

import Lexer.LexerToken

class ParserDeclarationTokenInvalid(val invalidToken : LexerToken, fileName : String) : ParserBaseException(invalidToken.LineOfCode, fileName, "Invalid declaration. Token:<$invalidToken>")
{
}