package TypeChecker.Exceptions

class TypeCheckerCantAccessPrivate (lineOfCode : Int, fileName : String,typeName : String, name : String) : TypeCheckerBaseException(lineOfCode, fileName, "Can't Access private '$typeName' named: '$name'")