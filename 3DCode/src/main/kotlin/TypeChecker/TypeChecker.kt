package TypeChecker

import Evaluator.Exceptions.NotFound.FunctionNotFoundRuntimeException
import Evaluator.Exceptions.NotFound.ReturnNotFoundRuntimeException
import Evaluator.Exceptions.NotFound.VariableNotFoundRuntimeException
import Parser.ParserToken.*
import Parser.ParserToken.Values.DynamicValue
import TypeChecker.Exceptions.*
import com.sun.tools.javac.Main

class TypeChecker() {

    private val checkedFiles = mutableListOf<String>()

    fun check(mainFile : File, args : List<Expression.Value>?) {
        checkFile(mainFile)

        if(mainFile.functionDeclarations["Main"].isNullOrEmpty())
            throw TypeCheckerFunctionNotFoundException(-1, "Main")

        mainFile.functionDeclarations["Main"]?.let { mainFunctionList ->

            if(mainFunctionList.count() != 1)
                throw TypeCheckerOnlyOneMainException(mainFunctionList.first().LineOfCode)

            val mainFunction = mainFunctionList.first()

            val a = args?.map { getExpressionType(it, HashMap(), mainFile)}
            if(!checkParameter(mainFunction, a, mainFile))
                throw TypeCheckerFunctionParameterException(mainFunction.LineOfCode ,mainFunction.functionName,a)

            checkFunctionDeclaration(mainFunction, mainFile)
        }

    }

    private fun checkFile(file: File){

        file.includes.forEach { t, f ->
            f ?: throw TypeCheckerFileNotFoundException(file.name)
            if(checkedFiles.contains(file.name))
                checkFile(f)
        }

        file.variableDeclaration.forEach { (_, d) ->
            checkVariableDeclaration(d, HashMap(), file)
        }

        file.functionDeclarations.forEach { (n, functions) ->
            functions.forEach{ function ->
                if(function.functionName != "Main"){
                    checkFunctionDeclaration(function, file)

                    if(functions.count {
                            if(it.parameters.isNullOrEmpty() || function.parameters.isNullOrEmpty())
                                it.parameters.isNullOrEmpty() && function.parameters.isNullOrEmpty()
                            else
                                it.parameters.size == function.parameters.size &&
                                        it.parameters.zip(function.parameters.orEmpty() ).all{ it.first.type == it.second.type }
                        } >= 2) throw TypeCheckerDuplicateFunctionException(function.LineOfCode,function)
                }
            }
        }

        file.classDeclarations.forEach { (n, c) ->
            checkClassDeclaration(c, file)
        }



    }

    private fun checkClassDeclaration(classDef: Declaration.ClassDeclare, file : File) {

        val localVariables = classDef.classBody.variables?.let { HashMap(classDef.classBody.variables.associate { it.name to it.type}) } ?: HashMap()

        classDef.classBody.variables?.forEach { v ->
            checkVariableDeclaration(v,localVariables, file)
        }

        classDef.classBody.functions.forEach{ (_, functions) ->
            functions.forEach{ function ->
                checkMethodDeclaration(function, localVariables, file)

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

    private fun checkFunctionDeclaration(functionDeclaration : Declaration.FunctionDeclare, file : File){
        checkBodyTypes(functionDeclaration.functionName ,functionDeclaration.body, functionDeclaration.returnType, functionDeclaration.parameters?.associate{it.name to it.type}?.let { HashMap(it)} ?: HashMap(), file)
    }

    private fun checkMethodDeclaration(functionDeclaration : Declaration.FunctionDeclare, classVariables : HashMap<String, Type>, file : File){
        val variables = combineVariables(classVariables,functionDeclaration.parameters?.associate{it.name to it.type}?.let { HashMap(it)} ?: HashMap())
        checkBodyTypes(functionDeclaration.functionName ,functionDeclaration.body, functionDeclaration.returnType, variables, file)
    }

    private fun checkParameter(functionDeclaration : Declaration.FunctionDeclare, args : List<Type>?, file : File) : Boolean{
        if(functionDeclaration.parameters.isNullOrEmpty() && args.isNullOrEmpty())
            return true

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


    private fun checkBodyTypes(functionName : String ,body: Body , returnType: Type? , upperVariables: HashMap<String, Type>?, file : File){

        val localVariables = body.localVariables?.let { HashMap(body.localVariables.associate { it.name to it.type}) } ?: HashMap()

        val combinedVariables = combineVariables(upperVariables,localVariables)

        body.localVariables?.forEach { lv ->
            checkVariableDeclaration(lv, combinedVariables, file)
        }

        body.functionBody.forEach { statement ->
            when(statement){
                is Statement.AssignValue -> {
                    when(statement.variableName){
                        "return" -> {
                            val type = getExpressionType( statement.expression, combinedVariables, file)
                            if(returnType != type)
                                throw TypeCheckerReturnTypeException(statement.LineOfCode, functionName ,returnType, type)
                        }
                        else -> {
                            val type = getExpressionType( statement.expression, combinedVariables, file)
                            if (combinedVariables[statement.variableName] != type)
                                throw TypeCheckerWrongTypeAssignmentException(statement.LineOfCode, statement.variableName, combinedVariables[statement.variableName] ,type)
                        }
                    }
                }
                is Statement.Block -> {
                    checkBodyTypes(functionName, statement.body, returnType ,combinedVariables, file)
                }
                is Statement.If -> {
                    val conditionType = getExpressionType(statement.condition, combinedVariables, file)
                    conditionType as? Type.Boolean ?: throw TypeCheckerConditionException(statement.LineOfCode, conditionType)

                    checkBodyTypes(functionName, statement.ifBody, returnType, combinedVariables, file)
                    statement.elseBody?.let { checkBodyTypes(functionName, it, returnType, combinedVariables, file) }
                }
                is Statement.While -> {
                    val conditionType = getExpressionType(statement.condition, combinedVariables, file)
                    conditionType as? Type.Boolean ?: throw TypeCheckerConditionException(statement.LineOfCode, conditionType)

                    checkBodyTypes(functionName, statement.body, returnType, combinedVariables, file)
                }
                is Statement.ProcedureCall -> {
                    if(statement.procedureName != "Println" && statement.procedureName != "Print")
                    {
                        val procedureList = file.functionDeclarations[statement.procedureName] ?: throw TypeCheckerFunctionNotFoundException(statement.LineOfCode, statement.procedureName)
                        procedureList.firstOrNull { checkParameter(it, statement.parameterList?.map { getExpressionType(it, HashMap(), file)}, file) }
                            ?: throw TypeCheckerFunctionParameterException(statement.LineOfCode,statement.procedureName, statement.parameterList?.map { getExpressionType(it, HashMap(), file)})
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

    private fun checkVariableDeclaration(variableDeclaration: Declaration.VariableDeclaration, localVariables : HashMap<String, Type>, file : File) {
        val type = getExpressionType(variableDeclaration.expression, localVariables, file)
        if(variableDeclaration.type != type)
            throw TypeCheckerWrongTypeAssignmentException(variableDeclaration.LineOfCode, variableDeclaration.name, variableDeclaration.type, type)
    }

    private fun getExpressionType(expression: Expression, localVariables : HashMap<String, Type>, file : File): Type {
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

                        val action = {  classDec : Declaration.ClassDeclare, importFile : File ->
                            val methodList = classDec.classBody.functions[classDec.className] ?: throw TypeCheckerConstructorNotFoundException(expression.LineOfCode, expression.functionName, classDec.className)
                            val parameterTypes = expression.parameterList?.map { getExpressionType(it, localVariables, file)}
                            methodList.firstOrNull { checkParameter(it, parameterTypes, file) } ?: throw TypeCheckerFunctionParameterException(expression.LineOfCode,expression.functionName, parameterTypes)

                            Type.Custom(classDec.className)
                        }

                        file.classDeclarations[expression.functionName]?.let {
                            return action(it,file)
                        }

                        file.includes[expression.functionName]?.let { importFile ->
                            return action(importFile.classDeclarations[expression.functionName] ?: throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, expression.functionName), importFile)
                        }

                        file.functionDeclarations[expression.functionName]?.let { funcs ->
                            val functionList = file.functionDeclarations[expression.functionName] ?: throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, expression.functionName)
                            val parameterTypes = expression.parameterList?.map { getExpressionType(it, localVariables, file)}
                            val function = functionList.firstOrNull { checkParameter(it, parameterTypes, file) } ?: throw TypeCheckerFunctionParameterException(expression.LineOfCode,expression.functionName, parameterTypes)
                            return function.returnType
                        }

                        throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, expression.functionName)
                    }
                }
            }
            is Expression.UseVariable -> {
                localVariables[expression.variableName] ?: file.variableDeclaration[expression.variableName]?.type ?: throw TypeCheckerVariableNotFoundException(expression.LineOfCode, expression.variableName)
            }
            is Expression.UseDotVariable -> {
                useDotVariable(localVariables, expression, file)
            }
            is Expression.Operation -> {
                if(expression.operator == Operator.Equals)
                    throw TypeCheckerOperationException(expression.LineOfCode, "Can't use Operator at this position", expression.operator)

                if(expression.expressionB == null){
                    return when(expression.operator){
                        Operator.Not -> {
                            val typeA = getExpressionType(expression.expressionA, localVariables, file)
                            typeA as? Type.Boolean ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode, Operator.Not,typeA)
                        }
                        Operator.Minus->  {
                            val typeA = getExpressionType(expression.expressionA, localVariables, file)
                            typeA as? Type.Integer ?: typeA as? Type.Float ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode, Operator.Minus,typeA)
                        }
                        else -> throw TypeCheckerOperationException(expression.LineOfCode, "Needs more then one Argument", expression.operator)
                    }
                }
                else{
                    val typeA = getExpressionType(expression.expressionA, localVariables, file)
                    val typeB = getExpressionType(expression.expressionB, localVariables, file)

                    return when (expression.operator){

                        Operator.And -> checkOperatorTypes<Type.Boolean>(Type.Boolean,typeA,typeB,expression, file)
                        Operator.Or -> checkOperatorTypes<Type.Boolean>(Type.Boolean,typeA,typeB,expression, file)

                        Operator.Minus -> numberOperation(typeA, typeB, expression, file)
                        Operator.Multiply -> numberOperation(typeA, typeB, expression, file)
                        Operator.Divide -> numberOperation(typeA, typeB, expression, file)
                        Operator.Less -> checkOperatorTypes2<Type.Boolean, Type.Float>(Type.Boolean,typeA,typeB,expression, file)
                        Operator.LessEqual -> checkOperatorTypes2<Type.Boolean, Type.Float>(Type.Boolean,typeA,typeB,expression, file)
                        Operator.Greater -> checkOperatorTypes2<Type.Boolean, Type.Float>(Type.Boolean,typeA,typeB,expression, file)
                        Operator.GreaterEquals -> checkOperatorTypes2<Type.Boolean, Type.Float>(Type.Boolean,typeA,typeB,expression, file)

                        Operator.Plus -> when{
                                typeA is Type.Integer && typeB is Type.Integer -> Type.Integer
                                typeA is Type.Integer || typeA is Type.Float && typeB is Type.Integer || typeB is Type.Float -> Type.Float
                                typeA is Type.String && typeB is Type.String -> Type.String
                                else -> throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeA, typeB)
                        }

                        Operator.DoubleEquals -> checkOperatorAllTypes(Type.Boolean,typeA,typeB,expression, file)
                        Operator.NotEqual -> checkOperatorAllTypes(Type.Boolean,typeA,typeB,expression, file)

                        Operator.Not -> throw TypeCheckerOperationException(expression.LineOfCode, "Needs only one Argument", expression.operator)
                        Operator.Equals -> throw TypeCheckerOperationException(expression.LineOfCode, "Can't use Operator at this position", expression.operator)

                    }
                }
            }
        }
    }

    private fun useDotVariable(localVariables: HashMap<String, Type>, expression: Expression.UseDotVariable, file : File) : Type{
        val classType = (localVariables[expression.variableName] ?: file.variableDeclaration[expression.variableName]?.type) as? Type.Custom //?: throw TypeCheckerVariableNotFoundException(expression.LineOfCode, expression.variableName)
        val classObj = file.classDeclarations[classType?.name] ?: file.includes[classType?.name]?.classDeclarations?.get(classType?.name)
        val classVariables = classObj?.classBody?.variables?.associateTo(HashMap()) { it.name to it.type } ?: HashMap()

        return when (val expression2 = expression.expression){
            is Expression.UseVariable -> {
                classObj ?: throw TypeCheckerClassNotFoundException(expression.LineOfCode, expression.variableName)
                classVariables[expression2.variableName] ?: throw TypeCheckerVariableNotFoundException(expression.LineOfCode, expression.variableName)
            }
            is Expression.UseDotVariable -> useDotVariable(classVariables ,expression2, file)
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
                        val parameterTypes = expression2.parameterList?.map{ getExpressionType(it, localVariables, file)}
                        val function = functionList.firstOrNull { checkParameter(it, parameterTypes, file) } ?: throw TypeCheckerFunctionParameterException(expression.LineOfCode,expression2.functionName, parameterTypes)
                        return function.returnType
                    }
                }
            }
            else -> throw TypeCheckerCantUseOperationOnDot(expression2.LineOfCode, expression2)
        }
    }

    private fun numberOperation(typeA: Type, typeB: Type, expression: Expression.Operation, file : File)
    = when {
        typeA is Type.Integer && typeB is Type.Integer -> Type.Integer
        typeA is Type.Integer || typeA is Type.Float && typeB is Type.Integer || typeB is Type.Float -> Type.Float
        else -> throw TypeCheckerOperatorTypeException(expression.LineOfCode, expression.operator, typeA, typeB)
    }

    private fun <T> checkOperatorTypes(outputType : Type ,typeA : Type, typeB : Type, expression: Expression.Operation, file : File): Type {
        typeA as? T ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeA)
        typeB as? T ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeB)
        return outputType
    }

    private fun <T,D> checkOperatorTypes2(outputType : Type ,typeA : Type, typeB : Type, expression: Expression.Operation, file : File): Type {
        typeA as? T ?: typeA as? D ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeA)
        typeB as? T ?: typeB as? D ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeB)
        return outputType
    }

    private fun checkOperatorAllTypes(outputType : Type ,typeA : Type, typeB : Type, expression: Expression.Operation, file : File): Type {
        if(typeA == typeB)
            return outputType
        throw TypeCheckerOperatorTypeException(expression.LineOfCode ,expression.operator, typeA, typeB)
    }

}