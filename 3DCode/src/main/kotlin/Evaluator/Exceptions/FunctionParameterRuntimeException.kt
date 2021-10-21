package Evaluator.Exceptions

import Parser.ParserToken.Parameter


class FunctionParameterRuntimeException(lineOfCode: Int, fileName : String, val functionName : String, val parameters : List<Parameter>?) : EvaluatorBaseException(lineOfCode, fileName ,"Function '$functionName' has to many or few parameter: [$parameters]")