package Evaluator.Exceptions

import Parser.ParserToken.Type

class TypeMismatchRuntimeException(lineOfCode: Int, fileName : String, message: String, expectedType : Type) : EvaluatorBaseException(lineOfCode, fileName ,"$message '$expectedType'")