package Evaluator.Exceptions.NotFound

import Evaluator.Exceptions.EvaluatorBaseException

class FunctionNotFoundRuntimeException(lineOfCode: Int, fileName : String, val functionName : String) : EvaluatorBaseException(lineOfCode, fileName ,"Couldn't find Function: '$functionName'")