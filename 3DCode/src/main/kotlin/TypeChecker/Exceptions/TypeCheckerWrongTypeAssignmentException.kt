package TypeChecker.Exceptions

import Parser.ParserToken.*

class TypeCheckerWrongTypeAssignmentException(lineOfCode : Int, fileName : String,variableName: String,expectedType: Type?, actualType : Type)
    : TypeCheckerBaseException(lineOfCode, fileName, "Cant assign type '$actualType' to variable '$variableName of Type '$expectedType''")