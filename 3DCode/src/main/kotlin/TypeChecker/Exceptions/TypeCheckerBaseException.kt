package TypeChecker.Exceptions

open class TypeCheckerBaseException(lineOfCode: Int, fileName : String, message: String) : Exception("[Error at 'line:$lineOfCode' in '$fileName'] $message")