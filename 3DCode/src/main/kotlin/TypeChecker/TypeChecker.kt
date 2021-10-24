package TypeChecker

import Parser.ParserToken.*
import TypeChecker.Exceptions.*

class TypeChecker {

    private val checkedFiles = mutableListOf<String>()

    fun check(mainFile : File, args : List<Expression.Value>?) {
        checkFile(mainFile)

        if(mainFile.functionDeclarations["Main"].isNullOrEmpty())
            throw TypeCheckerFunctionNotFoundException(-1, mainFile.name, "Main")

        mainFile.functionDeclarations["Main"]?.let { mainFunctionList ->

            if(mainFunctionList.count() != 1)
                throw TypeCheckerOnlyOneMainException(mainFunctionList.first().LineOfCode, mainFile.name)

            val mainFunction = mainFunctionList.first()

            val a = args?.map { getExpressionType(it, HashMap(), mainFile)}
            if(!checkParameter(mainFunction.parameters, null, a))
                throw TypeCheckerFunctionParameterException(mainFunction.LineOfCode, mainFile.name ,mainFunction.functionName,a,"function")

            checkFunctionDeclaration(mainFunction, mainFile)
        }

    }

    private fun checkFile(file: File){

        file.includes.forEach { (t, f) ->
            f ?: throw TypeCheckerFileNotFoundException(file.name)
            if(checkedFiles.contains(file.name))
                checkFile(f)
        }

        file.variableDeclaration.forEach { (_, d) ->
            checkVariableDeclaration(d, HashMap(), file)
        }

        file.functionDeclarations.forEach { (_, functions) ->
            functions.forEach{ function ->
                if(function.functionName != "Main"){
                    checkFunctionDeclaration(function, file)

                    if(functions.count { functionLocal -> checkForDuplicateFunction(function, functionLocal) } >= 2)
                        throw TypeCheckerDuplicateFunctionException(function.LineOfCode, file.name, function)
                }
            }
        }

        file.classDeclarations.forEach { (_, c) ->
            checkClassDeclaration(c, file)
        }
    }

    private fun checkForDuplicateFunction(function : Declaration.FunctionDeclare, functionLocal : Declaration.FunctionDeclare) : Boolean {
        return if(functionLocal.parameters.isNullOrEmpty() || function.parameters.isNullOrEmpty())
            functionLocal.parameters.isNullOrEmpty() && function.parameters.isNullOrEmpty()
        else
            functionLocal.parameters.size == function.parameters.size && functionLocal.parameters.zip(function.parameters.orEmpty() ).all{ it.first.type == it.second.type }
    }


    private fun checkClassDeclaration(classDef: Declaration.ClassDeclare, file : File) {

        val localVariables = classDef.classBody.variables?.let { HashMap(classDef.classBody.variables.associate { it.name to it.type}) } ?: HashMap()

        classDef.classBody.variables?.forEach { v ->
            checkVariableDeclaration(v,localVariables, file)
        }

        classDef.classBody.functions.forEach{ (_, functions) ->
            functions.forEach{ function ->
                checkMethodDeclaration(function, localVariables, file)

                if(functions.count { functionLocal -> checkForDuplicateFunction(function, functionLocal)} >= 2) throw TypeCheckerDuplicateFunctionException(function.LineOfCode, file.name, function)
            }
        }

        if(classDef.classBody.functions[classDef.className].isNullOrEmpty())
            throw TypeCheckerConstructorNotFoundException(classDef.LineOfCode, file.name, classDef.className, classDef.className)

        classDef.classBody.functions[classDef.className]?.forEach {
            if(it.returnType != Type.Void)
                throw TypeCheckerReturnTypeException(it.LineOfCode, file.name, classDef.className)
        }
    }

    private fun checkFunctionDeclaration(functionDeclaration : Declaration.FunctionDeclare, file : File){
        checkBodyTypes(functionDeclaration.functionName ,functionDeclaration.body, functionDeclaration.returnType, functionDeclaration.parameters?.associate{it.name to it.type}?.let { HashMap(it)} ?: HashMap(), file)
    }

    private fun checkMethodDeclaration(functionDeclaration : Declaration.FunctionDeclare, classVariables : HashMap<String, Type>, file : File){
        val variables = combineVariables(classVariables,functionDeclaration.parameters?.associate{it.name to it.type}?.let { HashMap(it)} ?: HashMap(), file)
        checkBodyTypes(functionDeclaration.functionName ,functionDeclaration.body, functionDeclaration.returnType, variables, file)
    }

    private fun checkParameter(parameters: List<Parameter>?, generics: List<String>?, args: List<Type>?) : Boolean{
        if(parameters.isNullOrEmpty() && args.isNullOrEmpty())
            return true

        if(parameters?.size != args?.size)
            return false

        if(parameters.isNullOrEmpty() && args.isNullOrEmpty())
            return true

        val parameterCombined = parameters!!.zip(args.orEmpty()){ fp, p -> fp to p }

        return parameterCombined.fold(true) { acc, e ->
            if(!acc)
                return false
            e.first.type == e.second || generics?.contains((e.first.type as? Type.Custom)?.name ) ?: false
        }
    }

    private fun checkBodyTypes(functionName : String ,body: Body , returnType: Type? , upperVariables: HashMap<String, Type>?, file : File){

        val localVariables = body.localVariables?.let { HashMap(body.localVariables.associate { it.name to it.type}) } ?: HashMap()

        val combinedVariables = combineVariables(upperVariables,localVariables, file)

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
                                throw TypeCheckerReturnTypeException(statement.LineOfCode, file.name, functionName ,returnType, type)
                        }
                        else -> {
                            val type = getExpressionType( statement.expression, combinedVariables, file)
                            if (combinedVariables[statement.variableName] != type)
                                throw TypeCheckerWrongTypeAssignmentException(statement.LineOfCode, file.name, statement.variableName, combinedVariables[statement.variableName] ,type)
                        }
                    }
                }
                is Statement.Block -> {
                    checkBodyTypes(functionName, statement.body, returnType ,combinedVariables, file)
                }
                is Statement.If -> {
                    val conditionType = getExpressionType(statement.condition, combinedVariables, file)
                    conditionType as? Type.Boolean ?: throw TypeCheckerConditionException(statement.LineOfCode, file.name, conditionType)

                    checkBodyTypes(functionName, statement.ifBody, returnType, combinedVariables, file)
                    statement.elseBody?.let { checkBodyTypes(functionName, it, returnType, combinedVariables, file) }
                }
                is Statement.While -> {
                    val conditionType = getExpressionType(statement.condition, combinedVariables, file)
                    conditionType as? Type.Boolean ?: throw TypeCheckerConditionException(statement.LineOfCode, file.name, conditionType)

                    checkBodyTypes(functionName, statement.body, returnType, combinedVariables, file)
                }
                is Statement.ProcedureCall -> {
                    if(statement.procedureName != "Println" && statement.procedureName != "Print")
                    {
                        val procedureList = file.functionDeclarations[statement.procedureName] ?: throw TypeCheckerFunctionNotFoundException(statement.LineOfCode, file.name, statement.procedureName)
                        procedureList.firstOrNull { procedure -> checkParameter(
                            procedure.parameters,
                            procedure.generics,
                            statement.parameterList?.map { getExpressionType(it, HashMap(), file)}) }
                            ?: throw TypeCheckerFunctionParameterException(statement.LineOfCode, file.name, statement.procedureName, statement.parameterList?.map { getExpressionType(it, HashMap(), file)}, "procedure")
                    }
                }
                is Statement.UseClass -> statementUseClass(statement, combinedVariables, file)
            }
        }


    }

    private fun combineVariables(upperEnvironment : HashMap<String, Type>?,lowerEnvironment : HashMap<String, Type>, file: File) : HashMap<String, Type>{
        if (upperEnvironment == null)
            return lowerEnvironment
        return HashMap((upperEnvironment.keys + lowerEnvironment.keys).associateWith { k -> lowerEnvironment[k] ?: upperEnvironment[k] ?: throw TypeCheckerVariableNotFoundException(-1, file.name, k) })
    }

    private fun checkVariableDeclaration(variableDeclaration: Declaration.VariableDeclaration, localVariables : HashMap<String, Type>, file : File) {
        val type = getExpressionType(variableDeclaration.expression, localVariables, file)

        if(variableDeclaration.type != type && type !is Type.Null)
            throw TypeCheckerWrongTypeAssignmentException(variableDeclaration.LineOfCode, file.name, variableDeclaration.name, variableDeclaration.type, type)

    }

    private fun statementUseClass(statement : Statement.UseClass, localVariables : HashMap<String, Type>, file : File){

        if(!(localVariables.containsKey(statement.variableName) || file.variableDeclaration.containsKey(statement.variableName)))
            throw TypeCheckerVariableNotFoundException(statement.LineOfCode,file.name, statement.variableName)

        val classType = localVariables[statement.variableName] ?: file.variableDeclaration[statement.variableName]?.type

        val action = { classObj : Declaration.ClassDeclare, importFile : File ->
            when(val statement2 = statement.statement){
                is Statement.AssignValue -> {
                    val variableDef = classObj.classBody.variables?.firstOrNull{it.name == statement2.variableName} ?: throw TypeCheckerVariableNotFoundException(statement2.LineOfCode, importFile.name, statement2.variableName)

                    val type1 = getExpressionType(statement2.expression, localVariables,importFile)
                    var type2 = variableDef.type

                    if(classType is Type.CustomWithGenerics && variableDef.type is Type.AbstractCustom)
                        type2 = getClassGenericType(classType, variableDef.type, classObj) ?: throw TypeCheckerGenericsMissingException(statement.LineOfCode,file.name,"Procedure")

                    if(type2 != type1){
                        throw TypeCheckerWrongTypeAssignmentException(statement2.LineOfCode, importFile.name, statement2.variableName, variableDef.type, type1)
                    }
                }
                is Statement.ProcedureCall -> {
                    val functionList = classObj.classBody.functions[statement2.procedureName] ?: throw TypeCheckerFunctionNotFoundException(statement2.LineOfCode, importFile.name, statement2.procedureName)
                    val parameterTypes = statement2.parameterList?.map{ getExpressionType(it, localVariables, importFile)}

                    val function = functionList.firstOrNull { checkParameter(
                        it.parameters,
                        combineGenerics(it.generics, classObj.generics),
                        parameterTypes
                    ) } ?: throw TypeCheckerFunctionParameterException(statement2.LineOfCode, importFile.name, statement2.procedureName, parameterTypes, "method")
                }
                is Statement.UseClass ->{
                    statementUseClass(statement2, classObj.classBody.variables?.associateTo(hashMapOf()) { it.name to it.type } ?: hashMapOf(), importFile)
                }
                else -> throw Exception("Can't use $statement2 in this context")
            }
        }

        (classType as? Type.AbstractCustom)?.let {

            file.classDeclarations[classType.name]?.let {
                action(it, file)
                return
            }

            file.includes[classType.name]?.let { importFile ->
                val classObj = importFile.classDeclarations[classType.name] ?: throw TypeCheckerClassNotFoundException(-1,importFile.name, classType.name)
                action(classObj, importFile)
                return
            }
        }

        throw Exception("Couldn't find class named: $classType")
    }

    private fun getExpressionType(expression: Expression, localVariables : HashMap<String, Type>, file : File): Type {
        return when(expression){
            is Expression.Value -> expression.value.getType()
            is Expression.FunctionCall -> {
                when(expression.functionName){
                    "ToString" -> {
                        if(expression.parameterList == null)
                            throw TypeCheckerFunctionParameterException(expression.LineOfCode, file.name, "Function: 'ToString' must have one or more transfer parameters")
                        Type.String
                    }
                    else -> {

                        val action = { classDec : Declaration.ClassDeclare, _: File ->
                            val methodList = classDec.classBody.functions[classDec.className] ?: throw TypeCheckerConstructorNotFoundException(expression.LineOfCode, file.name, expression.functionName, classDec.className)
                            val parameterTypes = expression.parameterList?.map { getExpressionType(it, localVariables, file)}
                            val method = methodList.firstOrNull { functionDeclaration ->
                                val generics  = combineGenerics(functionDeclaration.generics, classDec.generics)
                                checkParameter(functionDeclaration.parameters, generics, parameterTypes) } ?: throw TypeCheckerFunctionParameterException(expression.LineOfCode, file.name, expression.functionName, parameterTypes, "constructor" )

                            if(classDec.generics?.size != expression.generics?.size && method.generics?.size != expression.generics?.size)
                                throw TypeCheckerGenericsMissingException(expression.LineOfCode, file.name, "constructor")

                            if(classDec.generics.isNullOrEmpty() || classDec.generics.isEmpty())
                                Type.Custom(classDec.className)
                            else{
                                expression.generics ?: throw TypeCheckerGenericsMissingException(expression.LineOfCode,file.name, "Constructor")
                                Type.CustomWithGenerics(classDec.className,expression.generics)
                            }

                        }

                        file.classDeclarations[expression.functionName]?.let {
                            return action(it,file)
                        }

                        file.includes[expression.functionName]?.let { importFile ->
                            return action(importFile.classDeclarations[expression.functionName] ?: throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, file.name, expression.functionName), importFile)
                        }

                        file.functionDeclarations[expression.functionName]?.let { _ ->
                            val functionList = file.functionDeclarations[expression.functionName] ?: throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, file.name, expression.functionName)
                            val parameterTypes = expression.parameterList?.map { getExpressionType(it, localVariables, file)}
                            val function = functionList.firstOrNull { checkParameter(
                                it.parameters,
                                it.generics,
                                parameterTypes
                            ) }
                                ?: throw TypeCheckerFunctionParameterException(expression.LineOfCode, file.name, expression.functionName, parameterTypes, "function")

                            if(function.generics?.size != expression.generics?.size)
                                throw TypeCheckerGenericsMissingException(expression.LineOfCode, file.name, "function")

                            expression.generics?.let {
                                function.generics ?: throw TODO()
                                if(function.returnType is Type.Custom && function.generics.contains(function.returnType.name))
                                    return expression.generics[function.generics.indexOf(function.returnType.name)]
                            }

                            return function.returnType
                        }

                        throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, file.name, expression.functionName)
                    }
                }
            }
            is Expression.UseVariable -> {
                localVariables[expression.variableName] ?: file.variableDeclaration[expression.variableName]?.type ?: throw TypeCheckerVariableNotFoundException(expression.LineOfCode, file.name, expression.variableName)
            }
            is Expression.UseDotVariable -> {
                useDotVariable(localVariables, expression, null, file)
            }
            is Expression.Operation -> {
                if(expression.operator == Operator.Equals)
                    throw TypeCheckerOperationException(expression.LineOfCode, file.name, "Can't use Operator at this position", expression.operator)

                if(expression.expressionB == null){
                    return when(expression.operator){
                        Operator.Not -> {
                            val typeA = getExpressionType(expression.expressionA, localVariables, file)
                            typeA as? Type.Boolean ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode, file.name, Operator.Not,typeA)
                        }
                        Operator.Minus->  {
                            val typeA = getExpressionType(expression.expressionA, localVariables, file)
                            typeA as? Type.Integer ?: typeA as? Type.Float ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode, file.name, Operator.Minus,typeA)
                        }
                        else -> throw TypeCheckerOperationException(expression.LineOfCode, file.name, "Needs more then one Argument", expression.operator)
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
                                (typeA is Type.Integer || typeA is Type.Float) && (typeB is Type.Integer || typeB is Type.Float) -> Type.Float
                                typeA is Type.String && typeB is Type.String -> Type.String
                                else -> throw TypeCheckerOperatorTypeException(expression.LineOfCode, file.name ,expression.operator, typeA, typeB)
                        }

                        Operator.DoubleEquals -> checkOperatorAllTypes(Type.Boolean,typeA,typeB,expression, file)
                        Operator.NotEqual -> checkOperatorAllTypes(Type.Boolean,typeA,typeB,expression, file)

                        Operator.Not -> throw TypeCheckerOperationException(expression.LineOfCode, file.name, "Needs only one Argument", expression.operator)
                        Operator.Equals -> throw TypeCheckerOperationException(expression.LineOfCode, file.name, "Can't use Operator at this position", expression.operator)

                    }
                }
            }
        }
    }

    private fun useDotVariable(localVariables: HashMap<String, Type>, expression: Expression.UseDotVariable, upperType: Type.CustomWithGenerics?, file : File) : Type{
        if(!(localVariables.containsKey(expression.variableName) || file.variableDeclaration.containsKey(expression.variableName)))
            throw TypeCheckerVariableNotFoundException(expression.LineOfCode,file.name, expression.variableName)

        val classType = localVariables[expression.variableName] ?: file.variableDeclaration[expression.variableName]?.type

        fun action(classObj : Declaration.ClassDeclare , importFile : File) : Type
        {
            return when (val expression2 = expression.expression){
                is Expression.UseVariable -> {
                    var returnType = classObj.classBody.variables?.associateTo(HashMap()) { it.name to it.type }?.get(expression2.variableName) ?: throw TypeCheckerVariableNotFoundException(expression.LineOfCode, importFile.name, expression.variableName)

                    if(returnType is Type.AbstractCustom && classType is Type.CustomWithGenerics && classObj.generics?.contains(returnType.name) == true){

                        returnType = getClassGenericType(classType, returnType, classObj) ?: throw TypeCheckerGenericsMissingException(expression.LineOfCode,file.name,"Class")

                        if(upperType != null && returnType is Type.Custom)
                            returnType = getClassGenericType(upperType, returnType, classObj) ?: throw TypeCheckerGenericsMissingException(expression.LineOfCode,file.name,"Class")

                    }

                    return returnType
                }
                is Expression.UseDotVariable -> {
                    useDotVariable(classObj.classBody.variables?.associateTo(HashMap()) { it.name to it.type } ?: HashMap() ,expression2, upperType?: classType as? Type.CustomWithGenerics, importFile)
                }
                is Expression.FunctionCall -> {
                    val functionList = classObj.classBody.functions[expression2.functionName] ?: throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, importFile.name, expression2.functionName)
                    val parameterTypes = expression2.parameterList?.map{ getExpressionType(it, localVariables, importFile)}
                    val function = functionList.firstOrNull { checkParameter(it.parameters, it.generics, parameterTypes) } ?: throw TypeCheckerFunctionParameterException(expression.LineOfCode, importFile.name, expression2.functionName, parameterTypes, "method")

                    if(function.returnType is Type.AbstractCustom){
                        if (expression2.generics != null && function.generics != null && function.returnType is Type.Custom && function.generics.contains(function.returnType.name))
                            return expression2.generics[function.generics.indexOf(function.returnType.name)]

                        if(classType is Type.CustomWithGenerics )
                            return getClassGenericType(classType, function.returnType, classObj) ?: throw TypeCheckerGenericsMissingException(expression.LineOfCode,file.name,"Function")
                    }

                    return function.returnType
                }
                else -> throw TypeCheckerCantUseOperationOnDot(expression2.LineOfCode, file.name, expression2)
            }
        }

        (classType as? Type.AbstractCustom)?.let {

            file.classDeclarations[classType.name]?.let {
                return action(it, file)
            }

            file.includes[classType.name]?.let { importFile ->
                val classObj = importFile.classDeclarations[classType.name] ?: throw TypeCheckerClassNotFoundException(-1,importFile.name, classType.name)
                return action(classObj, importFile)
            }
        }

        if(expression.expression is Expression.FunctionCall && (expression.expression as Expression.FunctionCall).functionName == "ToString"){
            if((expression.expression as Expression.FunctionCall).parameterList != null)
                throw TypeCheckerFunctionParameterException(expression.LineOfCode, file.name, "Function: 'ToString' must have one or more transfer parameters")
            return Type.String
        }

        throw TypeCheckerCantUseOperationOnDot(expression.LineOfCode, file.name, expression)
    }

    private fun combineGenerics(generics1 : List<String>?,generics2 : List<String>?) : List<String>?{
        if(generics1.isNullOrEmpty() && generics2.isNullOrEmpty())
            return null
        return listOf<String>().union(generics1 ?: listOf()).union(generics2 ?: listOf()).toList()
    }

    private fun getClassGenericType(
        type: Type.CustomWithGenerics,
        functionReturnType: Type.AbstractCustom,
        classDef: Declaration.ClassDeclare
    ) : Type?{

        val genericPosition = classDef.generics?.indexOf(functionReturnType.name) ?: return null
        if(genericPosition == -1)
            return null
        return type.generics[genericPosition]
    }

    private fun numberOperation(typeA: Type, typeB: Type, expression: Expression.Operation, file : File)
    = when {
        typeA is Type.Integer && typeB is Type.Integer -> Type.Integer
        (typeA is Type.Integer || typeA is Type.Float) && (typeB is Type.Integer || typeB is Type.Float) -> Type.Float
        else -> throw TypeCheckerOperatorTypeException(expression.LineOfCode, file.name, expression.operator, typeA, typeB)
    }

    private fun <T> checkOperatorTypes(outputType : Type ,typeA : Type, typeB : Type, expression: Expression.Operation, file : File): Type {
        typeA as? T ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode, file.name, expression.operator, typeA)
        typeB as? T ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode, file.name, expression.operator, typeB)
        return outputType
    }

    private fun <T,D> checkOperatorTypes2(outputType : Type ,typeA : Type, typeB : Type, expression: Expression.Operation, file : File): Type {
        typeA as? T ?: typeA as? D ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode, file.name, expression.operator, typeA)
        typeB as? T ?: typeB as? D ?: throw TypeCheckerOperatorTypeException(expression.LineOfCode, file.name, expression.operator, typeB)
        return outputType
    }

    private fun checkOperatorAllTypes(outputType : Type ,typeA : Type, typeB : Type, expression: Expression.Operation, file : File): Type {
        if(typeA == typeB || typeA is Type.Null || typeB is Type.Null)
            return outputType
        throw TypeCheckerOperatorTypeException(expression.LineOfCode, file.name, expression.operator, typeA, typeB)
    }

}