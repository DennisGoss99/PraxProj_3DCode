package Parser.ParserToken

class ClassBody (val functions : List<Declaration.FunctionDeclare>, val variables : List<Declaration.VariableDeclaration>? = null)
{
    override fun toString(): String
    {
        return "{functions{${functions?.toString()}},LocalVariables{${variables?.toString()}}} "
    }
}