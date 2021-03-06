package Evaluator

import Evaluator.Exceptions.*
import Evaluator.Exceptions.NotFound.*
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.DynamicValue
import openGLOutput.exercise.components.geometry.mesh.RenderableBase
import openGLOutput.framework.ModelLoader
import kotlin.random.Random

class Evaluator {

    fun eval(file: File, args : List<Expression.Value>? = null, functionName: String = "Main", environment: HashMap<String, Expression.Value> = hashMapOf()) : Expression.Value? {

        if (environment.isEmpty()){
            fun action(fileImport : File)
            {
                fileImport.variableDeclaration.forEach { (n, v) ->
                    fileImport.globalEnvironment[n] = evalExpression(v.expression, environment,null, fileImport)
                }

                fileImport.includes.forEach { (_, u) ->
                    if(u != null && u.variablesEventuated)
                        action(u)
                }
            }

            action(file)
        }

        if(!file.functionDeclarations.containsKey(functionName))
            return null

        return evalFunction(file.functionDeclarations[functionName], functionName, args, environment, null, file)
    }

    private fun evalFunction(functions : MutableList<Declaration.FunctionDeclare>?, functionName : String, rawParameter: List<Expression>?, environment: HashMap<String, Expression.Value>, currentClass : Declaration.ClassDeclare?, file : File) : Expression.Value? {

        val parameter = rawParameter?.map { evalExpression(it,environment, currentClass, file) }
        val function = functions?.firstOrNull{ functionAcceptsParameter(it,it.generics, parameter) } ?: throw FunctionNotFoundRuntimeException(-1, file.name, functionName)

        val localEnvironment = function.parameters?.zip(parameter.orEmpty()){ fp, p -> fp.name to p }?.associateTo(HashMap()){it.first to it.second} ?: HashMap()

        if(function.returnType == Type.Void){
            evalBody(function.body, localEnvironment, currentClass, file)
            return null
        }

        return evalBody(function.body, localEnvironment, currentClass, file) ?: throw ReturnNotFoundRuntimeException(function.LineOfCode, file.name, function.functionName)
    }

    private fun evalMethod(classDefinition: Declaration.ClassDeclare, functionName : String, rawParameter: List<Expression>?, environment: HashMap<String, Expression.Value>, file : File): Expression.Value?{
        val parameter = rawParameter?.map { evalExpression(it,environment, classDefinition, file) }
        val function = classDefinition.classBody.functions[functionName]?.firstOrNull{ functionAcceptsParameter(it, combineGenerics(it.generics, classDefinition.generics), parameter) } ?: throw FunctionNotFoundRuntimeException(rawParameter?.get(0)?.LineOfCode ?: -1, file.name, functionName)

        var returnValue : Expression.Value? = null

        val localEnvironment = function.parameters?.zip(parameter.orEmpty()){ fp, p -> fp.name to p }?.associateTo(HashMap()){it.first to it.second} ?: HashMap()

        val methodEnvironment = combineEnvironments(environment,localEnvironment, file)

        if(function.returnType == Type.Void){
            evalBody(function.body, methodEnvironment, classDefinition, file)
        }else{
            returnValue = evalBody(function.body, methodEnvironment, classDefinition, file) ?: throw ReturnNotFoundRuntimeException(function.LineOfCode, file.name, function.functionName)
        }

        changeEnvironment(environment,methodEnvironment,localEnvironment.keys.toList())

        return returnValue
    }

    private fun combineGenerics(generics1 : List<String>?,generics2 : List<String>?) : List<String>?{
        if(generics1.isNullOrEmpty() && generics2.isNullOrEmpty())
            return null
        return listOf<String>().union(generics1 ?: listOf()).union(generics2 ?: listOf()).toList()
    }

    private fun evalConstructor(classDefinition: Declaration.ClassDeclare, parameter: List<Expression.Value>?, file : File) : Expression.Value {
        val classEnvironment = HashMap<String, Expression.Value>()

        classDefinition.classBody.variables?.forEach {
            classEnvironment[it.name] = evalExpression(it.expression, combineEnvironments(file.globalEnvironment,classEnvironment, file), classDefinition, file)
        }

        evalMethod(classDefinition, classDefinition.className, parameter, classEnvironment, file)

        return Expression.Value(DynamicValue.Class(classEnvironment, Type.Custom(classDefinition.className)))
    }

    private fun evalBody(body: Body, environment: HashMap<String, Expression.Value>, currentClass: Declaration.ClassDeclare?, file : File) : Expression.Value? {

        val localEnvironment = HashMap<String, Expression.Value>()
        val shadowMap = mutableListOf<String>()

        body.localVariables?.forEach { variable ->
            if(localEnvironment.containsKey(variable.name))
                throw EvaluatorBaseException(variable.LineOfCode, file.name, "Variable can't be initialized twice '${variable.name}'")

            localEnvironment[variable.name] = evalExpression(variable.expression, combineEnvironments(environment,localEnvironment, file), currentClass, file)
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
                            return evalExpression(statement.expression, localEnvironment, currentClass, file)
                        }
                        else -> {
                             when {
                                localEnvironment.containsKey(statement.variableName) -> localEnvironment[statement.variableName] = evalExpression(statement.expression, localEnvironment, currentClass, file)
                                file.globalEnvironment.containsKey(statement.variableName) -> file.globalEnvironment[statement.variableName] = evalExpression(statement.expression, localEnvironment, currentClass, file)
                                else -> throw VariableNotFoundRuntimeException(statement.LineOfCode, file.name, statement.variableName)
                            }
                        }
                    }
                }
                is Statement.If -> {
                    val condition = evalExpression(statement.condition, localEnvironment, currentClass, file).value as? ConstantValue.Boolean
                        ?: throw TypeMismatchRuntimeException(statement.LineOfCode, file.name, "If condition must be of type", Type.Boolean)
                    if(condition.value){
                        evalBody(statement.ifBody, localEnvironment, currentClass, file)?.let {
                            changeEnvironment(environment, localEnvironment, shadowMap)
                            return it }
                    }else{
                        statement.elseBody?.let { evalBody(statement.elseBody, localEnvironment, currentClass, file)?.let {
                            changeEnvironment(environment, localEnvironment, shadowMap)
                            return it } }
                    }
                }
                is Statement.While -> {
                    while ((evalExpression(statement.condition, localEnvironment, currentClass, file).value as? ConstantValue.Boolean)?.value ?: throw TypeMismatchRuntimeException(statement.LineOfCode, file.name, "While condition must be of type", Type.Boolean))
                    {
                        evalBody(statement.body, localEnvironment, currentClass, file)?.let {
                            changeEnvironment(environment, localEnvironment, shadowMap)
                            return it }
                    }
                }
                is Statement.ProcedureCall ->{
                    when(statement.procedureName){
                        "Println" -> statement.parameterList?.map { evalExpression(it,localEnvironment, currentClass, file) }?.forEach { p -> println(p.value.getValueAsString())}
                        "Print" -> {
                            statement.parameterList?.map { evalExpression(it,localEnvironment, currentClass, file) }?.forEach { p -> print(p.value.getValueAsString())}
                        }
                        "_integratedFunctionSetArray" -> arraySetProcedureCall(statement, localEnvironment, currentClass, file)
                        "_integratedFunctionInitializeArray" -> arrayInitializeProcedureCall(statement, localEnvironment, currentClass, file)
                        "_integratedLoadObjectCube" -> loadObjectCall(statement, localEnvironment, currentClass, file)
                        else ->{
                            currentClass?.classBody?.functions?.get(statement.procedureName)?.let { _ ->
                                evalMethod(currentClass, statement.procedureName, statement.parameterList,localEnvironment, file)
                                changeEnvironment(environment, localEnvironment, shadowMap)
                                return@forEach
                            }

                            file.functionDeclarations[statement.procedureName]?.let { functionDeclarations ->
                                 evalFunction(functionDeclarations,statement.procedureName,statement.parameterList,localEnvironment, currentClass, file)
                                return@forEach
                            }

                            throw FunctionNotFoundRuntimeException(statement.LineOfCode, file.name, statement.procedureName)
                        }
                    }
                }
                is Statement.UseClass -> statementUseClass(statement, localEnvironment, file)
                is Statement.Block -> {
                    evalBody(statement.body,localEnvironment, currentClass, file)?.let {
                        changeEnvironment(environment, localEnvironment, shadowMap)
                        return it }
                }
            }
        }

        changeEnvironment(environment, localEnvironment, shadowMap)
        return null
    }


    private fun statementUseClass(statement : Statement.UseClass, environment: HashMap<String, Expression.Value>, file : File){
        val obj = environment.getOrDefault(statement.variableName,null) ?: file.globalEnvironment.getOrDefault(statement.variableName,null) ?: throw VariableNotFoundRuntimeException(statement.LineOfCode, file.name, statement.variableName)
        val classObj = (obj.value as? DynamicValue.Class) ?: throw EvaluatorBaseException(statement.LineOfCode, file.name,"Can't use dot operation on baseTypes")

        val action = { classDef : Declaration.ClassDeclare, importFile : File ->
            when(val statement2 = statement.statement){
                is Statement.AssignValue -> {
                    when{
                        classObj.value.containsKey(statement2.variableName) -> classObj.value[statement2.variableName] = evalExpression(statement2.expression, environment, classDef, file)
                        else -> VariableNotFoundRuntimeException(statement2.LineOfCode, file.name, statement2.variableName)
                    }
                }
                is Statement.ProcedureCall -> {
                    val parameters = statement2.parameterList?.map { evalExpression(it,environment, classDef, file) }
                    evalMethod(classDef,statement2.procedureName,parameters,classObj.value, importFile)
                }
                is Statement.UseClass ->{
                    statementUseClass(statement2, classObj.value, importFile)
                }
                else -> throw EvaluatorBaseException(statement2.LineOfCode, file.name,"Can't use $statement2 in this context")
            }
        }

        file.classDeclarations[classObj.type.name]?.let { classDef ->
            action(classDef, file)
            return
        }

        file.includes[classObj.type.name]?.let { fileImport ->
            action(fileImport.classDeclarations[classObj.type.name] ?: throw EvaluatorBaseException(statement.LineOfCode,file.name, "Couldn't find class named: ${classObj.type.name}") , fileImport)
            return
        }

        throw EvaluatorBaseException(statement.LineOfCode,file.name,"Couldn't find class named: ${classObj.type.name}")
    }

    private fun combineEnvironments(upperEnvironment : HashMap<String, Expression.Value>,lowerEnvironment : HashMap<String, Expression.Value>, file: File) : HashMap<String, Expression.Value>{
        return HashMap((upperEnvironment.keys + lowerEnvironment.keys).associateWith { k -> lowerEnvironment[k] ?: upperEnvironment[k] ?: throw VariableNotFoundRuntimeException(-1, file.name, k) })
    }

    private fun changeEnvironment(environment : HashMap<String, Expression.Value>, localEnvironment : HashMap<String, Expression.Value>, shadowMap : List<String>){
        environment.forEach{ entry ->
            if(!shadowMap.contains(entry.key))
                environment[entry.key] = localEnvironment[entry.key] ?: throw Exception("Shouldn't occur")
        }
    }

    private fun evalExpression(expression: Expression, environment : HashMap<String, Expression.Value>, currentClass : Declaration.ClassDeclare?, file: File) : Expression.Value{
        return when(expression){
            is Expression.Value -> expression
            is Expression.UseVariable -> getVariableValue(expression, environment, file)
            is Expression.UseDotVariable -> evalDotVariable(expression, environment, currentClass, file)
            is Expression.Operation -> {
                if(expression.expressionB == null){
                    return when(expression.operator){
                        Operator.Not -> Expression.Value(ConstantValue.Boolean(!(evalExpression(expression.expressionA, environment, currentClass, file).value as?  ConstantValue.Boolean ?: throw TypeMismatchRuntimeException(expression.LineOfCode, file.name, "This type can't be negated",Type.Boolean)).value))
                        Operator.Minus-> negateNumber(evalExpression(expression.expressionA, environment, currentClass, file), file)
                        else -> throw OperationRuntimeException(expression.LineOfCode, file.name, "Needs more then one Argument", expression.operator)
                    }
                }
                else{
                    val v1raw = evalExpression(expression.expressionA, environment, currentClass, file).value
                    val v2raw = evalExpression(expression.expressionB, environment, currentClass, file).value

                    val v1 = if(v1raw is ConstantValue.Null) null else v1raw.value
                    val v2 = if(v2raw is ConstantValue.Null) null else v2raw.value

                    return when(expression.operator){
                        Operator.DoubleEquals -> Expression.Value(ConstantValue.Boolean(v1 == v2))
                        Operator.Plus -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Integer(v1 + v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Float(numberToFloat(v1) + numberToFloat(v2)))
                            v1 is String && v2 is String -> Expression.Value(ConstantValue.String(v1 + v2))
                            else -> throw EvaluatorBaseException(expression.LineOfCode,file.name,"Can't use add operation on [${v1?.let { v1::class }} + ${v2?.let { v2::class }}]")
                        }
                        Operator.Minus -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Integer(v1 - v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Float(numberToFloat(v1) - numberToFloat(v2)))
                            else -> throw EvaluatorBaseException(expression.LineOfCode,file.name,"Can't use subtract operation on [${v1?.let { v1::class }} - ${v2?.let { v2::class }}]")
                        }
                        Operator.Multiply -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Integer(v1 * v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Float(numberToFloat(v1) * numberToFloat(v2)))
                            else -> throw EvaluatorBaseException(expression.LineOfCode,file.name,"Can't use multiply operation on [${v1?.let { v1::class }} * ${v2?.let { v2::class }}]")
                        }
                        Operator.Divide -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Integer(v1 / v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Float(numberToFloat(v1) / numberToFloat(v2)))
                            else -> throw EvaluatorBaseException(expression.LineOfCode,file.name,"Can't use division operation on [${v1?.let { v1::class }} * ${v2?.let { v2::class }}]")
                        }
                        Operator.And -> evalBinaryBoolean(v1,v2){x,y -> x&&y}
                        Operator.Or -> evalBinaryBoolean(v1,v2){x,y -> x||y}
                        Operator.NotEqual -> Expression.Value(ConstantValue.Boolean(v1 != v2))
                        Operator.Less -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Boolean(v1 < v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Boolean(numberToFloat(v1) < numberToFloat(v2)))
                            else -> throw EvaluatorBaseException(expression.LineOfCode,file.name,"Can't use less operation on [${v1?.let { v1::class }} < ${v2?.let { v2::class }}]")
                        }
                        Operator.LessEqual -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Boolean(v1 <= v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Boolean(numberToFloat(v1) <= numberToFloat(v2)))
                            else -> throw EvaluatorBaseException(expression.LineOfCode,file.name,"Can't use lessEqual operation on [${v1?.let { v1::class }} <= ${v2?.let { v2::class }}]")
                        }
                        Operator.Greater -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Boolean(v1 > v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Boolean(numberToFloat(v1) > numberToFloat(v2)))
                            else -> throw EvaluatorBaseException(expression.LineOfCode,file.name,"Can't use less operation on [${v1?.let { v1::class }} > ${v2?.let { v2::class }}]")
                        }
                        Operator.GreaterEquals -> when{
                            v1 is Int && v2 is Int -> Expression.Value(ConstantValue.Boolean(v1 >= v2))
                            v1 is Float || v1 is Int && v2 is Float || v2 is Int -> Expression.Value(ConstantValue.Boolean(numberToFloat(v1) >= numberToFloat(v2)))
                            else -> throw EvaluatorBaseException(expression.LineOfCode,file.name,"Can't use lessEqual operation on [${v1?.let { v1::class }} >= ${v2?.let { v2::class }}]")
                        }
                        Operator.Not -> throw OperationRuntimeException(expression.LineOfCode, file.name, "Needs only one Argument", expression.operator)
                        Operator.Equals -> throw OperationRuntimeException(expression.LineOfCode, file.name, "In this position operator isn't allowed", expression.operator)

                    }
                }

            }
            is Expression.FunctionCall ->{
                return when(expression.functionName){
                    "GetRandomInt" -> getRandomIntImplementation(expression.parameterList?.map { evalExpression(it,environment, currentClass, file) }, file)
                    "ToString" -> toStringImplementation(expression.parameterList?.map { evalExpression(it,environment, currentClass, file) } )
                    "BitAnd" -> bitAndImplementation(expression.parameterList?.map { evalExpression(it,environment, currentClass, file) } )
                    "BitXor" -> bitXorImplementation(expression.parameterList?.map { evalExpression(it,environment, currentClass, file) } )
                    "ToInt" -> toIntImplementation(expression.parameterList?.map { evalExpression(it,environment, currentClass, file) } )
                    "_integratedFunctionGetArray" -> arrayGetFunctionCall(expression, environment, currentClass, file)
                    else -> {
                        file.classDeclarations[expression.functionName]?.let { classDec ->
                            return evalConstructor(classDec, expression.parameterList?.map { evalExpression(it,environment, currentClass, file) }, file)
                        }

                        file.includes[expression.functionName]?.let { include ->
                            val classDec = include.classDeclarations[expression.functionName] ?: throw ClassNotFoundException()
                            return evalConstructor(classDec, expression.parameterList?.map { evalExpression(it,environment, currentClass, file) }, include)
                        }

                        file.functionDeclarations[expression.functionName]?.let { functions ->
                            return evalFunction(functions, expression.functionName, expression.parameterList, environment, currentClass, file)
                                ?: throw ReturnNotFoundRuntimeException(expression.LineOfCode, file.name, expression.functionName)
                        }

                        currentClass?.classBody?.functions?.get(expression.functionName)?.let {
                            return evalMethod(currentClass, expression.functionName, expression.parameterList, environment, file)
                                ?: throw ReturnNotFoundRuntimeException(expression.LineOfCode, file.name, expression.functionName)
                        }

                        throw FunctionNotFoundRuntimeException(expression.LineOfCode, file.name, expression.functionName)
                    }
                }

            }
        }
    }

    private fun functionAcceptsParameter(function: Declaration.FunctionDeclare, generics : List<String>?, parameterList: List<Expression.Value>?) : Boolean {

        if(function.parameters.isNullOrEmpty() && parameterList.isNullOrEmpty())
            return true

        if(function.parameters?.size != parameterList?.size)
            return false

        if(function.parameters.isNullOrEmpty())
            return true

        val parameterCombined = function.parameters.zip(parameterList.orEmpty()){ fp, p -> fp to p }

        return parameterCombined.fold(true){ acc, parameter ->
            if(!acc)
                return false
            parameter.first.type == parameter.second.value.getType() || generics?.contains((parameter.first.type as? Type.Custom)?.name ) ?: false
        }

    }

    private fun getVariableValue( expression: Expression.UseVariable, environment: HashMap<String, Expression.Value>, file : File) =
        environment.getOrDefault(expression.variableName, null) ?: file.globalEnvironment.getOrDefault(expression.variableName, null) ?: throw VariableNotFoundRuntimeException(expression.LineOfCode, file.name, expression.variableName)

    private fun evalDotVariable(expression: Expression.UseDotVariable, environment : HashMap<String, Expression.Value>, currentClass: Declaration.ClassDeclare?, file : File) : Expression.Value{

        val obj = environment.getOrDefault(expression.variableName,null) ?: file.globalEnvironment.getOrDefault(expression.variableName,null) ?: throw VariableNotFoundRuntimeException(expression.LineOfCode, file.name, expression.variableName)
        val classObj = (obj.value as? DynamicValue.Class)

        return when(val expression2 = expression.expression){
            is Expression.UseVariable -> getVariableValue(expression2, classObj?.value ?: throw EvaluatorBaseException(expression2.LineOfCode, file.name, "Can't use dot operation on baseTypes"), file)
            is Expression.UseDotVariable -> evalDotVariable(expression2,classObj?.value ?: throw EvaluatorBaseException(expression2.LineOfCode, file.name, "Can't use dot operation on baseTypes"), currentClass, file)
            is Expression.FunctionCall -> {
                classObj?.let {

                    val action = {  classDec : Declaration.ClassDeclare, importFile : File ->
                        val parameters = expression2.parameterList?.map { evalExpression(it,environment, currentClass, file) }
                        evalMethod(classDec, expression2.functionName, parameters, classObj.value, importFile) ?: throw ReturnNotFoundRuntimeException(expression2.LineOfCode, file.name, expression2.functionName)
                    }

                    file.classDeclarations[classObj.type.name]?.let { classDeclare ->
                        return action(classDeclare, file)
                    }

                    file.includes[classObj.type.name]?.let{ importFile ->
                        return action(importFile.classDeclarations[classObj.type.name] ?: throw EvaluatorBaseException(expression2.LineOfCode, file.name,"Can't find corresponding class"), importFile)
                    }
                }

                //if none Function with Name ToString exists
                if(expression2.functionName == "ToString")
                   return toStringImplementation(evalExpression( Expression.UseVariable(expression.variableName, expression.LineOfCode) , environment, currentClass, file))

                throw EvaluatorBaseException(expression2.LineOfCode, file.name,"Can't use dot operation on baseTypes")
            }
            else -> throw EvaluatorBaseException(expression2.LineOfCode, file.name,"Can't use $expression2 on ${expression.variableName}.")
        }
    }

    private fun negateNumber(v1: Expression.Value, file: File) : Expression.Value{
        return when (val v1n = v1.value){
            is ConstantValue.Integer -> Expression.Value(ConstantValue.Integer(-v1n.value))
            is ConstantValue.Float -> Expression.Value(ConstantValue.Float(-v1n.value))
                else -> throw TypeMismatchRuntimeException(v1.LineOfCode, file.name, "This type can't be negated",v1.value.getType())
        }
    }

    private fun evalBinaryBoolean(v1: Any?, v2: Any?, f : (Boolean, Boolean) -> Boolean) : Expression.Value {
        val v1n = v1 as? Boolean ?: throw Exception("Can't use a binary operation on $v1, it's not a boolean")
        val v2n = v2 as? Boolean ?: throw Exception("Can't use a binary operation on $v2, it's not a boolean")
        return Expression.Value( ConstantValue.Boolean(f(v1n, v2n)))
    }

    private fun numberToFloat(number : Any?) : Float = number as? Float ?: (number as Int).toFloat()

    private fun getRandomIntImplementation(parameterList: List<Expression>?,file: File) : Expression.Value{
        parameterList ?: throw EvaluatorBaseException(-1,file.name,"Function GetRandomInt needs one or two parameters. GetRandomInt(Int from, Int until)")
        if(parameterList.size == 1)
            return Expression.Value(ConstantValue.Integer(Random.nextInt(((parameterList[0] as Expression.Value).value as? ConstantValue.Integer)?.value ?:
            throw EvaluatorBaseException(-1,file.name,"Function GetRandomInt needs one or two parameters. GetRandomInt([Int from,] Int until)"))))

        if(parameterList.size == 2)
            return Expression.Value(ConstantValue.Integer(Random.nextInt(
                ((parameterList[0] as Expression.Value).value as? ConstantValue.Integer)?.value ?:
            throw EvaluatorBaseException(-1,file.name,"Function GetRandomInt needs one or two parameters. GetRandomInt([Int from,] Int until)"),
                ((parameterList[1] as Expression.Value).value as? ConstantValue.Integer)?.value ?:
                throw EvaluatorBaseException(-1,file.name,"Function GetRandomInt needs one or two parameters. GetRandomInt([Int from,] Int until)"))))

        throw EvaluatorBaseException(-1,file.name,"Function GetRandomInt needs one or two parameters. GetRandomInt([Int from,] Int until)")
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

    private fun toIntImplementation(parameterList: List<Expression>?): Expression.Value {
        parameterList ?: throw Exception("Function ToInt need one parameter")

        if (parameterList.size > 1)
            throw Exception("Function ToInt only accepts one Parameter")

        val value = (parameterList[0] as? Expression.Value )?.value?.value as? Float
            ?: throw Exception("Can't ToInt Expression: ${parameterList[0]}")

        return Expression.Value(ConstantValue.Integer(value.toInt()))
    }

    private fun bitAndImplementation(parameterList: List<Expression>?): Expression.Value {
        parameterList ?: throw Exception("Function BitAnd need two parameter")

        if (parameterList.size != 2)
            throw Exception("Function BitAnd need two parameter")

        val value = (parameterList[0] as? Expression.Value )?.value?.value as? Int
            ?: throw Exception("Can't BitAnd Expression: ${parameterList[0]}")
        val value1 = (parameterList[1] as? Expression.Value )?.value?.value as? Int
            ?: throw Exception("Can't BitAnd Expression: ${parameterList[1]}")

        return Expression.Value(ConstantValue.Integer(value and value1))
    }

    private fun bitXorImplementation(parameterList: List<Expression>?): Expression.Value {
        parameterList ?: throw Exception("Function BitXor need two parameter")

        if (parameterList.size != 2)
            throw Exception("Function BitXor need two parameter")

        val value = (parameterList[0] as? Expression.Value )?.value?.value as? Int
            ?: throw Exception("Can't BitXor Expression: ${parameterList[0]}")
        val value1 = (parameterList[1] as? Expression.Value )?.value?.value as? Int
            ?: throw Exception("Can't BitXor Expression: ${parameterList[1]}")

        return Expression.Value(ConstantValue.Integer(value xor value1))
    }


    private fun arraySetProcedureCall(statement: Statement.ProcedureCall, localEnvironment: HashMap<String, Expression.Value>, currentClass: Declaration.ClassDeclare?, file: File) {
        val parameter = statement.parameterList?.map { evalExpression(it, localEnvironment, currentClass, file) }
        val index = parameter?.get(0)?.value as? ConstantValue.Integer ?: throw EvaluatorBaseException(statement.LineOfCode, file.name, "First parameter of Set must be an Integer. 'Set(Int index, T Value)'")
        val value = parameter[1].value

        try {
            (localEnvironment["array"]?.value as? DynamicValue.Array)!!.value[index.value] =
                Expression.Value(value, statement.LineOfCode)
        }catch (e : Exception ){
            throw EvaluatorBaseException(statement.LineOfCode,file.name,e.message ?: "")
        }


    }

    private fun arrayInitializeProcedureCall(statement: Statement.ProcedureCall, localEnvironment: HashMap<String, Expression.Value>, currentClass: Declaration.ClassDeclare?, file: File) {
        val parameter = statement.parameterList?.map { evalExpression(it, localEnvironment, currentClass, file) }
        val size = parameter?.get(0)?.value as? ConstantValue.Integer ?: throw EvaluatorBaseException(statement.LineOfCode, file.name, "First parameter of Set must be an Integer. 'Array<T>(Int size)'")

        localEnvironment["array"] = Expression.Value(DynamicValue.Array(Array( size.value){Expression.Value(ConstantValue.Null())},Type.CustomWithGenerics("T", listOf())))
    }

    private fun arrayGetFunctionCall( expression: Expression.FunctionCall, environment: HashMap<String, Expression.Value>, currentClass: Declaration.ClassDeclare?, file: File): Expression.Value {
        val parameter = expression.parameterList?.map { evalExpression(it, environment, currentClass, file) }
        val index = parameter?.get(0)?.value as? ConstantValue.Integer ?: throw EvaluatorBaseException( expression.LineOfCode, file.name, "First parameter of Get must be an Integer. 'Get(Int index)'")
        try {
            return (environment["array"]?.value as? DynamicValue.Array)!!.value[index.value]
        }catch (e : Exception ){
            throw EvaluatorBaseException(expression.LineOfCode,file.name,e.message ?: "")
        }
    }

    val loadedObjects = hashMapOf<String, RenderableBase>()

    private fun loadObjectCall(statement: Statement.ProcedureCall, localEnvironment: HashMap<String, Expression.Value>, currentClass: Declaration.ClassDeclare?, file: File) {
        val parameter = statement.parameterList?.map { evalExpression(it, localEnvironment, currentClass, file) }
        val path = parameter?.get(0)?.value as? ConstantValue.String ?: throw EvaluatorBaseException(statement.LineOfCode, file.name, "First parameter must be of type String. 'Object(String path)'")
        val finalPath = if(path.value[1] == ':') path.value else "code/${path.value}"

        val renderObject = if(!loadedObjects.containsKey(finalPath)){
            loadedObjects[finalPath] = ModelLoader.loadModel(finalPath,0f, 0f,0f) ?: throw EvaluatorBaseException(statement.LineOfCode, file.name, "Couldn't load objectFile path:'${path.value}'")
            loadedObjects[finalPath]!!
        }else
            loadedObjects[finalPath]!!

        localEnvironment["_object"] = Expression.Value(DynamicValue.Object(renderObject, Type.Custom("_Object")))
    }
}