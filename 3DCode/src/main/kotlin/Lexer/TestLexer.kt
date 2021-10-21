package Lexer

class TestLexer(input: String) : Lexer(input, "Test") {

    override var currentLineOfCode: Int
        get() = -1
        set(value) {}

}