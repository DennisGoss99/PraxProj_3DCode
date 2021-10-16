package TypeChecker

import Evaluator.Exceptions.NotFound.FunctionNotFoundRuntimeException
import Evaluator.Exceptions.NotFound.VariableNotFoundRuntimeException
import Parser.ParserToken.*
import TypeChecker.Exceptions.*

class TypeChecker(private val declarations: List<Declaration>, private val args : List<Expression.Value>? = null) {

    private val functionDeclarations = HashMap<String, MutableList<Declaration.FunctionDeclare>>()
    private val globalVariableDeclarations = HashMap<String, Declaration.VariableDeclaration>()

    fun check(){

        declarations.forEach { d ->
            when(d){
                is Declaration.FunctionDeclare -> functionDeclarations.getOrPut(d.functionName, ::mutableListOf).add(d)
                is Declaration.VariableDeclaration -> {
                    checkVariableDeclaration(d, HashMap())
                    globalVariableDeclarations[d.name] = d
                }
            }
        }

        val mainFunctionList = functionDeclarations["Main"] ?: throw FunctionNotFoundRuntimeException("Main")
        if(mainFunctionList.count() != 1)
            throw TypeCheckerOnlyOneMainException(mainFunctionList.first().LineOfCode)

        val mainFunction = mainFunctionList.first()

        val a = args?.map { getExpressionType(it, HashMap())}
        if(!checkParameter(mainFunction, a))
            throw TypeCheckerFunctionParameterException(mainFunction.LineOfCode ,mainFunction.functionName,a)

        checkFunctionDeclaration(mainFunction)

        functionDeclarations.forEach { f ->
            if(f.key != "Main"){
                f.value.forEach { func -> checkFunctionDeclaration(func) }
            }
        }

    }

    private fun checkFunctionDeclaration(functionDeclaration : Declaration.FunctionDeclare){
        checkBodyTypes(functionDeclaration.functionName ,functionDeclaration.body, functionDeclaration.returnType, functionDeclaration.parameters?.associate{it.name to it.type}?.let { HashMap(it)} ?: HashMap())
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


        val localVariables = body.localVariables?.let { HashMap(body.localVariables?.associate { it.name to it.type}) } ?: HashMap()

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
                        val functionList = functionDeclarations[expression.functionName] ?: throw TypeCheckerFunctionNotFoundException(expression.LineOfCode, expression.functionName)
                        val parameterTypes = expression.parameterList?.map { getExpressionType(it, localVariables)}
                        val function = functionList.firstOrNull { checkParameter(it, parameterTypes) }
                            ?: throw TypeCheckerFunctionParameterException(expression.LineOfCode,expression.functionName, parameterTypes)

                        function.returnType
                    }
                }
            }
            is Expression.UseVariable -> {
                globalVariableDeclarations[expression.variableName]?.type ?: localVariables[expression.variableName] ?: throw TypeCheckerVariableNotFoundException(expression.LineOfCode, expression.variableName)
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
            is Expression.UseDotVariable -> TODO()
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