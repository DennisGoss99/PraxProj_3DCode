package TypeChecker.Exceptions

import Parser.ParserToken.Declaration

class TypeCheckerGenericsMissingException(lineOfCode : Int, fileName : String, functionTypeName : String) : TypeCheckerBaseException(lineOfCode, fileName, "$functionTypeName call is missing generics. Maybe add '<Type, ...>'")