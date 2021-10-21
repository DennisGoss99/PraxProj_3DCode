package TypeChecker.Exceptions

class TypeCheckerConstructorNotFoundException(lineOfCode : Int, fileName : String,constructorName : String, className : String) : TypeCheckerBaseException(lineOfCode, fileName, "Couldn't find constructor: '$constructorName()' of class '$className'")