package Evaluator.Exceptions

import Parser.ParserToken.Operator

class OperationRuntimeException(lineOfCode: Int, fileName : String, message : String, operator : Operator) : EvaluatorBaseException(lineOfCode, fileName ,"$message Operator:'$operator'")