package Parser

import Lexer.Lexer
import Lexer.LexerToken
import Parser.ParserToken.Declaration
import Parser.ParserToken.File
import TypeChecker.TypeChecker

class ParserManager(path : String) {

    val files = HashMap<String,File?>()

    init {
        loadImports(path)

        files.forEach { (_, f) ->
            if(f?.declarations != null){
                for (declaration in f.declarations){
                    when(declaration){
                        is Declaration.Imports -> declaration.list = files[declaration.name]?.declarations
                        else -> break
                    }
                }
            }
        }

        files.forEach { (_, f) ->
            TypeChecker(f?.declarations ?: mutableListOf()).check()
        }
    }

    public fun getApp() = files["App"]?.declarations ?: throw Exception("Couldn't find App")

    private fun loadImports(filePath: String) {
        val file = java.io.File(filePath)
        val inputStream = file.inputStream()
        val inputString = inputStream.bufferedReader().use { it.readText() }

        files[file.nameWithoutExtension] = null

        val lexer = Lexer(inputString)
        val rawImports = getImports(lexer)
        rawImports.forEach {
            if(!files.containsKey(it.first.substringBeforeLast('.')))
                loadImports(file.path.substringBeforeLast('\\') + '\\' + it.first)
        }

        val parserOutput = Parser(lexer).ParsingStart(rawImports.mapTo(mutableListOf()) { Declaration.Imports(it.first.substringAfterLast('/').substringBeforeLast('.'),null,it.second) })
        files[file.nameWithoutExtension] = File(file.nameWithoutExtension, parserOutput)
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