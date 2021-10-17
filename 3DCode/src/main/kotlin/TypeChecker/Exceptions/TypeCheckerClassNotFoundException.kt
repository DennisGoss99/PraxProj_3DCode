package TypeChecker.Exceptions

class TypeCheckerClassNotFoundException (lineOfCode : Int,className : String) : TypeCheckerBaseException(lineOfCode, "Couldn't find class: '$className'")