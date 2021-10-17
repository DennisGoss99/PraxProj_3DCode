package TypeChecker.Exceptions

class TypeCheckerConstructorNotFoundException(lineOfCode : Int,constructorName : String, className : String) : TypeCheckerBaseException(lineOfCode, "Couldn't find constructor: '$constructorName()' of class '$className'")