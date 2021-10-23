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
    object Null : Type()
    object Void : Type()

    abstract class AbstractCustom : Type(){
        abstract var name : kotlin.String
    }

    class Custom(override var name : kotlin.String) : AbstractCustom() {

        override fun toString(): kotlin.String {
            return "CustomType.$name"
        }

        override fun equals(other: Any?): kotlin.Boolean {
            return if(other is Custom)
                other.name == this.name
            else false
        }
    }

    class CustomWithGenerics(override var name : kotlin.String, var generics : List<Type>) : AbstractCustom(){

        override fun toString(): kotlin.String {
            return "Class.$name <${generics}>"
        }

        override fun equals(other: Any?): kotlin.Boolean {
            return if(other is CustomWithGenerics)
                other.name == this.name &&
                other.generics.size == other.generics.size &&
                other.generics.all { g -> this.generics[other.generics.indexOf(g)] == g}
            else
                false
        }
    }

}