package Parser.Exception

import Lexer.LexerToken

class ParserTypeLowerCase(val invalidToken : LexerToken.NameIdent)  : ParserBaseException(invalidToken.LineOfCode, "Types must be upper case. Maybe [${invalidToken.identify}-> ${invalidToken.identify.capitalize()}]")