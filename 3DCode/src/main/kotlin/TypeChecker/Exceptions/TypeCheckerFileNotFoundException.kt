package TypeChecker.Exceptions

class TypeCheckerFileNotFoundException(fileName : String) : Exception( "Couldn't find include: in '$fileName'")