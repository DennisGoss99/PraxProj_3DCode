package Parser.Exception

import Lexer.LexerToken
import Parser.ParserToken.Declaration

class ParserUnsupportedDeclaration (val invalidDeclaration : Declaration) : ParserBaseException(invalidDeclaration.LineOfCode, "class doesn't support: ${invalidDeclaration::class}")