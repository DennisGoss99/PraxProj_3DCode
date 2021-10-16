package TypeChecker.Exceptions

import Parser.ParserToken.*

class TypeCheckerFunctionParameterException :TypeCheckerBaseException {
    constructor(lineOfCode : Int,functionName : String, types: List<Type>?) : super(lineOfCode, "Couldn't find a Function '$functionName' with those types: [$types]")

    constructor(lineOfCode : Int,functionName : String, parameters : Parameter, types: Type) : super(lineOfCode, "Function '$functionName' parameterType doesn't match given parameter [${parameters.type}] [$types]")

    constructor(lineOfCode : Int,message : String) : super(lineOfCode, message)

}