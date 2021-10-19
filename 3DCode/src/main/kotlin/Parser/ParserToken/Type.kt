package Parser.ParserToken

sealed class Type
{
    override fun toString(): kotlin.String
    {
        return this.javaClass.simpleName
    }

    object Boolean : Type()
    object Char: Type()
    object Integer: Type()
    object Float : Type()
    object Double : Type()
    object String : Type()
    object Void : Type()
    class Custom(var name : kotlin.String) : Type(){

        override fun toString(): kotlin.String {
            return "Class.$name"
        }

        override fun equals(other: Any?): kotlin.Boolean {
            return if(other is Custom)
                other.name == this.name
            else false}
    }
}