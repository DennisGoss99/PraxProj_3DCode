package TypeChecker.Exceptions

import Parser.ParserToken.Type

class TypeCheckerReturnTypeException : TypeCheckerBaseException{
    constructor(lineOfCode : Int,functionName : String, expectedType: Type?, actualType : Type ) : super(lineOfCode, "Function return type '$expectedType' of function: '$functionName' doesn't match Type '$actualType'")
    constructor(lineOfCode : Int,constructorName : String ) : super(lineOfCode, "Constructor '$constructorName' isn't allowed to return something ")
}