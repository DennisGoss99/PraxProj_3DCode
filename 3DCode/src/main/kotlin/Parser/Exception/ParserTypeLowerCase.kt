package Parser.Exception

import Lexer.LexerToken

class ParserTypeLowerCase(val invalidToken : LexerToken.NameIdent, fileName : String)  : ParserBaseException(invalidToken.LineOfCode, fileName, "Types must be upper case. Maybe [${invalidToken.identify}-> ${invalidToken.identify.capitalize()}]")