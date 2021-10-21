package TypeChecker.Exceptions

import Parser.ParserToken.*

class TypeCheckerFunctionParameterException :TypeCheckerBaseException {
    constructor(lineOfCode : Int, fileName : String,functionName : String, types: List<Type>?) : super(lineOfCode, fileName, "Couldn't find a Function '$functionName' with those types: [$types]")

    constructor(lineOfCode : Int, fileName : String,functionName : String, parameters : Parameter, types: Type) : super(lineOfCode, fileName, "Function '$functionName' parameterType doesn't match given parameter [${parameters.type}] [$types]")

    constructor(lineOfCode : Int, fileName : String,message : String) : super(lineOfCode, fileName, message)

}