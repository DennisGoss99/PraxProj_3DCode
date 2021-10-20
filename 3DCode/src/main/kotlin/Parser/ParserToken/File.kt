package Parser.ParserToken

import java.util.HashMap

class File(
    val name : String,
    //Key contains Class names
    val includes : HashMap<String, File?>,
    val classDeclarations : HashMap<String, Declaration.ClassDeclare>,
    val functionDeclarations : HashMap<String, MutableList<Declaration.FunctionDeclare>>,
    val variableDeclaration: HashMap<String, Declaration.VariableDeclaration>,
    val globalEnvironment : HashMap<String, Expression.Value>,
    var variablesEventuated : Boolean = false
    )
