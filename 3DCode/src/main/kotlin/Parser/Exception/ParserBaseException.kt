package Parser.Exception


open class ParserBaseException(lineOfCode: Int, fileName : String, message: String) : Exception("[Error at 'line:$lineOfCode' in '$fileName'] $message")