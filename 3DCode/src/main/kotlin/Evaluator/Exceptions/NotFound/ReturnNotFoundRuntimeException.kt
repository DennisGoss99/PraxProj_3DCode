package Evaluator.Exceptions.NotFound

import Evaluator.Exceptions.EvaluatorBaseException

class ReturnNotFoundRuntimeException(lineOfCode: Int, fileName : String, val functionName : String) : EvaluatorBaseException(lineOfCode, fileName ,"Couldn't find return statement in function: '$functionName'")