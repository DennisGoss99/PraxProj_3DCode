package Parser

import Lexer.Lexer
import Lexer.LexerToken
import Parser.ParserToken.Declaration
import Parser.ParserToken.Expression
import Parser.ParserToken.File
import TypeChecker.TypeChecker
import java.util.HashMap

class ParserManager(path : String) {


    val loadedFiles = HashMap<String,File?>()

    init {
        var file = loadImports(path)
        loadedFiles[path.substringAfterLast('/').substringBeforeLast('.')] = file

        loadedFiles.forEach { _, f ->
            f?.includes?.forEach { n, i ->
                if(i == null)
                    f.includes[n] = loadedFiles[n]
            }
        }

        println("")
    }

    private fun loadImports(filePath: String) : File {
        val file = java.io.File(filePath)
        val inputStream = file.inputStream()
        val inputString = inputStream.bufferedReader().use { it.readText() }

        val lexer = Lexer(inputString)
        val rawImports = getImports(lexer)

        val includes = HashMap<String, File?>()
        val classDeclarations = HashMap<String, Declaration.ClassDeclare>()
        val functionDeclarations = HashMap<String, MutableList<Declaration.FunctionDeclare>>()
        val variableDeclarations = HashMap<String, Declaration.VariableDeclaration>()

        loadedFiles[file.nameWithoutExtension] = null

        rawImports.forEach {
            val name = it.first.substringAfterLast('/').substringBeforeLast('.')
            if(!loadedFiles.containsKey(name)){
                val loadedFile = loadImports(file.path.substringBeforeLast('\\') + '\\' + it.first)
                includes[name] = loadedFile
                loadedFiles[name] = loadedFile
            }else{
                includes[name] = loadedFiles[name]
            }
        }

        val parserOutput = Parser(lexer).ParsingStart() //rawImports.mapTo(mutableListOf()) { Declaration.Imports(it.first.substringAfterLast('/').substringBeforeLast('.'),null,it.second) })

        parserOutput.forEach { d ->
            when(d){
                is Declaration.ClassDeclare -> classDeclarations[d.className] = d
                is Declaration.FunctionDeclare -> functionDeclarations.getOrPut(d.functionName, ::mutableListOf).add(d)
                is Declaration.VariableDeclaration -> variableDeclarations[d.name] = d
            }

        }

        return File(file.nameWithoutExtension, includes ,classDeclarations , functionDeclarations ,variableDeclarations)//files[file.nameWithoutExtension] = File(file.nameWithoutExtension, parserOutput)
    }

    private fun getImports(lexer: Lexer) : List<Pair<String, Int>>{
        val fileNames = mutableListOf<Pair<String, Int>>()

        while (lexer.peek() is LexerToken.Import){
            val importToken = lexer.next()

            val fileName = (lexer.next() as? LexerToken.String_Literal) ?: throw Exception("")

            fileNames.add((fileName.s.replace('.','/') + ".c3d") to importToken.LineOfCode )
        }
        return fileNames
    }

}