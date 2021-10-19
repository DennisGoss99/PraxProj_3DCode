package Parser

import Lexer.Lexer
import Lexer.LexerToken
import Parser.Exception.*
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue

class Parser(val lexer: Lexer)
{
    private var currentLineOfCode = 1

    private val aplTree: Declaration.FunctionDeclare? = null
    private val _debugOutPut = false
    private var _currentBlockDepth = 0

    private inline fun <reified A> FetchNextExpectedToken(expectedType: String): A {
        val next = GetTextToken()

        if (next !is A) {
            throw Exception("Unexpected token: expected $expectedType, but got $next")
        }

        return next
    }

    private fun GetTextToken() : LexerToken
    {
        val token = lexer.next()
        currentLineOfCode = token.LineOfCode

        return token
    }

    fun ParsingStart() : List<Declaration>
    {



        var declarationList : List<Declaration>? = null

        try
        {
            declarationList = DeclarationListParse()
        }
        catch(e : Exception)
        {
            e.printStackTrace()
        }

        if(lexer.peek() is LexerToken.EOF)
        {
            val token = GetTextToken()
        }
        else
        {

            while(true)
            {
                val token = GetTextToken()

                if(token is LexerToken.EOF)
                    break;
            }
        }

        if(declarationList == null)
            throw ParserNoData()

        return declarationList
    }

    private fun DeclarationParse() : Declaration
    {
        return when(val nextToken = lexer.peek()){
            is LexerToken.Class -> ClassParse()
            is LexerToken.TypeIdent->{
                val type = TypeParse()
                when(val nextToken = lexer.peek())
                {
                    is LexerToken.NameIdent -> VariableDeclaration(type)
                    is LexerToken.FunctionIdent -> FuncitonParse(type)

                    else -> throw ParserDeclarationTokenInvalid(nextToken)
                }
            }
            // Constructor Parse
            is LexerToken.FunctionIdent -> FuncitonParse(Type.Void)

            is LexerToken.NameIdent -> throw ParserTypeLowerCase(nextToken)
            else -> throw ParserTypeUnknown(nextToken)
        }
    }

    private fun DeclarationListParse() : List<Declaration>
    {
        val declarationList = mutableListOf<Declaration>()

        while(true)
        {
            val token = lexer.peek()

            if(token is LexerToken.EOF)
            {
                break
            }

            declarationList.add(DeclarationParse())
        }

        return declarationList
    }

    private fun OperatorStength(operator: Operator) : Pair<Int, Int>
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

    private fun NameParse(): String
    {
        val token = GetTextToken()

        if(token is LexerToken.Return)
        {
            return "return"
        }

        if(token !is LexerToken.NameIdent)
        {
            throw ParserInvalidName(token)
        }

        val name = token

        return "${name.identify}"
    }

    private fun LocalVariablesParse() : List<Declaration.VariableDeclaration>?
    {
        val localVariableList = mutableListOf<Declaration.VariableDeclaration>()

        while(true)
        {
            val token = lexer.peek()

            if(token is LexerToken.Return || token !is LexerToken.TypeIdent) // return if there is no data or no "int a" stuff
            {
                break
            }

            val type = TypeParse()
            val name = NameParse()
            val expression = ExpressionParse()


            val variableDeclaration = Declaration.VariableDeclaration(type, name, expression,currentLineOfCode)

            localVariableList.add(variableDeclaration)
        }

        if(localVariableList.isEmpty())
        {
            return null
        }

        return localVariableList
    }

    private fun StatementListParse(): List<Statement>
    {
        val statementList = mutableListOf<Statement>()

        while(true)
        {
            val token = lexer.peek()

            when(token)
            {
                is LexerToken.Semicolon, // Expresion ended
                is LexerToken.RCurlyBrace -> break // Body has ended
                is LexerToken.Rparen ->
                {
                    GetTextToken()

                    while(lexer.peek() is LexerToken.Semicolon)
                    {
                        GetTextToken()
                    }

                    break
                }
            }

            statementList.add(StatementParse())
        }

        return statementList
    }

    private fun BodyParse(): Body
    {
        val expectedLCurly = FetchNextExpectedToken<LexerToken.LCurlyBrace>("LCurlyBrace")
        val localVariables = LocalVariablesParse()
        val statementList = StatementListParse()
        val expectedRCurly = FetchNextExpectedToken<LexerToken.RCurlyBrace>("RCurlyBrace")

        return Body(statementList, localVariables)
    }

    private fun ClassBodyParse(): ClassBody
    {
        FetchNextExpectedToken<LexerToken.LCurlyBrace>("LCurlyBrace")

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

            when(val declaration = DeclarationParse()){
                is Declaration.VariableDeclaration -> localVariables.add(declaration)
                is Declaration.FunctionDeclare -> methodsList.getOrPut(declaration.functionName, ::mutableListOf).add(declaration)
                else -> ParserUnsupportedDeclaration(declaration)
            }
        }

        FetchNextExpectedToken<LexerToken.RCurlyBrace>("RCurlyBrace")

        return ClassBody(methodsList, localVariables)
    }


    private fun ParameterParse() : Parameter
    {
        val type = TypeParse()
        val name = NameParse()

        return Parameter(name, type)
    }

    private fun ParameterParseAsDeclaration(): List<Parameter>?
    {
        val parameterList = mutableListOf<Parameter>()
        val expectedLBrace = FetchNextExpectedToken<LexerToken.Lparen>("Lparen")

        while (true)
        {
            val token = lexer.peek()
            val isAtEndOfParameter = token is LexerToken.Rparen
            val isSeperator = token is LexerToken.Comma

            if (isAtEndOfParameter)
            {
                GetTextToken()
                break
            }

            if(isSeperator)
            {
                GetTextToken()
            }

            parameterList.add(ParameterParse())
        }

        if (parameterList.size == 0) {
            return null
        }

        return parameterList
    }

    private fun FunctionIdentifyParse() : String
    {
        val name = FetchNextExpectedToken<LexerToken.FunctionIdent>("function identifier")
        return name.identify
    }

    private fun ClassIdentifyParse() : String
    {
        val name = FetchNextExpectedToken<LexerToken.TypeIdent>("class identifier")
        return name.identify
    }

    private fun FuncitonParse(type : Type): Declaration.FunctionDeclare
    {
        val currentLineOfCode = lexer.peek().LineOfCode
        val name = FunctionIdentifyParse()
        val parameter = ParameterParseAsDeclaration()
        val body = BodyParse()

        return Declaration.FunctionDeclare(type, name, body,parameter,currentLineOfCode)
    }

    private fun ClassParse(): Declaration.ClassDeclare{

        FetchNextExpectedToken<LexerToken.Class>("Class")

        val currentLineOfCode = lexer.peek().LineOfCode
        val name = ClassIdentifyParse()
        val body = ClassBodyParse()
        return Declaration.ClassDeclare(name, body,currentLineOfCode)
    }

    private fun OperatorParse() : Operator
    {
        val token = GetTextToken()

        return when(token)
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

            else -> throw ParserOperatorUnknown(token)
        }
    }

    private fun CalulationParse(leftExpression : Expression) : Expression.Operation
    {
        val operator = OperatorParse()
        val rightExpression = ExpressionParse()

       return CalculationSort(operator, leftExpression, rightExpression)
    }

    private fun CalculationSort(operator: Operator, expressionA: Expression, expressionB: Expression) : Expression.Operation
    {
        val operatorCurrentStrength = OperatorStength(operator)

        val sameBlockDepth = (expressionA.BlockDepth == expressionB.BlockDepth)// || (expressionA.BlockDepth == -1)
        val bothExpressionsAreOperator =
            (expressionA is Expression.Value || expressionA is Expression.UseVariable)
                    &&
            expressionB is Expression.Operation

        if(bothExpressionsAreOperator && sameBlockDepth)
        {
            val opB = expressionB as Expression.Operation

            val operatorB = opB.operator
            val operatorBStrength = OperatorStength(operatorB)

            var isAHigher = operatorCurrentStrength.first < operatorBStrength.first;

            if(isAHigher)
            {
                val rotatedOperation = Expression.Operation(
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

                return rotatedOperation
            }
        }

        val newOperation = Expression.Operation(operator, expressionA, expressionB,currentLineOfCode)
        newOperation.BlockDepth = minOf(expressionA.BlockDepth, expressionB.BlockDepth)

        return newOperation

    }

    private fun ValueParse() : Expression.Value
    {
        val token = GetTextToken()

        val expression = when(token)
        {
            is LexerToken.Number_Literal -> { Expression.Value(ConstantValue.Integer(token.n, Type.Integer)) }
            is LexerToken.Float_Literal -> {Expression.Value(ConstantValue.Float(token.f, Type.Float))}
            is LexerToken.Boolean_Literal -> { Expression.Value(ConstantValue.Boolean(token.b, Type.Boolean)) }
            is LexerToken.Char_Literal -> { Expression.Value(ConstantValue.Char(token.c, Type.Char)) }
            is LexerToken.String_Literal -> { Expression.Value(ConstantValue.String(token.s, Type.String)) }

            else -> throw ParserValueUnknown(token)
        }

        expression.BlockDepth = _currentBlockDepth

        return expression
    }

    private fun ParameterParseAsExpression() : List<Expression>?
    {
        val expressionList = mutableListOf<Expression>()
        val leftBracked = FetchNextExpectedToken<LexerToken.Lparen>("'('")
        val isRightBracked = lexer.peek() is LexerToken.Rparen

        if(isRightBracked)
        {
            FetchNextExpectedToken<LexerToken.Rparen>("')'")
            return null
        }

        while(true)
        {
            expressionList.add(ExpressionParse())

            val token = GetTextToken()

            if(token is LexerToken.Rparen)
            {
               break
            }

            if(token !is LexerToken.Comma)
            {
                throw ParserStatementInvalid(token)
            }
        }

        if(expressionList.isEmpty())
        {
            return null
        }

        return expressionList
    }

    private fun FunctionCallParse() : Expression.FunctionCall
    {
        val name = FunctionIdentifyParse()
        val parameter = ParameterParseAsExpression()

        return Expression.FunctionCall(name, parameter, currentLineOfCode)
    }

    private fun UseVariableParse() : Expression
    {
        val name = NameParse()

        val expression = if(lexer.peek() is LexerToken.Dot){
            FetchNextExpectedToken<LexerToken.Dot>("'.'")

            val expression = when(val nextLexerToken = lexer.peek()){
                is LexerToken.FunctionIdent -> FunctionCallParse()
                is LexerToken.NameIdent -> UseVariableParse()
                else -> throw Exception("$name.$nextLexerToken is not supported")
            }

            Expression.UseDotVariable(name,expression)
        }
        else
            Expression.UseVariable(name, currentLineOfCode)

        expression.BlockDepth = _currentBlockDepth
        return expression
    }

    private fun BracketBlock() : Expression
    {
        val leftBracket = FetchNextExpectedToken<LexerToken.Lparen>("'('")
        val leftBracketAgain = lexer.peek()

        _currentBlockDepth++

        val expression = when(leftBracketAgain)
        {
            is LexerToken.Lparen ->
            {
                val expression = BracketBlock()
                val next = lexer.peek()
                val hasSomethingAfter = next !is LexerToken.Rparen

                if(hasSomethingAfter)
                {
                    CalulationParse(expression)
                }
                else
                {
                    expression
                }
            }
            is LexerToken.Not -> NotParse()
            is LexerToken.Minus -> LoneMinusParse()

            is LexerToken.FunctionIdent ->
            {
                val expression = FunctionCallParse()
                val next = lexer.peek()

                if(next is LexerToken.Rparen)
                {
                    expression
                }
                else
                {
                    CalulationParse(expression)
                }
            }
            is LexerToken.NameIdent ->
            {
                val expression = UseVariableParse()
                val next = lexer.peek()

                if(next is LexerToken.Rparen)
                {
                    expression
                }
                else
                {
                    CalulationParse(expression)
                }
            }
            is LexerToken.String_Literal,
            is LexerToken.Char_Literal,
            is LexerToken.Boolean_Literal,
            is LexerToken.Number_Literal,
            is LexerToken.Float_Literal->
            {
                val expressionValue = ValueParse()
                val next = lexer.peek()

                if(next is LexerToken.Rparen)
                {
                    expressionValue
                }
                else
                {
                    CalulationParse(expressionValue)
                }
            }
            else -> throw ParserTokenUnexpected(leftBracketAgain)
        }

        val rightBracket = FetchNextExpectedToken<LexerToken.Rparen>("')'")

        expression.BlockDepth = _currentBlockDepth

        _currentBlockDepth--

        return expression
    }

    private fun NotParse() : Expression.Operation
    {
        val notToken = FetchNextExpectedToken<LexerToken.Not>("Not '!'")
        val expression = ExpressionParse()

        return  Expression.Operation(Operator.Not, expression,  null, currentLineOfCode)
    }

    private fun LoneMinusParse() : Expression.Operation
    {
        val minus = FetchNextExpectedToken<LexerToken.Minus>("Minus '-'")
        val nextToken = lexer.peek()

        if(nextToken is LexerToken.Number_Literal)
        {
            val value = ValueParse()

            return Expression.Operation(Operator.Minus, value, null, currentLineOfCode)
        }
        else
        {
            val expression = ExpressionParse()

            if(expression is Expression.Operation)
            {
                val newE =  Expression.Operation(Operator.Minus, expression.expressionA, null, currentLineOfCode)
                val newF = Expression.Operation(expression.operator, newE, expression.expressionB, currentLineOfCode)

                return newF
            }

            return Expression.Operation(Operator.Minus, expression, null, currentLineOfCode)
        }
    }

    private fun ExpressionParse(): Expression
    {
        var nextToken = lexer.peek()

        var expression = when(nextToken)
        {
            is LexerToken.Boolean_Literal,
            is LexerToken.Char_Literal,
            is LexerToken.String_Literal,
            is LexerToken.Float_Literal,
            is LexerToken.Number_Literal -> ValueParse()

            is LexerToken.FunctionIdent -> FunctionCallParse()
            is LexerToken.NameIdent -> UseVariableParse()

            is LexerToken.Lparen ->
            {
                BracketBlock()
            }

            is LexerToken.Not -> NotParse()
            is LexerToken.Minus -> LoneMinusParse()

            is LexerToken.AssignEquals ->
            {
                GetTextToken()
                ExpressionParse()
            }

            else -> throw ParserExpressionInvalid(nextToken)
        }

        var expectedSemicolon = lexer.peek()

        when(expectedSemicolon)
        {
            is LexerToken.Semicolon -> GetTextToken()

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
            is LexerToken.GreaterEqual -> {expression = CalulationParse(expression)}

//            is LexerToken.TypeIdent -> {}
//            else -> throw Exception("Can't identify LexerToken ${expectedSemicolon::class}");
        }



        //expression.BlockDepth = _currentBlockDepth

        return expression
    }

    private fun AssignmentParse() : Statement.AssignValue
    {
        val type = NameParse()
        val expression = ExpressionParse()

        return Statement.AssignValue(type, expression, currentLineOfCode)
    }

    private fun BlockParse() : Statement.Block
    {
        val body = BodyParse()

        return Statement.Block(body, currentLineOfCode)
    }

    private fun WhileParse() : Statement.While
    {
        val whileTag = FetchNextExpectedToken<LexerToken.While>("While")
        val condition = ConditionParse()
        val body = BodyParse()

        return  Statement.While(condition, body, currentLineOfCode)
    }

    private fun IfParse() : Statement.If
    {
        val ifTag = FetchNextExpectedToken<LexerToken.If>("if")
        val condition = ConditionParse()
        val ifBody = BodyParse()
        var elseBody : Body? = null

        val expectedElseToken = lexer.peek()

        if(expectedElseToken is LexerToken.Else)
        {
            GetTextToken()
            elseBody = BodyParse()
        }

        return Statement.If(condition, ifBody, elseBody, currentLineOfCode)
    }

    private fun ConditionParse() : Expression
    {
        val variableType = FetchNextExpectedToken<LexerToken.Lparen>("LexerToken.TypeIdent")
        val nextToken = lexer.peek()

        if(nextToken is LexerToken.Rparen)
        {
            throw ParserConditionEmpty(nextToken.LineOfCode)
        }

        val expression = ExpressionParse()

        val rightBrace = FetchNextExpectedToken<LexerToken.Rparen>("Rparen")

        return expression
    }

    private fun AssignParse(name: String) : Statement.AssignValue
    {
        val expression = when(val tokenEquals = GetTextToken()){
            is LexerToken.AssignEquals -> ExpressionParse()
            is LexerToken.AssignPlusEquals -> Expression.Operation(Operator.Plus,Expression.UseVariable(name), ExpressionParse())
            is LexerToken.AssignMinusEquals -> Expression.Operation(Operator.Minus,Expression.UseVariable(name), ExpressionParse())
            is LexerToken.AssignMulEquals -> Expression.Operation(Operator.Multiply,Expression.UseVariable(name), ExpressionParse())
            is LexerToken.AssignDivEquals -> Expression.Operation(Operator.Divide,Expression.UseVariable(name), ExpressionParse())
            else -> throw ParserUnsupportedAssignment(tokenEquals)
        }

        return Statement.AssignValue(name, expression, currentLineOfCode)
    }

    private fun ProcedureCallParse() : Statement.ProcedureCall
    {
        val name = FunctionIdentifyParse()
        val parameter = ParameterParseAsExpression()

        if(lexer.peek() == LexerToken.Semicolon())
            FetchNextExpectedToken<LexerToken.Semicolon>("';'")

        return Statement.ProcedureCall(name, parameter, currentLineOfCode)
    }

    private fun StatementParse(): Statement
    {
        return when(val token = lexer.peek())
        {
            is LexerToken.If -> IfParse()
            is LexerToken.While -> WhileParse()
            is LexerToken.LCurlyBrace ->  BlockParse()
            is LexerToken.Return -> AssignmentParse()
            is LexerToken.NameIdent ->{
                val token = NameParse()
                if(lexer.peek() is LexerToken.Dot) {
                    FetchNextExpectedToken<LexerToken.Dot>("'.'")
                    Statement.UseClass(token, StatementParse())
                }else
                    AssignParse(token)
            }
            is LexerToken.FunctionIdent -> ProcedureCallParse()

            else -> throw ParserStatementInvalid(token)
        }
    }

    private fun TypeParse(): Type
    {
        val variableType = GetTextToken()

        val identifier = variableType as LexerToken.TypeIdent

        return when (identifier.identify)
        {
            "Int" -> Type.Integer
            "Bool" -> Type.Boolean
            "Char" -> Type.Char
            "String" -> Type.String
            "Float" -> Type.Float
            "Double" -> Type.Double
            "Void" -> Type.Void

            else -> Type.Custom(identifier.identify)
        }
    }

    private fun VariableDeclaration(type: Type) : Declaration.VariableDeclaration
    {
        val variableName = NameParse()
        val expression = ExpressionParse()

        return Declaration.VariableDeclaration(type, variableName, expression, currentLineOfCode)
    }
}