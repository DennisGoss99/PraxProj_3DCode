package TypeChecker.Exceptions

import Parser.ParserToken.*

class TypeCheckerOperatorTypeException: TypeCheckerBaseException{

    constructor(lineOfCode : Int, fileName : String,operator : Operator, type: Type) : super(lineOfCode, fileName, "Can't use type '$type' with operator '$operator'")

    constructor(lineOfCode : Int, fileName : String,operator : Operator, typeA: Type, typeB: Type) : super(lineOfCode, fileName, "Can't use type '$typeA' and '$typeB' with operator '$operator'")

}