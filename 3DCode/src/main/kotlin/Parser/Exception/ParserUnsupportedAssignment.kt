package Parser.Exception

import Lexer.LexerToken
import Parser.ParserToken.Declaration

class ParserUnsupportedAssignment (private val invalidToken : LexerToken, fileName : String) : ParserBaseException(invalidToken.LineOfCode, fileName, "Can't assign with : ${invalidToken::class}")