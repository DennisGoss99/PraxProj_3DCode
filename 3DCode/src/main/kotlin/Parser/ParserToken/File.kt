package Parser.ParserToken

import java.util.HashMap

class File(
    val name : String,
    //Key contains Class names
    val includes : HashMap<String, File?>,
    val classDeclarations : HashMap<String, Declaration.ClassDeclare>,
    val functionDeclarations : HashMap<String, MutableList<Declaration.FunctionDeclare>>,
    val globalEnvironment : HashMap<String, Declaration.VariableDeclaration>
    ){
}
