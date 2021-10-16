package Evaluator

import Evaluator.Exceptions.*
import Evaluator.Exceptions.NotFound.*
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.DynamicValue

class Evaluator {

    private val classDeclarations = HashMap<String, Declaration.ClassDeclare>()
    private val functionDeclarations = HashMap<String, MutableList<Declaration.FunctionDeclare>>()
    private val globalEnvironment = HashMap<String, Expression.Value>()

    fun eval(declarations: List<Declaration>, args : List<Expression.Value>? = null) : Expression.Value? {

        declarations.forEach { d ->
            when(d){
                is Declaration.ClassDeclare -> classDeclarations[d.className] = d
                is Declaration.FunctionDeclare -> functionDeclarations.getOrPut(d.functionName, ::mutableListOf).add(d)
                is Declaration.VariableDeclaration -> globalEnvironment[d.name] = evalExpression(d.expression, globalEnvironment)
            }
        }

        return evalFunction(functionDeclarations["Main"], "Main",args, HashMap())
    }

    private fun evalFunction(functions : MutableList<Declaration.FunctionDeclare>?, functionName : String, rawParameter: List<Expression>?, environment: HashMap<String, Expression.Value>) : Expression.Value? {

        val parameter = rawParameter?.map { evalExpression(it,environment) }
        val function = functions?.firstOrNull{ functionAcceptsParameter(it, parameter) } ?: throw FunctionNotFoundRuntimeException(functionName)

        if(function.parameters?.size != parameter?.size)
            throw FunctionParameterRuntimeException(function.functionName,function.parameters)

        val localEnvironment = function.parameters?.zip(parameter.orEmpty()){ fp, p -> fp.name to p }?.associateTo(HashMap()){it.first to it.second} ?: HashMap()

        if(function.returnType == Type.Void){
            evalBody(function.body, localEnvironment)
            return null
        }

        return evalBody(function.body, localEnvironment) ?: throw ReturnNotFoundRuntimeException(function.functionName)
    }

    private fun evalMethod(functions : MutableList<Declaration.FunctionDeclare>?, functionName : String, rawParameter: List<Expression>?, environment: HashMap<String, Expression.Value> ): Expression.Value?{
        val parameter = rawParameter?.map { evalExpression(it,environment) }
        val function = functions?.firstOrNull{ functionAcceptsParameter(it, parameter) } ?: throw FunctionNotFoundRuntimeException(functionName)

        var returnValue : Expression.Value? = null

        val localEnvironment = function.parameters?.zip(parameter.orEmpty()){ fp, p -> fp.name to p }?.associateTo(HashMap()){it.first to it.second} ?: HashMap()

        val methodEnvironment = combineEnvironments(environment,localEnvironment)

        if(function.returnType == Type.Void){
            evalBody(function.body, methodEnvironment)
        }else{
            returnValue = evalBody(function.body, methodEnvironment) ?: throw ReturnNotFoundRuntimeException(function.functionName)
        }

        changeEnvironment(environment,methodEnvironment,localEnvironment.keys.toList())

        return returnValue
    }

    private fun evalConstructor(classDefinition: Declaration.ClassDeclare, parameter: List<Expression.Value>?) : Expression.Value {
        val classEnvironment = HashMap<String, Expression.Value>()

        classDefinition.classBody.variables?.forEach {
            classEnvironment[it.name] = evalExpression(it.expression, combineEnvironments(globalEnvironment,classEnvironment))
        }

        evalMethod(classDefinition.classBody.functions[classDefinition.className], classDefinition.className, parameter, classEnvironment)

        return Expression.Value(DynamicValue.Class(classEnvironment , Type.Custom(classDefinition.className.decapitalize())))
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
                        else -> evalFunction(functionDeclarations[statement.procedureName],statement.procedureName,statement.parameterList,localEnvironment)
                    }
                }
                is Statement.UseClass -> statementUseClass(statement, localEnvironment)
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

    private fun statementUseClass(statement : Statement.UseClass, environment: HashMap<String, Expression.Value>){
        val obj = environment.getOrDefault(statement.variableName,null) ?: globalEnvironment.getOrDefault(statement.variableName,null) ?: throw VariableNotFoundRuntimeException(statement.variableName)
        val classObj = (obj.value as? DynamicValue.Class) ?: throw Exception("Can't use dot operation on baseTypes")
        val classDef = classDeclarations[classObj.type.name.capitalize()] ?: throw Exception("Couldn't find class named: ${classObj.type.name.capitalize()}")

        when(val statement = statement.statement){
            is Statement.AssignValue -> {
                when{
                    classObj.value.containsKey(statement.variableName) -> classObj.value[statement.variableName] = evalExpression(statement.expression, environment)
                    else -> VariableNotFoundRuntimeException(statement.variableName)
                }
            }
            is Statement.ProcedureCall -> {
                val parameters = statement.parameterList?.map { evalExpression(it,environment) }
                evalMethod(classDef.classBody.functions[statement.procedureName],statement.procedureName,parameters,classObj.value)
            }
            is Statement.UseClass ->{
                statementUseClass(statement, classObj.value)
            }
            else -> throw Exception("Can't use $statement in this context")
        }
    }

    private fun combineEnvironments(upperEnvironment : HashMap<String, Expression.Value>,lowerEnvironment : HashMap<String, Expression.Value>) : HashMap<String, Expression.Value>{
        return HashMap((upperEnvironment.keys + lowerEnvironment.keys).associateWith { k -> lowerEnvironment[k] ?: upperEnvironment[k] ?: throw VariableNotFoundRuntimeException(k) })
    }

    private fun changeEnvironment(environment : HashMap<String, Expression.Value>, localEnvironment : HashMap<String, Expression.Value>, shadowMap : List<String>){
        environment.forEach{ entry ->
            if(!shadowMap.contains(entry.key))
                environment[entry.key] = localEnvironment[entry.key] ?: throw Exception("Shouldn't occur")
        }
    }

    private fun evalExpression(expression: Expression, environment : HashMap<String, Expression.Value> ) : Expression.Value{
        return when(expression){
            is Expression.Value -> expression
            is Expression.UseVariable -> getVariableValue(expression, environment)
            is Expression.UseDotVariable -> evalDotVariable(expression, environment)
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
                        functionDeclarations[expression.functionName]?.let { funcs ->
                            return evalFunction(funcs, expression.functionName, expression.parameterList, environment)
                                ?: throw ReturnNotFoundRuntimeException(expression.functionName)
                        }

                        classDeclarations[expression.functionName]?.let { classDec ->
                            return evalConstructor(classDec, expression.parameterList?.map { evalExpression(it,environment) })
                        }

                        throw FunctionNotFoundRuntimeException(expression.functionName)
                    }
                }

            }
        }
    }

    private fun functionAcceptsParameter(function: Declaration.FunctionDeclare, parameterList: List<Expression.Value>?) : Boolean {
        if(function.parameters?.size != parameterList?.size)
            return false

        if(function.parameters.isNullOrEmpty())
            return true

        val parameterCombined = function.parameters.zip(parameterList.orEmpty()){ fp, p -> fp to p }

        return parameterCombined.fold(true){ acc, parameter ->
            if(!acc)
                return false
            parameter.first.type == parameter.second.value.getType()
        }

    }

    private fun getVariableValue( expression: Expression.UseVariable, environment: HashMap<String, Expression.Value>) =
        environment.getOrDefault(expression.variableName, null) ?: globalEnvironment.getOrDefault(expression.variableName, null) ?: throw VariableNotFoundRuntimeException(expression.variableName)

    private fun evalDotVariable(expression: Expression.UseDotVariable, environment : HashMap<String, Expression.Value>) : Expression.Value{

        val obj = environment.getOrDefault(expression.variableName,null) ?: globalEnvironment.getOrDefault(expression.variableName,null) ?: throw VariableNotFoundRuntimeException(expression.variableName)
        val classObj = (obj.value as? DynamicValue.Class)

        return when(val expression2 = expression.expression){
            is Expression.UseVariable -> getVariableValue(expression2, classObj?.value ?: throw Exception("Can't use dot operation on baseTypes"))
            is Expression.UseDotVariable -> evalDotVariable(expression2,classObj?.value ?: throw Exception("Can't use dot operation on baseTypes"))
            is Expression.FunctionCall -> {
                when(expression2.functionName){
                    "ToString" -> toStringImplementation(evalExpression( Expression.UseVariable(expression.variableName, expression.LineOfCode) , environment))
                    else -> {
                        classObj ?: throw Exception("Can't use dot operation on baseTypes")
                        val classDec = classDeclarations[classObj.type.name] ?: throw Exception("Can't find corresponding class")
                        val parameters = expression2.parameterList?.map { evalExpression(it,environment) }
                        evalMethod(classDec.classBody.functions[expression2.functionName], expression2.functionName, parameters, classObj.value) ?: throw ReturnNotFoundRuntimeException(expression2.functionName)
                    }
                }
            }
            else -> throw Exception("Can't use ${expression2} on ${expression.variableName}.")
        }
    }

    private fun toStringImplementation(expression: Expression.Value): Expression.Value{
        return Expression.Value(ConstantValue.String(expression.value.getValueAsString()))
    }

    private fun toStringImplementation(parameterList: List<Expression>?): Expression.Value {
        parameterList ?: throw Exception("Function ToString need one parameter")

        if (parameterList.size > 1)
            throw Exception("Function ToString only accepts one Parameter")

        val value = parameterList.first() as? Expression.Value
            ?: throw Exception("Can't ToString Expression: ${parameterList.first()}")

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