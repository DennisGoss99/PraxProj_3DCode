package Lexer

import Lexer.Exceptions.*
import PeekableIterator

open class Lexer(input: String) {

    private val iterator: PeekableIterator<Char> = PeekableIterator(input.iterator())
    private var lookahead: LexerToken? = null

    protected open var currentLineOfCode = 1

    public fun next(): LexerToken{

        lookahead?.let { lookahead = null; return it }
        consumeWhitespace()
        var nextChar = consumeComments()

        if (!iterator.hasNext())
            return LexerToken.EOF

        return when (val c = nextChar ?: iterator.next()) {
            '(' -> LexerToken.Lparen(currentLineOfCode)
            ')' -> LexerToken.Rparen(currentLineOfCode)
            '[' -> LexerToken.LBracket(currentLineOfCode)
            ']' -> LexerToken.RBracket(currentLineOfCode)
            '{' -> LexerToken.LCurlyBrace(currentLineOfCode)
            '}' -> LexerToken.RCurlyBrace(currentLineOfCode)
            ';' -> LexerToken.Semicolon(currentLineOfCode)
            ',' -> LexerToken.Comma(currentLineOfCode)
            '.' -> LexerToken.Dot(currentLineOfCode)

            '+' -> when (iterator.peek()) {
                '=' -> {
                    iterator.next()
                    LexerToken.AssignPlusEquals(currentLineOfCode)
                }
                else -> LexerToken.Plus(currentLineOfCode)
            }
            '-' -> when (iterator.peek()) {
                '=' -> {
                    iterator.next()
                    LexerToken.AssignMinusEquals(currentLineOfCode)
                }
                else -> LexerToken.Minus(currentLineOfCode)
            }
            '*' -> when (iterator.peek()) {
                '=' -> {
                    iterator.next()
                    LexerToken.AssignMulEquals(currentLineOfCode)
                }
                else -> LexerToken.Mul(currentLineOfCode)
            }
            '/' -> when (iterator.peek()) {
                '=' -> {
                    iterator.next()
                    LexerToken.AssignDivEquals(currentLineOfCode)
                }
                else -> LexerToken.Div(currentLineOfCode)
            }
            '=' -> when (iterator.peek()) {
                '=' -> {
                    iterator.next()
                    LexerToken.Double_Equals(currentLineOfCode)
                }
                else -> LexerToken.AssignEquals(currentLineOfCode)
            }
            '&' -> when (iterator.peek()) {
                '&' -> {
                    iterator.next()
                    LexerToken.And(currentLineOfCode)
                }
                else -> throw LexerUnexpectedCharException(currentLineOfCode, c,'&')
            }
            '|' -> when (iterator.peek()) {
                '|' -> {
                    iterator.next()
                    LexerToken.Or(currentLineOfCode)
                }
                else -> throw LexerUnexpectedCharException(currentLineOfCode, c,'|')
            }
            '!' -> when (iterator.peek()) {
                '=' -> {
                    iterator.next()
                    LexerToken.NotEqual(currentLineOfCode)
                }
                else -> LexerToken.Not(currentLineOfCode)
            }
            '<' -> when (iterator.peek()) {
                '=' -> {
                    iterator.next()
                    LexerToken.LessEqual(currentLineOfCode)
                }
                else -> LexerToken.Less(currentLineOfCode)
            }
            '>' -> when (iterator.peek()) {
                '=' -> {
                    iterator.next()
                    LexerToken.GreaterEqual(currentLineOfCode)
                }
                else -> LexerToken.Greater(currentLineOfCode)
            }

            '\'' -> getChar()
            '\"' -> getString()
            else -> when {
                c.isJavaIdentifierStart() -> ident(c)
                c.isDigit() -> digit(c)
                else -> throw LexerUnexpectedCharException(currentLineOfCode, c)
            }
        }
    }

    public fun peek(): LexerToken {
        val token = next()
        lookahead = token
        return token
    }

    private fun digit(c: Char): LexerToken {
        var result = c.toString()
        while (iterator.hasNext() && (iterator.peek().isDigit() || iterator.peek() == '.')) {
            result += iterator.next()
        }

        return when{
            result.contains('.') -> LexerToken.Float_Literal(result.toFloat(), currentLineOfCode)
            else ->                      LexerToken.Number_Literal(result.toInt(), currentLineOfCode)
        }
    }

    private fun identBase(c: Char): String {
        var result = c.toString()
        while (iterator.hasNext() && (iterator.peek().isJavaIdentifierPart() || iterator.peek() == '[' || iterator.peek() == ']')) {
            result += iterator.next()
        }
        return result;
    }

    private fun ident(c: Char): LexerToken {
        var result = identBase(c);

        return when (result) {
            "true" -> LexerToken.Boolean_Literal(true, currentLineOfCode)
            "false" -> LexerToken.Boolean_Literal(false, currentLineOfCode)
            "if" -> LexerToken.If(currentLineOfCode)
            "else" -> LexerToken.Else(currentLineOfCode)
            "while" -> LexerToken.While(currentLineOfCode)
            "return" -> LexerToken.Return(currentLineOfCode)
            "class" -> LexerToken.Class(currentLineOfCode)
            "include",
            "import" -> LexerToken.Import(currentLineOfCode)
            else -> {
                if(iterator.peek() != '(')
                    if(result[0].isLowerCase())
                        LexerToken.NameIdent(result, currentLineOfCode)
                    else
                        LexerToken.TypeIdent(result, currentLineOfCode)
                else
                    LexerToken.FunctionIdent(result, currentLineOfCode)
            }
        }
    }

    private fun getChar(): LexerToken {

        return when(val c = iterator.peek()){
            '\'' -> throw LexerConstCharException(currentLineOfCode, "Char can't be empty")
            else -> {

                val c = iterator.next()
                if(iterator.next() != '\'')
                    throw LexerConstCharException(currentLineOfCode, "Char can only be a single char")

                return LexerToken.Char_Literal(c, currentLineOfCode);
            }
        }

    }

    private fun getString(): LexerToken {

        return when(val c = iterator.next()){
            '\"' -> LexerToken.String_Literal("", currentLineOfCode);
            else -> {
                var result = c.toString()

                while (iterator.hasNext() && iterator.peek() != '\"') {
                    result += iterator.next()
                }

                if(!iterator.hasNext())
                    throw LexerConstStringException(currentLineOfCode, "Missing closing '\"' char")

                iterator.next()
                return LexerToken.String_Literal(result, currentLineOfCode);
            }
        }

    }

    private fun consumeComments() : Char? {
        if(!iterator.hasNext())
            return null

        if(iterator.peek() != '/')
            return null

        iterator.next()

        when(iterator.peek()){
            '/' ->{
                iterator.next()
                while(iterator.hasNext() && iterator.next() != '\n'){}
                currentLineOfCode++
                consumeWhitespace()
                consumeComments()
            }
            '*' ->{
                iterator.next()
                while(iterator.peek() != '*'){
                    if(iterator.peek() == '/' ){
                        consumeComments()
                    }else{
                        if(iterator.next() == '\n')
                            currentLineOfCode++
                    }
                }
                iterator.next()
                iterator.next()
                consumeWhitespace()
                consumeComments()
            }
            else -> return '/'
        }
        return null
    }

    private fun consumeWhitespace() {
        while (iterator.hasNext()) {
            val nextChar = iterator.peek()

            if(nextChar == '\n')
                currentLineOfCode++

            if (!nextChar.isWhitespace()) break
            iterator.next()
        }
    }

}