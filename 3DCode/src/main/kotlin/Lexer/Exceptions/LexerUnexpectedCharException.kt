package Lexer.Exceptions

class LexerUnexpectedCharException : LexerBaseException{
    val unexpectedChar : Char

    constructor(lineOfCode: Int, fileName : String, unexpectedChar : Char) : super(lineOfCode, fileName, "Unexpected char: '$unexpectedChar'"){
        this.unexpectedChar = unexpectedChar
    }

    constructor(lineOfCode: Int, fileName : String, unexpectedChar : Char, beforeChar: Char) : super(lineOfCode, fileName, "Unexpected char: '$unexpectedChar' after char '$beforeChar'"){
        this.unexpectedChar = unexpectedChar
    }


}