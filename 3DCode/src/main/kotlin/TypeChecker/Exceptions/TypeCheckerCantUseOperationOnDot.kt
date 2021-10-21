package TypeChecker.Exceptions

import Parser.ParserToken.Expression

class TypeCheckerCantUseOperationOnDot (lineOfCode : Int, fileName : String, expression: Expression) : TypeCheckerBaseException(lineOfCode, fileName, "Cant use operation:${expression} on .")