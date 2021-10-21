package TypeChecker.Exceptions

class TypeCheckerFunctionNotFoundException(lineOfCode : Int, fileName : String,functionName : String) : TypeCheckerBaseException(lineOfCode, fileName, "Couldn't find function: '$functionName'")