package TypeChecker.Exceptions

import Parser.ParserToken.Expression

class TypeCheckerCantUseOperationOnDot (lineOfCode : Int, expression: Expression) : TypeCheckerBaseException(lineOfCode, "Cant use operation:${expression} on .")