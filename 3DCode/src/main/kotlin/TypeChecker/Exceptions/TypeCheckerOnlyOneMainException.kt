package TypeChecker.Exceptions

class TypeCheckerOnlyOneMainException(lineOfCode : Int, fileName : String) : TypeCheckerBaseException(lineOfCode, fileName, "Only one main is allowed to exists")