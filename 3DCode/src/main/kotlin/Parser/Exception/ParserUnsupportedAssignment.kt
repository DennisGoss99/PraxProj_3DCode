package Parser.Exception

import Lexer.LexerToken
import Parser.ParserToken.Declaration

class ParserUnsupportedAssignment (private val invalidToken : LexerToken) : ParserBaseException(invalidToken.LineOfCode, "Can't assign with : ${invalidToken::class}")