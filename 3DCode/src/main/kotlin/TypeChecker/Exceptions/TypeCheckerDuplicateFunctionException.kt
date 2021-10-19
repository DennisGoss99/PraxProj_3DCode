package TypeChecker.Exceptions

import Parser.ParserToken.Declaration

class TypeCheckerDuplicateFunctionException(lineOfCode : Int, function : Declaration.FunctionDeclare, ) : TypeCheckerBaseException(lineOfCode, "Duplicate functions with same parameters aren't allowed. function :'${function.functionName}(${function.parameters})'")