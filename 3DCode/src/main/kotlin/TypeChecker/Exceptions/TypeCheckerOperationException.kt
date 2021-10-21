package TypeChecker.Exceptions

import Parser.ParserToken.Operator

class TypeCheckerOperationException(lineOfCode : Int, fileName : String,message : String, operator : Operator) : TypeCheckerBaseException(lineOfCode, fileName, "$message Operator:'$operator'")