package TypeChecker.Exceptions

class TypeCheckerVariableNotFoundException (lineOfCode : Int, fileName : String,variableName : String) : TypeCheckerBaseException(lineOfCode, fileName, "Couldn't find variable: '$variableName'")