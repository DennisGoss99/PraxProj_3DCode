package Parser.ParserToken

sealed class Declaration : ILineOfCode // Lila
{
    override fun toString(): String{
        return this.javaClass.simpleName
    }

    data class ClassDeclare(
        val className: String,
        val classBody: ClassBody,
        val generics : List<String>?,
        val isPrivate : Boolean = false,
        override val LineOfCode: Int = -1
    ) : Declaration()

    data class FunctionDeclare(
        val returnType: Type,
        val functionName: String,
        val body: Body,
        val parameters : List<Parameter>?,
        val generics : List<String>?,
        val isPrivate : Boolean = false,
        override val LineOfCode: Int = -1
    ) : Declaration()

    data class VariableDeclaration(val type: Type, val name: String, val expression : Expression, val isPrivate : Boolean = false, override val LineOfCode: Int = -1) : Declaration()

    data class Import(
        val name: String,
        override val LineOfCode: Int = -1
    ) : Declaration()
}