package TypeChecker.Exceptions

import Parser.ParserToken.*

class TypeCheckerFunctionParameterException :TypeCheckerBaseException {
    constructor(lineOfCode : Int, fileName : String,functionName : String, types: List<Type>?, functionTypeName : String) : super(lineOfCode, fileName, "Couldn't find a $functionTypeName '$functionName' with those types: [$types]")

    constructor(lineOfCode : Int, fileName : String, functionName : String, parameters : Parameter, functionTypeName : String, types: Type) : super(lineOfCode, fileName, "$functionTypeName '$functionName' parameterType doesn't match given parameter [${parameters.type}] [$types]")

    constructor(lineOfCode : Int, fileName : String, message : String) : super(lineOfCode, fileName, message)

}