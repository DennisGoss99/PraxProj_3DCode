package Parser.Exception

class ParserConditionEmpty(lineOfCode: Int, fileName : String) : ParserBaseException(lineOfCode, fileName, "Condition can't be empty. Specify an expression has yield a boolean.")