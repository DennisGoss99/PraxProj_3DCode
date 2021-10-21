package Evaluator.Exceptions.NotFound

import Evaluator.Exceptions.EvaluatorBaseException

class VariableNotFoundRuntimeException (lineOfCode: Int, fileName : String, val variableName : String) : EvaluatorBaseException(lineOfCode, fileName ,"Couldn't find variable: '$variableName'")