package Parser

import Lexer.Lexer
import Lexer.LexerToken
import Parser.Exception.*
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue

class Parser(val lexer: Lexer, val fileName : String)
{
    private var currentLineOfCode = 1
    private var _currentBlockDepth = 0

    private inline fun <reified A> fetchNextExpectedToken(expectedType: String): A {
        val next = getNextToken()

        if (next !is A) {
            throw ParserTokenUnexpected(expectedType, next, fileName)
        }

        return next
    }

    private fun getNextToken() : LexerToken
    {
        val token = lexer.next()
        currentLineOfCode = token.LineOfCode

        return token
    }

    fun parsingStart() : List<Declaration>
    {
        val declarationList : MutableList<Declaration> = mutableListOf()

        try
        {
            declarationListParse().forEach { declarationList.add(it) }
        }
        catch(e : Exception)
        {
            e.printStackTrace()
        }

        if(lexer.peek() is LexerToken.EOF)
        {
            getNextToken()
        }
        else
        {

            while(true)
            {
                val token = getNextToken()

                if(token is LexerToken.EOF)
                    break
            }
        }

        return declarationList
    }

    private fun declarationParse() : Declaration
    {
        return when(val nextToken = lexer.peek()){
            is LexerToken.Class -> classParse()
            is LexerToken.TypeIdent->{
                val type = typeParse()
                when(val nextToken2 = lexer.peek())
                {
                    is LexerToken.NameIdent -> variableDeclaration(type)
                    is LexerToken.FunctionIdent -> functionParse(type)

                    else -> throw ParserDeclarationTokenInvalid(nextToken2, fileName)
                }
            }
            // Constructor Parse
            is LexerToken.FunctionIdent -> functionParse(Type.Void)

            is LexerToken.NameIdent -> throw ParserTypeLowerCase(nextToken, fileName)
            else -> throw ParserTypeUnknown(nextToken, fileName)
        }
    }

    private fun declarationListParse() : List<Declaration>
    {
        val declarationList = mutableListOf<Declaration>()

        while(true)
        {
            val token = lexer.peek()

            if(token is LexerToken.EOF)
            {
                break
            }

            declarationList.add(declarationParse())
        }

        return declarationList
    }

    private fun operatorStrength(operator: Operator) : Pair<Int, Int>
    {
        return when(operator)
        {
            Operator.Not ->  1 to 2

            Operator.Multiply,
            Operator.Divide -> 3 to 4

            Operator.Plus,
            Operator.Minus -> 5 to 6

            Operator.Less,
            Operator.LessEqual,
            Operator.Greater,
            Operator.GreaterEquals -> 7 to 8


            Operator.DoubleEquals,
            Operator.NotEqual -> 9 to 10

            Operator.And -> 10 to 11
            Operator.Or -> 12 to 13

            Operator.Equals -> 14 to 15
        }
    }

    private fun nameParse(): String {
        val token = getNextToken()

        if (token is LexerToken.Return) {
            return "return"
        }

        if (token !is LexerToken.NameIdent) {
            throw ParserInvalidName(token, fileName)
        }

        return token.identify
    }

    private fun localVariablesParse() : List<Declaration.VariableDeclaration>?
    {
        val localVariableList = mutableListOf<Declaration.VariableDeclaration>()

        while(true)
        {
            val token = lexer.peek()

            if(token is LexerToken.Return || token !is LexerToken.TypeIdent) // return if there is no data or no "int a" stuff
            {
                break
            }

            val variableDeclaration = variableDeclaration(typeParse())

            localVariableList.add(variableDeclaration)
        }

        if(localVariableList.isEmpty())
        {
            return null
        }

        return localVariableList
    }

    private fun statementListParse(): List<Statement>
    {
        val statementList = mutableListOf<Statement>()

        while(true)
        {
            when(lexer.peek())
            {
                is LexerToken.Semicolon, // Expresion ended
                is LexerToken.RCurlyBrace -> break // Body has ended
                is LexerToken.Rparen ->
                {
                    getNextToken()

                    while(lexer.peek() is LexerToken.Semicolon)
                    {
                        getNextToken()
                    }

                    break
                }
                else -> statementList.add(statementParse())
            }
        }

        return statementList
    }

    private fun bodyParse(): Body
    {
        fetchNextExpectedToken<LexerToken.LCurlyBrace>("LCurlyBrace")
        val localVariables = localVariablesParse()
        val statementList = statementListParse()
        fetchNextExpectedToken<LexerToken.RCurlyBrace>("RCurlyBrace")

        return Body(statementList, localVariables)
    }

    private fun classBodyParse(): ClassBody
    {
        fetchNextExpectedToken<LexerToken.LCurlyBrace>("LCurlyBrace")

        val localVariables = mutableListOf<Declaration.VariableDeclaration>()
        val methodsList = HashMap<String, MutableList<Declaration.FunctionDeclare>>()
        // TODO: 13.10.2021 ; ClassList

        while(true)
        {
            val token = lexer.peek()

            if(token is LexerToken.RCurlyBrace)
            {
                break
            }

            when(val declaration = declarationParse()){
                is Declaration.VariableDeclaration -> localVariables.add(declaration)
                is Declaration.FunctionDeclare -> methodsList.getOrPut(declaration.functionName, ::mutableListOf).add(declaration)
                else -> ParserUnsupportedDeclaration(declaration, fileName)
            }
        }

        fetchNextExpectedToken<LexerToken.RCurlyBrace>("RCurlyBrace")

        return ClassBody(methodsList, localVariables)
    }


    private fun parameterParse() : Parameter
    {
        val type = typeParse()
        val name = nameParse()

        return Parameter(name, type)
    }

    private fun parameterParseAsDeclaration(): List<Parameter>?
    {
        if(lexer.peek() is LexerToken.NameIdent)
            throw ParserBaseException(currentLineOfCode, fileName, "Generic class variables must have <> after the name. 'Maybe try: TYPE name<TYPE,..> = ..'")

        val parameterList = mutableListOf<Parameter>()
        fetchNextExpectedToken<LexerToken.Lparen>("Lparen")

        while (true) {
            val token = lexer.peek()

            //isAtEndOfParameter
            if (token is LexerToken.Rparen) {
                getNextToken()
                break
            }

            //isSeparator
            if(token is LexerToken.Comma)
                getNextToken()

            parameterList.add(parameterParse())
        }

        if (parameterList.size == 0) {
            return null
        }

        return parameterList
    }

    private fun functionIdentifyParse() : String
    {
        val name = fetchNextExpectedToken<LexerToken.FunctionIdent>("function identifier")
        return name.identify
    }

    private fun classIdentifyParse() : String
    {
        val name = fetchNextExpectedToken<LexerToken.TypeIdent>("class identifier")
        return name.identify
    }

    private fun functionParse(type : Type): Declaration.FunctionDeclare
    {
        val currentLineOfCode = lexer.peek().LineOfCode
        val name = functionIdentifyParse()
        val generics = genericsParse()
        val parameter = parameterParseAsDeclaration()
        val body = bodyParse()

        return Declaration.FunctionDeclare(type, name, body,parameter, generics,currentLineOfCode)
    }

    private fun classParse(): Declaration.ClassDeclare{

        fetchNextExpectedToken<LexerToken.Class>("Class")
        val generics = genericsParse()
        val currentLineOfCode = lexer.peek().LineOfCode
        val name = classIdentifyParse()
        val body = classBodyParse()
        return Declaration.ClassDeclare(name, body, generics,currentLineOfCode)
    }

    private fun genericCall() : List<Type>?{
        if(lexer.peek() !is LexerToken.Less)
            return null

        val lessSymbol = fetchNextExpectedToken<LexerToken.Less>("<")

        val generics = mutableListOf<Type>()

        while (true){
            when(val token = lexer.peek()){
                is LexerToken.TypeIdent -> generics.add(typeParse())
                is LexerToken.Comma -> fetchNextExpectedToken<LexerToken.Comma>(",")

                is LexerToken.EOF -> throw ParserMissingGenericBracketException(lessSymbol.LineOfCode, fileName)
                is LexerToken.Greater -> {
                    fetchNextExpectedToken<LexerToken.Greater>(">")
                    return generics
                }

                is LexerToken.GreaterEqual -> throw ParserBaseException(currentLineOfCode,fileName, "Token <GreaterEqual> was not expected. 'Maybe put space between > and ='")
                else -> throw ParserTokenUnexpected(token, fileName)
            }
        }
    }

    private fun genericsParse() : List<String>?{
        if(lexer.peek() !is LexerToken.Less)
            return null
        val lessSymbol = fetchNextExpectedToken<LexerToken.Less>("<")

        val generics = mutableListOf<String>()

        while (true){
            when(val nextToken = getNextToken()){
                is LexerToken.TypeIdent -> generics.add(nextToken.identify)
                is LexerToken.Comma -> {}

                is LexerToken.EOF -> throw ParserMissingGenericBracketException(lessSymbol.LineOfCode, fileName)
                is LexerToken.Greater -> return generics
            }
        }
    }

    private fun operatorParse() : Operator
    {
        return when(val token = getNextToken())
        {
            is LexerToken.Plus -> Operator.Plus
            is LexerToken.Minus -> Operator.Minus
            is LexerToken.Mul -> Operator.Multiply
            is LexerToken.Div -> Operator.Divide
            is LexerToken.Double_Equals-> Operator.DoubleEquals

            is LexerToken.And-> Operator.And
            is LexerToken.Or-> Operator.Or
            is LexerToken.Not-> Operator.Not
            is LexerToken.NotEqual-> Operator.NotEqual
            is LexerToken.Less-> Operator.Less
            is LexerToken.LessEqual-> Operator.LessEqual
            is LexerToken.Greater-> Operator.Greater
            is LexerToken.GreaterEqual -> Operator.GreaterEquals

            else -> throw ParserOperatorUnknown(token, fileName)
        }
    }

    private fun calculationParse(leftExpression : Expression) : Expression.Operation
    {
        val operator = operatorParse()
        val rightExpression = expressionParse()

       return calculationSort(operator, leftExpression, rightExpression)
    }

    private fun calculationSort(operator: Operator, expressionA: Expression, expressionB: Expression) : Expression.Operation
    {
        val operatorCurrentStrength = operatorStrength(operator)

        val sameBlockDepth = (expressionA.BlockDepth == expressionB.BlockDepth)// || (expressionA.BlockDepth == -1)
        val bothExpressionsAreOperator =
            (expressionA is Expression.Value || expressionA is Expression.UseVariable)
                    &&
            expressionB is Expression.Operation

        if(bothExpressionsAreOperator && sameBlockDepth)
        {
            val opB = expressionB as Expression.Operation

            val operatorB = opB.operator
            val operatorBStrength = operatorStrength(operatorB)

            val isAHigher = operatorCurrentStrength.first < operatorBStrength.first

            if(isAHigher)
            {
                return Expression.Operation(
                    operatorB,
                    Expression.Operation(
                        operator,
                        expressionA,
                        expressionB.expressionA,
                        currentLineOfCode
                    ),
                    expressionB.expressionB,
                    currentLineOfCode
                )
            }
        }

        val newOperation = Expression.Operation(operator, expressionA, expressionB,currentLineOfCode)
        newOperation.BlockDepth = minOf(expressionA.BlockDepth, expressionB.BlockDepth)

        return newOperation

    }

    private fun valueParse() : Expression.Value
    {
        val expression = when(val token = getNextToken())
        {
            is LexerToken.Number_Literal -> { Expression.Value(ConstantValue.Integer(token.n, Type.Integer)) }
            is LexerToken.Float_Literal -> {Expression.Value(ConstantValue.Float(token.f, Type.Float))}
            is LexerToken.Boolean_Literal -> { Expression.Value(ConstantValue.Boolean(token.b, Type.Boolean)) }
            is LexerToken.Char_Literal -> { Expression.Value(ConstantValue.Char(token.c, Type.Char)) }
            is LexerToken.String_Literal -> { Expression.Value(ConstantValue.String(token.s, Type.String)) }
            is LexerToken.Null -> { Expression.Value(ConstantValue.Null(Type.Null)) }

            else -> throw ParserValueUnknown(token, fileName)
        }

        expression.BlockDepth = _currentBlockDepth

        return expression
    }

    private fun parameterParseAsExpression() : List<Expression>?
    {
        val expressionList = mutableListOf<Expression>()
        fetchNextExpectedToken<LexerToken.Lparen>("'('")
        val isRightBraked = lexer.peek() is LexerToken.Rparen

        if(isRightBraked)
        {
            fetchNextExpectedToken<LexerToken.Rparen>("')'")
            return null
        }

        while(true)
        {
            expressionList.add(expressionParse())

            val token = getNextToken()

            if(token is LexerToken.Rparen)
            {
               break
            }

            if(token !is LexerToken.Comma)
            {
                throw ParserStatementInvalid(token, fileName)
            }
        }

        if(expressionList.isEmpty())
        {
            return null
        }

        return expressionList
    }


    private fun functionCallParse() : Expression.FunctionCall
    {
        val name = functionIdentifyParse()
        val generics = genericCall()
        val parameter = parameterParseAsExpression()

        //use for function.function operations
        if(lexer.peek() is LexerToken.Dot){
            fetchNextExpectedToken<LexerToken.Dot>("'.'")
            val upperFunction = functionCallParse()
            upperFunction.parameterList = listOf(Expression.FunctionCall(name, parameter, generics, currentLineOfCode)).union(upperFunction.parameterList ?: listOf()).toList()
            return upperFunction
        }

        return Expression.FunctionCall(name, parameter, generics, currentLineOfCode)
    }

    private fun useVariableParse() : Expression
    {
        val name = nameParse()

        val expression = if(lexer.peek() is LexerToken.Dot){
            fetchNextExpectedToken<LexerToken.Dot>("'.'")

            val expression = when(val nextLexerToken = lexer.peek()){
                is LexerToken.FunctionIdent -> functionCallParse()
                is LexerToken.NameIdent -> useVariableParse()
                else -> throw Exception("$name.$nextLexerToken is not supported")
            }

            Expression.UseDotVariable(name,expression)
        }
        else
            Expression.UseVariable(name, currentLineOfCode)

        expression.BlockDepth = _currentBlockDepth
        return expression
    }

    private fun bracketBlock() : Expression
    {
        fetchNextExpectedToken<LexerToken.Lparen>("'('")
        val leftBracketAgain = lexer.peek()

        _currentBlockDepth++

        val expression = when(leftBracketAgain)
        {
            is LexerToken.Lparen ->
            {
                val expression = bracketBlock()
                val next = lexer.peek()
                val hasSomethingAfter = next !is LexerToken.Rparen

                if(hasSomethingAfter)
                {
                    calculationParse(expression)
                }
                else
                {
                    expression
                }
            }
            is LexerToken.Not -> notParse()
            is LexerToken.Minus -> loneMinusParse()

            is LexerToken.FunctionIdent ->
            {
                val expression = functionCallParse()
                val next = lexer.peek()

                if(next is LexerToken.Rparen)
                {
                    expression
                }
                else
                {
                    calculationParse(expression)
                }
            }
            is LexerToken.NameIdent ->
            {
                val expression = useVariableParse()
                val next = lexer.peek()

                if(next is LexerToken.Rparen)
                {
                    expression
                }
                else
                {
                    calculationParse(expression)
                }
            }
            is LexerToken.String_Literal,
            is LexerToken.Char_Literal,
            is LexerToken.Boolean_Literal,
            is LexerToken.Number_Literal,
            is LexerToken.Float_Literal->
            {
                val expressionValue = valueParse()
                val next = lexer.peek()

                if(next is LexerToken.Rparen)
                {
                    expressionValue
                }
                else
                {
                    calculationParse(expressionValue)
                }
            }
            else -> throw ParserTokenUnexpected(leftBracketAgain, fileName)
        }

        fetchNextExpectedToken<LexerToken.Rparen>("')'")

        expression.BlockDepth = _currentBlockDepth

        _currentBlockDepth--

        return expression
    }

    private fun notParse() : Expression.Operation
    {
        fetchNextExpectedToken<LexerToken.Not>("Not '!'")
        val expression = expressionParse()

        return  Expression.Operation(Operator.Not, expression,  null, currentLineOfCode)
    }

    private fun loneMinusParse() : Expression.Operation
    {
        fetchNextExpectedToken<LexerToken.Minus>("Minus '-'")
        val nextToken = lexer.peek()

        if(nextToken is LexerToken.Number_Literal)
        {
            val value = valueParse()

            return Expression.Operation(Operator.Minus, value, null, currentLineOfCode)
        }
        else
        {
            val expression = expressionParse()

            if(expression is Expression.Operation)
            {
                val newE =  Expression.Operation(Operator.Minus, expression.expressionA, null, currentLineOfCode)
                return Expression.Operation(expression.operator, newE, expression.expressionB, currentLineOfCode)

            }

            return Expression.Operation(Operator.Minus, expression, null, currentLineOfCode)
        }
    }

    private fun expressionParse(): Expression
    {

        var expression = when(val nextToken = lexer.peek())
        {
            is LexerToken.Boolean_Literal,
            is LexerToken.Char_Literal,
            is LexerToken.String_Literal,
            is LexerToken.Float_Literal,
            is LexerToken.Number_Literal,
            is LexerToken.Null, -> valueParse()

            is LexerToken.FunctionIdent -> functionCallParse()
            is LexerToken.NameIdent -> useVariableParse()

            is LexerToken.Lparen ->
            {
                bracketBlock()
            }

            is LexerToken.Not -> notParse()
            is LexerToken.Minus -> loneMinusParse()

            is LexerToken.AssignEquals ->
            {
                getNextToken()
                expressionParse()
            }

            else -> throw ParserExpressionInvalid(nextToken, fileName)
        }

        when(lexer.peek())
        {
            is LexerToken.Semicolon -> getNextToken()

            is LexerToken.Plus,
            is LexerToken.Minus,
            is LexerToken.Mul,
            is LexerToken.Div,
            is LexerToken.Double_Equals,
            is LexerToken.And,
            is LexerToken.Or,
            is LexerToken.Not,
            is LexerToken.NotEqual,
            is LexerToken.Less,
            is LexerToken.LessEqual,
            is LexerToken.Greater,
            is LexerToken.GreaterEqual -> {expression = calculationParse(expression)}
        }

        return expression
    }

    private fun assignmentParse() : Statement.AssignValue
    {
        val type = nameParse()
        val expression = expressionParse()

        return Statement.AssignValue(type, expression, currentLineOfCode)
    }

    private fun blockParse() : Statement.Block
    {
        val body = bodyParse()

        return Statement.Block(body, currentLineOfCode)
    }

    private fun whileParse() : Statement.While
    {
        fetchNextExpectedToken<LexerToken.While>("While")
        val condition = conditionParse()
        val body = bodyParse()

        return  Statement.While(condition, body, currentLineOfCode)
    }

    private fun ifParse() : Statement.If
    {
        fetchNextExpectedToken<LexerToken.If>("if")
        val condition = conditionParse()
        val ifBody = bodyParse()
        var elseBody : Body? = null

        val expectedElseToken = lexer.peek()

        if(expectedElseToken is LexerToken.Else)
        {
            getNextToken()
            elseBody = bodyParse()
        }

        return Statement.If(condition, ifBody, elseBody, currentLineOfCode)
    }

    private fun conditionParse() : Expression
    {
        fetchNextExpectedToken<LexerToken.Lparen>("LexerToken.TypeIdent")
        val nextToken = lexer.peek()

        if(nextToken is LexerToken.Rparen)
        {
            throw ParserConditionEmpty(nextToken.LineOfCode, fileName)
        }

        val expression = expressionParse()

        fetchNextExpectedToken<LexerToken.Rparen>("Rparen")

        return expression
    }

    private fun assignParse(name : String, useVariable: Expression) : Statement.AssignValue
    {
        val expression = when(val tokenEquals = getNextToken()){
            is LexerToken.AssignEquals -> expressionParse()
            is LexerToken.AssignPlusEquals -> Expression.Operation(Operator.Plus, useVariable, expressionParse())
            is LexerToken.AssignMinusEquals -> Expression.Operation(Operator.Minus, useVariable, expressionParse())
            is LexerToken.AssignMulEquals -> Expression.Operation(Operator.Multiply, useVariable, expressionParse())
            is LexerToken.AssignDivEquals -> Expression.Operation(Operator.Divide, useVariable, expressionParse())
            else -> throw ParserUnsupportedAssignment(tokenEquals, fileName)
        }

        return Statement.AssignValue(name, expression, currentLineOfCode)
    }

    private fun procedureCallParse() : Statement.ProcedureCall
    {
        val name = functionIdentifyParse()
        val generics = genericCall()
        val parameter = parameterParseAsExpression()

        if(lexer.peek() == LexerToken.Semicolon())
            fetchNextExpectedToken<LexerToken.Semicolon>("';'")

        return Statement.ProcedureCall(name, parameter, generics, currentLineOfCode)
    }

    private fun statementParse(): Statement
    {
        return when(val token = lexer.peek())
        {
            is LexerToken.If -> ifParse()
            is LexerToken.While -> whileParse()
            is LexerToken.LCurlyBrace ->  blockParse()
            is LexerToken.Return -> assignmentParse()
            is LexerToken.NameIdent ->{
                val name = nameParse()
                if(lexer.peek() is LexerToken.Dot) {
                    fetchNextExpectedToken<LexerToken.Dot>("'.'")
                    val statement = Statement.UseClass(name, dotStatementParse(Expression.UseDotVariable(name, Expression.Value(ConstantValue.Null()),currentLineOfCode)), currentLineOfCode)
                    statement
                }else
                    assignParse(name, Expression.UseVariable(name, currentLineOfCode))
            }
            is LexerToken.FunctionIdent -> procedureCallParse()

            else -> throw ParserStatementInvalid(token, fileName)
        }
    }

    private fun dotStatementParse(expression: Expression.UseDotVariable): Statement
    {
        return when(val token = lexer.peek())
        {
            is LexerToken.NameIdent ->{
                val name = nameParse()
                if(lexer.peek() is LexerToken.Dot) {
                    fetchNextExpectedToken<LexerToken.Dot>("'.'")
                    expression.expression = Expression.UseDotVariable(name,Expression.Value(ConstantValue.Null()),currentLineOfCode)
                    Statement.UseClass(name, dotStatementParse(expression))
                }else{
                    expression.expression = Expression.UseVariable(name, currentLineOfCode)
                    assignParse(name, expression)
                }

            }
            is LexerToken.FunctionIdent -> procedureCallParse()

            else -> throw ParserStatementInvalid(token, fileName)
        }
    }

    private fun typeParse(): Type
    {
        val variableType = getNextToken()

        val identifier = variableType as? LexerToken.TypeIdent ?: throw ParserTypeLowerCase(variableType as LexerToken.NameIdent, fileName)

        return when (identifier.identify)
        {
            "Int" -> Type.Integer
            "Bool" -> Type.Boolean
            "Char" -> Type.Char
            "String" -> Type.String
            "Float" -> Type.Float
            "Double" -> Type.Double
            "Void" -> Type.Void

            else ->{
                val generics = genericCall()
                if(generics != null)
                    Type.CustomWithGenerics(identifier.identify, generics)
                else
                    Type.Custom(identifier.identify)
            }
        }
    }

    private fun variableDeclaration(typeUpper: Type) : Declaration.VariableDeclaration
    {
        var type = typeUpper
        val variableName = nameParse()
        val generics = genericCall()
        val expression = expressionParse()

        if(generics != null){
            type as? Type.Custom ?: throw ParserBaseException(currentLineOfCode ,fileName, "Can't use generics with base types")
            type = Type.CustomWithGenerics(type.name, generics)
        }

        return Declaration.VariableDeclaration(type, variableName, expression, currentLineOfCode)
    }
}