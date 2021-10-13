package Evaluator

import Evaluator.Exceptions.*
import Evaluator.Exceptions.NotFound.*
import Parser.ParserToken.*

class Evaluator {

    private val functionDeclarations = HashMap<String, Declaration.FunctionDeclare>()
    private val globalEnvironment = HashMap<String, Expression.Value>()

    fun eval( declarations: List<Declaration>, args : List<Expression.Value>? = null) : Expression.Value? {

        declarations.forEach { d ->
            when(d){
                is Declaration.FunctionDeclare -> functionDeclarations[d.functionName] = d
                is Declaration.VariableDeclaration -> globalEnvironment[d.name] = evalExpression(d.expression, globalEnvironment)
            }
        }
        val mainFunction = functionDeclarations["Main"] ?: throw FunctionNotFoundRuntimeException("Main")

        return evalFunction(mainFunction,args)
    }

    private fun evalFunction(function : Declaration.FunctionDeclare, parameter: List<Expression.Value>?) : Expression.Value? {

        if(function.parameters?.size != parameter?.size)
            throw FunctionParameterRuntimeException(function.functionName,function.parameters)

        val localEnvironment = HashMap<String, Expression.Value>()
        function.parameters?.zip(parameter.orEmpty()){ fp, p -> fp.name to p }?.associateTo(localEnvironment){it.first to it.second} ?: HashMap<String, Expression.Value>()

        if(function.returnType == Type.Void){
            evalBody(function.body, localEnvironment)
            return null
        }

        return evalBody(function.body, localEnvironment) ?: throw ReturnNotFoundRuntimeException(function.functionName)
    }

    private fun evalProcedure(procedure : Declaration.FunctionDeclare, parameter: List<Expression.Value>?) {

        if(procedure.parameters?.size != parameter?.size)
            throw FunctionParameterRuntimeException(procedure.functionName,procedure.parameters)

        val localEnvironment = HashMap<String, Expression.Value>()
        procedure.parameters?.zip(parameter.orEmpty()){ fp, p -> fp.name to p }?.associateTo(localEnvironment){it.first to it.second} ?: HashMap<String, Expression.Value>()

        evalBody(procedure.body, localEnvironment)
    }

    private fun evalBody(body: Body, environment: HashMap<String, Expression.Value>) : Expression.Value? {

        val localEnvironment = HashMap<String, Expression.Value>()
        val shadowMap = mutableListOf<String>()

        body.localVariables?.forEach { variable ->
            if(localEnvironment.containsKey(variable.name))
                throw Exception("Variable can't be initialized twice '${variable.name}'")

            localEnvironment[variable.name] = evalExpression(variable.expression, combineEnvironments(environment,localEnvironment))
        }

        environment.forEach{ v->
            if(localEnvironment.containsKey(v.key))
                shadowMap.add(v.key)
            else
                localEnvironment[v.key] = v.value
        }

        body.functionBody.forEach { statement ->
            when(statement){
                is Statement.AssignValue ->{
                    when(statement.variableName){
                        "return" -> {
                            changeEnvironment(environment, localEnvironment, shadowMap)
                            return evalExpression(statement.expression, localEnvironment)
                        }
                        else -> {
                             when {
                                 localEnvironment.containsKey(statement.variableName) -> localEnvironment[statement.variableName] = evalExpression(statement.expression, localEnvironment)
                                globalEnvironment.containsKey(statement.variableName) -> globalEnvironment[statement.variableName] = evalExpression(statement.expression, localEnvironment)
                                else -> throw VariableNotFoundRuntimeException(statement.variableName)
                            }
                        }
                    }
                }
                is Statement.If -> {
                    val condition = evalExpression(statement.condition, localEnvironment).value as? ConstantValue.Boolean
                        ?: throw TypeMismatchRuntimeException("If condition must be of type", Type.Boolean)
                    if(condition.value){
                        evalBody(statement.ifBody, localEnvironment)?.let {
                            changeEnvironment(environment, localEnvironment, shadowMap)
                            return it }
                    }else{
                        statement.elseBody?.let { evalBody(statement.elseBody, localEnvironment)?.let {
                            changeEnvironment(environment, localEnvironment, shadowMap)
                            return it } }
                    }
                }
                is Statement.While -> {
                    while ((evalExpression(statement.condition, localEnvironment).value as? ConstantValue.Boolean)?.value ?: throw TypeMismatchRuntimeException("While condition must be of type", Type.Boolean))
                    {
                        evalBody(statement.body, localEnvironment)?.let {
                            changeEnvironment(environment, localEnvironment, shadowMap)
                            return it }
                    }
                }
                is Statement.ProcedureCall ->{
                    when(statement.procedureName){
                        "Println" -> statement.parameterList?.map { evalExpression(it,localEnvironment) }?.forEach { p -> println(p.value.getValueAsString())}
                        "Print" -> {
                            statement.parameterList?.map { evalExpression(it,localEnvironment) }?.forEach { p -> print(p.value.getValueAsString())}
                        }
                        else -> {
                            val procedure = functionDeclarations[statement.procedureName] ?: throw FunctionNotFoundRuntimeException(statement.procedureName)
                            evalProcedure(procedure, statement.parameterList?.map { evalExpression(it,localEnvironment) })
                        }
                    }
                }
                is Statement.Block -> {
                    evalBody(statement.body,localEnvironment)?.let {
                        changeEnvironment(environment, localEnvironment, shadowMap)
                        return it }
                }
            }
        }

        changeEnvironment(environment, localEnvironment, shadowMap)
        return null
    }

    private fun combineEnvironments(upperEnvironment : HashMap<String, Expression.Value>,lowerEnvironment : HashMap<String, Expression.Value>) : HashMap<String, Expression.Value>{
        return HashMap((upperEnvironment.keys + lowerEnvironment.keys).associateWith { k -> lowerEnvironment[k] ?: upperEnvironment[k] ?: throw VariableNotFoundRuntimeException(k) })
    }

    private fun changeEnvironment(environment : HashMap<String, Expression.Value>, localEnvironment : HashMap<String, Expression.Value>,shadowMap : List<String>){
        environment.forEach{ entry ->
            if(!shadowMap.contains(entry.key))
                environment[entry.key] = localEnvironment[entry.key] ?: throw Exception("Shouldn't occur")
        }
    }


    private fun evalExpression(expression: Expression, environment : HashMap<String, Expression.Value> ) : Expression.Value{
        return when(expression){
            is Expression.Value -> expression
            is Expression.UseVariable ->{
                environment.getOrDefault(expression.variableName,null) ?: globalEnvironment.getOrDefault(expression.variableName,null) ?: throw VariableNotFoundRuntimeException(expression.variableName)
            }
            is Expression.Operation -> {
                if(expression.expressionB == null){
                    return when(expression.operator){
                        Operator.Not -> Expression.Value(ConstantValue.Boolean(!(evalExpression(expression.expressionA, environment).value as?  ConstantValue.Boolean ?: throw TypeMismatchRuntimeException("This type can't be negated",Type.Boolean)).value))
                        Operator.Minus-> negateNumber(evalExpression(expression.expressionA, environment))
                        else -> throw OperationRuntimeException("Needs more then one Argument", expression.operator)
                    }
                }
                else{
                    val v1 = evalExpression(expression.expressionA,environment).value.value
                    val v2 = evalExpression(expression.expressionB,environment).value.value

                    return when(expression.operator){
                        Operator.DoubleEquals -> Expression.Value(ConstantValue.Boolean(v1 == v2))
                        Operator.Plus -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Integer(v1 + v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Float(numberToFloat(v1) + numberToFloat(v2)))
                            v1 is String && v2 is String -> Expression.Value(ConstantValue.String(v1 + v2))
                            else -> throw Exception("Can't use add operation on [${v1::class} + ${v2::class}]")
                        }
                        Operator.Minus -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Integer(v1 - v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Float(numberToFloat(v1) - numberToFloat(v2)))
                            else -> throw Exception("Can't use subtract operation on [${v1::class} - ${v2::class}]")
                        }
                        Operator.Multiply -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Integer(v1 * v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Float(numberToFloat(v1) * numberToFloat(v2)))
                            else -> throw Exception("Can't use multiply operation on [${v1::class} * ${v2::class}]")
                        }
                        Operator.And -> evalBinaryBoolean(v1,v2){x,y -> x&&y}
                        Operator.Or -> evalBinaryBoolean(v1,v2){x,y -> x||y}
                        Operator.NotEqual -> Expression.Value(ConstantValue.Boolean(v1 != v2))
                        Operator.Less -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Boolean(v1 < v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Boolean(numberToFloat(v1) < numberToFloat(v2)))
                            else -> throw Exception("Can't use less operation on [${v1::class} < ${v2::class}]")
                        }
                        Operator.LessEqual -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Boolean(v1 <= v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Boolean(numberToFloat(v1) <= numberToFloat(v2)))
                            else -> throw Exception("Can't use lessEqual operation on [${v1::class} <= ${v2::class}]")
                        }
                        Operator.Greater -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Boolean(v1 > v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Boolean(numberToFloat(v1) > numberToFloat(v2)))
                            else -> throw Exception("Can't use less operation on [${v1::class} > ${v2::class}]")
                        }
                        Operator.GreaterEquals -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Boolean(v1 >= v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Boolean(numberToFloat(v1) >= numberToFloat(v2)))
                            else -> throw Exception("Can't use lessEqual operation on [${v1::class} >= ${v2::class}]")
                        }
                        Operator.Not -> throw OperationRuntimeException("Needs only one Argument", expression.operator)
                        Operator.Equals -> throw OperationRuntimeException("In this position operator isn't allowed", expression.operator)
                    }
                }

            }
            is Expression.FunctionCall ->{
                return when(expression.functionName){
                    "ToString" -> toStringImplementation(expression.parameterList?.map { evalExpression(it,environment) } )
                    else -> {
                        val function = functionDeclarations[expression.functionName] ?: throw FunctionNotFoundRuntimeException(expression.functionName)
                        return evalFunction(function, expression.parameterList?.map { evalExpression(it,environment) }) ?: throw ReturnNotFoundRuntimeException(expression.functionName)
                    }
                }
            }
        }


    }

    private fun toStringImplementation(parameterList: List<Expression>?): Expression.Value{
        parameterList ?: throw Exception("Function ToString need one parameter")
        if(parameterList.size > 1)
            throw Exception("Function ToString only accepts one Parameter")

        val value = parameterList.first() as? Expression.Value ?: throw Exception("Can't ToString Expression: ${parameterList.first()}")
        return Expression.Value(ConstantValue.String(value.value.getValueAsString()))
    }

    private fun negateNumber(v1: Expression.Value): Expression.Value{
        return when (val v1n = v1.value){
            is ConstantValue.Integer -> Expression.Value(ConstantValue.Integer(-v1n.value))
            is ConstantValue.Float -> Expression.Value(ConstantValue.Float(-v1n.value))
                else -> throw TypeMismatchRuntimeException("This type can't be negated",v1.value.getType())
        }
    }

    private fun evalBinaryBoolean(v1: Any, v2: Any, f: (Boolean, Boolean) -> Boolean): Expression.Value {
        val v1n = v1 as? Boolean ?: throw Exception("Can't use a binary operation on $v1, it's not a boolean")
        val v2n = v2 as? Boolean ?: throw Exception("Can't use a binary operation on $v2, it's not a boolean")
        return Expression.Value( ConstantValue.Boolean(f(v1n, v2n)))
    }

    private fun numberToFloat(number : Any) : Float = number as? Float ?: (number as Int).toFloat()
}
