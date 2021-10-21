package TypeChecker.Exceptions

class TypeCheckerClassNotFoundException (lineOfCode : Int, fileName : String,className : String) : TypeCheckerBaseException(lineOfCode, fileName, "Couldn't find class: '$className'")