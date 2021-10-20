package TypeChecker

import Evaluator.Exceptions.NotFound.FunctionNotFoundRuntimeException
import Evaluator.Exceptions.NotFound.ReturnNotFoundRuntimeException
import Evaluator.Exceptions.NotFound.VariableNotFoundRuntimeException
import Parser.ParserToken.*
import Parser.ParserToken.Values.DynamicValue
import TypeChecker.Exceptions.*

class TypeChecker(private val declarations: List<Declaration>, private val args : List<Expression.Value>? = null) {

    private val importDeclarations = HashMap<String, Declaration.ClassDeclare>()
    private val classDeclarations = HashMap<String, Declaration.ClassDeclare>()
    private val functionDeclarations = HashMap<String, MutableList<Declaration.FunctionDeclare>>()
    private val globalVariableDeclarations = HashMap<String, Declaration.VariableDeclaration>()

    fun check(){

        declarations.forEach { d ->
            when(d){
                is Declaration.Imports -> {
                    d.list?.forEach {
                        if(it is Declaration.ClassDeclare){
                            if(importDeclarations.containsKey(it.className))
                                throw TypeCheckerDuplicateClassException(it.LineOfCode, it.className)
                            importDeclarations[it.className] = it
                        }
                    }
                }
                is Declaration.ClassDeclare -> {
                    if(classDeclarations.containsKey(d.className))
                        throw TypeCheckerDuplicateClassException(d.LineOfCode, d.className)
                    classDeclarations[d.className] = d
                }
                is Declaration.FunctionDeclare -> {
                    val functionList = functionDeclarations.getOrPut(d.functionName, ::mutableListOf)

                    if(functionList.any {it.parameters?.size == d.parameters?.size && it.parameters?.zip(d.parameters ?: listOf() )?.all { it.first.type == it.second.type } != false })
                        throw TypeCheckerDuplicateFunctionException(d.LineOfCode, d)

                    functionList.add(d)
                }
                is Declaration.VariableDeclaration -> {
                    checkVariableDeclaration(d, HashMap())
                    globalVariableDeclarations[d.name] = d
                }

            }
        }

        classDeclarations.forEach {
            checkClassDeclaration(it.value)
        }

        functionDeclarations["Main"]?.let { mainFunctionList ->

            if(mainFunctionList.count() != 1)
                throw TypeCheckerOnlyOneMainException(mainFunctionList.first().LineOfCode)

            val mainFunction = mainFunctionList.first()

            val a = args?.map { getExpressionType(it, HashMap())}
            if(!checkParameter(mainFunction, a))
                throw TypeCheckerFunctionParameterException(mainFunction.LineOfCode ,mainFunction.functionName,a)

            checkFunctionDeclaration(mainFunction)
        }


        functionDeclarations.forEach { f ->
            if(f.key != "Main"){
                f.value.forEach { func -> checkFunctionDeclaration(func) }
            }
        }

    }

    private fun checkClassDeclaration(classDef: Declaration.ClassDeclare) {

        val localVariables = classDef.classBody.variables?.let { HashMap(classDef.classBody.variables.associate { it.name to it.type}) } ?: HashMap()

        classDef.classBody.variables?.forEach { v ->
            checkVariableDeclaration(v,localVariables)
        }

        classDef.classBody.functions.forEach{ (_, functions) ->
            functions.forEach{ function ->
                checkMethodDeclaration(function, localVariables)

                if(functions.count {
                        if(it.parameters.isNullOrEmpty() || function.parameters.isNullOrEmpty())
                            it.parameters.isNullOrEmpty() && function.parameters.isNullOrEmpty()
                        else
                            it.parameters.size == function.parameters.size &&
                            it.parameters.zip(function.parameters.orEmpty() ).all{ it.first.type == it.second.type }
                } >= 2) throw TypeCheckerDuplicateFunctionException(function.LineOfCode,function)
            }
        }

        if(classDef.classBody.functions[classDef.className].isNullOrEmpty())
            throw TypeCheckerConstructorNotFoundException(classDef.LineOfCode, classDef.className, classDef.className)

        classDef.classBody.functions[classDef.className]?.forEach {
            if(it.returnType != Type.Void)
                throw TypeCheckerReturnTypeException(it.LineOfCode,classDef.className)
        }
    }

    private fun checkFunctionDeclaration(functionDeclaration : Declaration.FunctionDeclare){
        checkBodyTypes(functionDeclaration.functionName ,functionDeclaration.body, functionDeclaration.returnType, functionDeclaration.parameters?.associate{it.name to it.type}?.let { HashMap(it)} ?: HashMap())
    }

    private fun checkMethodDeclaration(functionDeclaration : Declaration.FunctionDeclare, classVariables : HashMap<String, Type>){
        val variables = combineVariables(classVariables,functionDeclaration.parameters?.associate{it.name to it.type}?.let { HashMap(it)} ?: HashMap())
        checkBodyTypes(functionDeclaration.functionName ,functionDeclaration.body, functionDeclaration.returnType, variables)
    }

    private fun checkParameter(functionDeclaration : Declaration.FunctionDeclare, args : List<Type>?) : Boolean{
        if(functionDeclaration.parameters?.size != args?.size)
            return false

        if(functionDeclaration.parameters.isNullOrEmpty() && args.isNullOrEmpty())
            return true

        val parameterCombined = functionDeclaration.parameters!!.zip(args.orEmpty()){ fp, p -> fp to p }

        return parameterCombined.fold(true) { acc, e ->
            if(!acc)
                return false
            e.first.type == e.second
        }
    }


    private fun checkBodyTypes(functionName : String ,body: Body , returnType: Type? , upperVariables: HashMap<String, Type>?){

        val localVariables = body.localVariables?.let { HashMap(body.localVariables.associate { it.name to it.type}) } ?: HashMap()

        val combinedVariables = combineVariables(upperVariables,localVariables)

        body.localVariables?.forEach { lv ->
            checkVariableDeclaration(lv, combinedVariables)
        }

        body.functionBody.forEach { statement ->
            when(statement){
                is Statement.AssignValue -> {
                    when(statement.variableName){
                        "return" -> {
                            val type = getExpressionType( statement.expression, combinedVariables)
                            if(returnType != type)
                                throw TypeCheckerReturnTypeException(statement.LineOfCode, functionName ,returnType, type)
                        }
                        else -> {
                            val type = getExpressionType( statement.expression, combinedVariables)
                            if (combinedVariables[statement.variableName] != type)
                                throw TypeCheckerWrongTypeAssignmentException(statement.LineOfCode, statement.variableName, combinedVariables[statement.variableName] ,type)
                        }
                    }
                }
                is Statement.Block -> {
                    checkBodyTypes(functionName, statement.body, returnType ,combinedVariables)
                }
                is Statement.If -> {
                    val conditionType = getExpressionType(statement.condition, combinedVariables)
                    conditionType as? Type.Boolean ?: throw TypeCheckerConditionException(statement.LineOfCode, conditionType)

                    checkBodyTypes(functionName, statement.ifBody, returnType, combinedVariables)
                    statement.elseBody?.let { checkBodyTypes(functionName, it, returnType, combinedVariables) }
                }
                is Statement.While -> {
                    val conditionType = getExpressionType(statement.condition, combinedVariables)
                    conditionType as? Type.Boolean ?: throw TypeCheckerConditionException(statement.LineOfCode, conditionType)

                    checkBodyTypes(functionName, statement.body, returnType, combinedVariables)
                }
                is Statement.ProcedureCall -> {
                    if(statement.procedureName != "Println" && statement.procedureName != "Print")
                    {
                        val procedureList = functionDeclarations[statement.procedureName] ?: throw TypeCheckerFunctionNotFoundException(statement.LineOfCode, statement.procedureName)
                        procedureList.firstOrNull { checkParameter(it, statement.parameterList?.map { getExpressionType(it, HashMap())}) }
                            ?: throw TypeCheckerFunctionParameterException(statement.LineOfCode,statement.procedureName, statement.parameterList?.map { getExpressionType(it, HashMap())})
                    }
                }
            }
        }
    }

    private fun combineVariables(upperEnvironment : HashMap<String, Type>?,lowerEnvironment : HashMap<String, Type>) : HashMap<String, Type>{
        if (upperEnvironment == null)
            return lowerEnvironment
        return HashMap((upperEnvironment.keys + lowerEnvironment.keys).associateWith { k -> lowerEnvironment[k] ?: upperEnvironment[k] ?: throw VariableNotFoundRuntimeException(k) })
    }

    private fun checkVariableDeclaration(variableDeclaration: Declaration.VariableDeclaration, localVariables : HashMap<String, Type>) {
        val type = getExpressionType(variableDeclaration.expression, localVariables)
        if(variableDeclaration.type != type)
            throw TypeCheckerWrongTypeAssignmentException(variableDeclaration.LineOfCode, variableDeclaration.name, variableDeclaration.type, type)
    }

    private fun getExpressionType(expression: Expression, localVariables : HashMap<String, Type>): Type {
        return when(expression){
            is Expression.Value -> expression.value.getType()
            is Expression.FunctionCall -> {
                when(expression.functionName){
                    "ToString" -> {
                        if(expression.parameterList == null)
                            throw TypeCheckerFunctionParameterException(expression.LineOfCode, "Function: 'ToString' must have one or more transfer parameters")
                        Type.String
                    }
                    else -> {

                        classDeclarations[expression.functionName] ?: importDeclarations[expression.functionName]?.let {
                            val methodList = it.classBody.functions[it.className] ?: throw TypeCheckerConstructorNotFoundException(expression.LineOfCode, expression.functionName, it.className)
                            val parameterTypes = expression.parameterList?.map { getExpressionType(it, localVariables)}
                            methodList.firstOrNull { checkParameter(it, parameterTypes) } ?: throw TypeCheckerFunctionParameterException(expression.LineOfCode,expression.functionName, parameterTypes)
                            return Type.Custom(it.className)
                        }

                        functionDeclarations[expression.functionName]?.let { funcs ->
                            val functionList = functionDeclarations[expression.functionName] ?: throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, expression.functionName)
                            val parameterTypes = expression.parameterList?.map { getExpressionType(it, localVariables)}
                            val function = functionList.firstOrNull { checkParameter(it, parameterTypes) } ?: throw TypeCheckerFunctionParameterException(expression.LineOfCode,expression.functionName, parameterTypes)
                            return function.returnType
                        }

                        throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, expression.functionName)
                    }
                }
            }
            is Expression.UseVariable -> {
                localVariables[expression.variableName] ?: globalVariableDeclarations[expression.variableName]?.type ?: throw TypeCheckerVariableNotFoundException(expression.LineOfCode, expression.variableName)
            }
            is Expression.UseDotVariable -> {
                useDotVariable(localVariables, expression)
            }
            is Expression.Operation -> {
                if(expression.operator == Operator.Equals)
                    throw TypeCheckerOperationException(expression.LineOfCode, "Can't use Operator at this position", expression.operator)

                if(expression.expressionB == null){
                    return when(expression.operator){
                        Operator.Not -> {
                            val typeA = getExpressionType(expression.expressionA, localVariables)
                            typeA as? Type.Boolean ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode, Operator.Not,typeA)
                        }
                        Operator.Minus->  {
                            val typeA = getExpressionType(expression.expressionA, localVariables)
                            typeA as? Type.Integer ?: typeA as? Type.Float ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode, Operator.Minus,typeA)
                        }
                        else -> throw TypeCheckerOperationException(expression.LineOfCode, "Needs more then one Argument", expression.operator)
                    }
                }
                else{
                    val typeA = getExpressionType(expression.expressionA, localVariables)
                    val typeB = getExpressionType(expression.expressionB, localVariables)

                    return when (expression.operator){

                        Operator.And -> checkOperatorTypes<Type.Boolean>(Type.Boolean,typeA,typeB,expression)
                        Operator.Or -> checkOperatorTypes<Type.Boolean>(Type.Boolean,typeA,typeB,expression)

                        Operator.Minus -> numberOperation(typeA, typeB, expression)
                        Operator.Multiply -> numberOperation(typeA, typeB, expression)
                        Operator.Divide -> numberOperation(typeA, typeB, expression)
                        Operator.Less -> checkOperatorTypes2<Type.Boolean, Type.Float>(Type.Boolean,typeA,typeB,expression)
                        Operator.LessEqual -> checkOperatorTypes2<Type.Boolean, Type.Float>(Type.Boolean,typeA,typeB,expression)
                        Operator.Greater -> checkOperatorTypes2<Type.Boolean, Type.Float>(Type.Boolean,typeA,typeB,expression)
                        Operator.GreaterEquals -> checkOperatorTypes2<Type.Boolean, Type.Float>(Type.Boolean,typeA,typeB,expression)

                        Operator.Plus -> when{
                                typeA is Type.Integer && typeB is Type.Integer -> Type.Integer
                                typeA is Type.Integer || typeA is Type.Float && typeB is Type.Integer || typeB is Type.Float -> Type.Float
                                typeA is Type.String && typeB is Type.String -> Type.String
                                else -> throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeA, typeB)
                        }

                        Operator.DoubleEquals -> checkOperatorAllTypes(Type.Boolean,typeA,typeB,expression)
                        Operator.NotEqual -> checkOperatorAllTypes(Type.Boolean,typeA,typeB,expression)

                        Operator.Not -> throw TypeCheckerOperationException(expression.LineOfCode, "Needs only one Argument", expression.operator)
                        Operator.Equals -> throw TypeCheckerOperationException(expression.LineOfCode, "Can't use Operator at this position", expression.operator)

                    }
                }
            }
        }
    }

    private fun useDotVariable(localVariables: HashMap<String, Type>, expression: Expression.UseDotVariable) : Type{
        val classType = (localVariables[expression.variableName] ?: globalVariableDeclarations[expression.variableName]?.type) as? Type.Custom //?: throw TypeCheckerVariableNotFoundException(expression.LineOfCode, expression.variableName)
        val classObj = classDeclarations[classType?.name] ?: importDeclarations[classType?.name]
        val classVariables = classObj?.classBody?.variables?.associateTo(HashMap()) { it.name to it.type } ?: HashMap()

        return when (val expression2 = expression.expression){
            is Expression.UseVariable -> {
                classObj ?: throw TypeCheckerClassNotFoundException(expression.LineOfCode, expression.variableName)
                classVariables[expression2.variableName] ?: throw TypeCheckerVariableNotFoundException(expression.LineOfCode, expression.variableName)
            }
            is Expression.UseDotVariable -> useDotVariable(classVariables ,expression2)
            is Expression.FunctionCall -> {
                when(expression2.functionName){
                    "ToString" -> {
                        if(expression2.parameterList != null)
                            throw TypeCheckerFunctionParameterException(expression.LineOfCode, "Function: 'ToString' must have one or more transfer parameters")
                        Type.String
                    }
                    else ->{
                        classObj ?: throw TypeCheckerVariableNotFoundException(expression.LineOfCode, expression.variableName)
                        val functionList = classObj.classBody.functions[expression2.functionName] ?: throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, expression2.functionName)
                        val parameterTypes = expression2.parameterList?.map{ getExpressionType(it, localVariables)}
                        val function = functionList.firstOrNull { checkParameter(it, parameterTypes) } ?: throw TypeCheckerFunctionParameterException(expression.LineOfCode,expression2.functionName, parameterTypes)
                        return function.returnType
                    }
                }
            }
            else -> throw TypeCheckerCantUseOperationOnDot(expression2.LineOfCode, expression2)
        }
    }

    private fun numberOperation(typeA: Type, typeB: Type, expression: Expression.Operation)
    = when {
        typeA is Type.Integer && typeB is Type.Integer -> Type.Integer
        typeA is Type.Integer || typeA is Type.Float && typeB is Type.Integer || typeB is Type.Float -> Type.Float
        else -> throw TypeCheckerOperatorTypeException(expression.LineOfCode, expression.operator, typeA, typeB)
    }

    private fun <T> checkOperatorTypes(outputType : Type ,typeA : Type, typeB : Type, expression: Expression.Operation): Type {
        typeA as? T ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeA)
        typeB as? T ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeB)
        return outputType
    }

    private fun <T,D> checkOperatorTypes2(outputType : Type ,typeA : Type, typeB : Type, expression: Expression.Operation): Type {
        typeA as? T ?: typeA as? D ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeA)
        typeB as? T ?: typeB as? D ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeB)
        return outputType
    }

    private fun checkOperatorAllTypes(outputType : Type ,typeA : Type, typeB : Type, expression: Expression.Operation): Type {
        if(typeA == typeB)
            return outputType
        throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeA, typeB)
    }

}