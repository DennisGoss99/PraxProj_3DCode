package TypeChecker.Exceptions

class TypeCheckerDuplicateClassException(lineOfCode : Int, fileName : String, className :String, ) : TypeCheckerBaseException(lineOfCode, fileName, "Duplicate Classes aren't allowed. class :'$className'")