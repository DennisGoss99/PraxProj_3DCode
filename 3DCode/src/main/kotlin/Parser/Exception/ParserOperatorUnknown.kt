package Parser.Exception

import Lexer.LexerToken

class ParserOperatorUnknown(val token : LexerToken, fileName : String) : ParserBaseException(token.LineOfCode, fileName, "Operator unkown. <$token>")