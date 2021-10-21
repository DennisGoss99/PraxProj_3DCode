package TypeChecker.Exceptions

import Parser.ParserToken.Type

class TypeCheckerConditionException(lineOfCode : Int, fileName : String,wrongType : Type) : TypeCheckerBaseException(lineOfCode, fileName, "Can't use type '$wrongType' in condition")