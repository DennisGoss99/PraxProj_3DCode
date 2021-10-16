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
    class Custom() : Type(){
        public var name : kotlin.String = ""

        constructor(name : kotlin.String) : this() {
            this.name = name.capitalize()
        }

        override fun equals(other: Any?): kotlin.Boolean { return if(other is Custom) other.name == this.name else false}
    }
}