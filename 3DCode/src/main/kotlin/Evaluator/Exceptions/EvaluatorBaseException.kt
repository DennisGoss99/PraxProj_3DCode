package Evaluator.Exceptions

open class EvaluatorBaseException (lineOfCode: Int, fileName : String, message: String) : Exception("[Error at 'line:$lineOfCode' in '$fileName'] $message")