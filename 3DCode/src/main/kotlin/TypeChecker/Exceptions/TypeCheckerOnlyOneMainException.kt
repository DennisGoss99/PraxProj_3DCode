package TypeChecker.Exceptions

class TypeCheckerOnlyOneMainException(lineOfCode : Int) : TypeCheckerBaseException(lineOfCode, "Only one main is allowed to exists")