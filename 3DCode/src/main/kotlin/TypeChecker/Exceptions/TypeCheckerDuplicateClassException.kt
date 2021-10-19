package TypeChecker.Exceptions

class TypeCheckerDuplicateClassException(lineOfCode : Int, className :String, ) : TypeCheckerBaseException(lineOfCode, "Duplicate Classes aren't allowed. class :'$className'")