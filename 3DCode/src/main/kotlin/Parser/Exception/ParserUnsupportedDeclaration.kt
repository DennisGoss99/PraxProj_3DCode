package Parser.Exception

import Lexer.LexerToken
import Parser.ParserToken.Declaration

class ParserUnsupportedDeclaration (val invalidDeclaration : Declaration, fileName : String) : ParserBaseException(invalidDeclaration.LineOfCode, fileName, "class doesn't support: ${invalidDeclaration::class}")