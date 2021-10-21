package TypeChecker.Exceptions

import Parser.ParserToken.Declaration

class TypeCheckerDuplicateFunctionException(lineOfCode : Int, fileName : String, function : Declaration.FunctionDeclare, ) : TypeCheckerBaseException(lineOfCode, fileName, "Duplicate functions with same parameters aren't allowed. function :'${function.functionName}(${function.parameters})'")