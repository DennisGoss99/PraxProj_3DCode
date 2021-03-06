package Lexer

sealed class LexerToken() {

        public override fun toString(): String {
            return this.javaClass.simpleName
        }

        open val LineOfCode : Int = -1
        open val FileName : String = "-1"


        // Keywords
        data class If(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}
        data class Else(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}
        data class While(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}
        data class For(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}
        data class Return(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}

        data class Null(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}

        data class Class(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}
        data class Import(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}

        data class Private(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}

        // Symbols
        data class AssignEquals(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}       // =
        data class AssignPlusEquals(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}   // +=
        data class AssignMinusEquals(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}  // -=
        data class AssignMulEquals(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}    // *=
        data class AssignDivEquals(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}    // /=
        data class Semicolon(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}          // ;
        data class Lparen(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}             // (
        data class Rparen(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}             // )
        data class LBracket(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}           // [
        data class RBracket(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}           // ]
        data class LCurlyBrace(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}        // {
        data class RCurlyBrace(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}        // }
        data class Comma(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}              // ,
        data class Dot(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}                // .

        // Operatoren
        data class Plus(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}               // +
        data class Minus(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}              // -
        data class Mul(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}                // *
        data class Div(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}                // /
        data class Double_Equals(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}      // ==

        data class And(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}                // &&
        data class Or(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}                 // ||
        data class Not(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}                // !
        data class NotEqual(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}           // !=
        data class Less(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}               // <
        data class LessEqual(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}          // <=
        data class Greater(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}            // >
        data class GreaterEqual(override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString()}       // >=

        data class NameIdent(val identify: String, override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString() + "($identify)"}
        data class TypeIdent(val identify: String, override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString() + "($identify)"}
        data class FunctionIdent(val identify: String, override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString() + "($identify)"}

        // Literals
        data class Boolean_Literal(val b: Boolean, override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString() + "($b)"}
        data class Number_Literal(val n: Int, override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString() + "($n)"}
        data class Float_Literal(val f: Float, override val LineOfCode : Int = -1): LexerToken(){ override fun toString(): String = super.toString() + "($f)"}
        data class Char_Literal(val c: Char, override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString() + "($c)"}
        data class String_Literal(val s: String, override val LineOfCode : Int = -1) : LexerToken(){ override fun toString(): String = super.toString() + "($s)"}



        // Control Token
        object EOF : LexerToken(){ override fun toString(): String = super.toString()}
}