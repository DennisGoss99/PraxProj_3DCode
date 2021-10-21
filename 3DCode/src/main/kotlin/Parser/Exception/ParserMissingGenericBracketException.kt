package Parser.Exception

class ParserMissingGenericBracketException(lineOfCode: Int, fileName : String) : ParserBaseException(lineOfCode, fileName, "Missing generic closing symbol '>'")