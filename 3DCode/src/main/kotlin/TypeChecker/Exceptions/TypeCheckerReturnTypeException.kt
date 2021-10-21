package TypeChecker.Exceptions

import Parser.ParserToken.Type

class TypeCheckerReturnTypeException : TypeCheckerBaseException{
    constructor(lineOfCode : Int, fileName : String,functionName : String, expectedType: Type?, actualType : Type ) : super(lineOfCode, fileName, "Function return type '$expectedType' of function: '$functionName' doesn't match Type '$actualType'")
    constructor(lineOfCode : Int, fileName : String,constructorName : String ) : super(lineOfCode, fileName, "Constructor '$constructorName' isn't allowed to return something ")
}