package Parser

import Lexer.Lexer
import Lexer.LexerToken
import Parser.ParserToken.Declaration
import Parser.ParserToken.File
import TypeChecker.Exceptions.TypeCheckerDuplicateClassException
import java.util.HashMap

class ParserManager{
    companion object {

        private var loadedFiles = HashMap<String,File?>()

        fun loadFromDisk(path: String): File {
            loadedFiles = HashMap<String, File?>()

            var file = loadImportsFile(path)
            loadedFiles[path.substringAfterLast('/').substringBeforeLast('.')] = file

            loadedFiles.forEach { (_, f) ->
                f?.includes?.forEach { (n, i) ->
                    if (i == null)
                        f.includes[n] = loadedFiles[n]
                }
            }
            return file
        }

        fun loadFromString(list: MutableList<Pair<String , String>>) : File {
            loadedFiles = HashMap<String, File?>()

            if(list.size == 0)
                throw Exception("No code files found")

            list.forEach {
                loadedFiles[it.first] = loadImportsString(it)
            }


            loadedFiles.forEach { (_, f) ->
                f?.includes?.forEach { (n, i) ->
                    if (i == null)
                        f.includes[n] = loadedFiles[n]
                }
            }

            return loadedFiles["App"] ?: throw Exception("Couldn't find file 'App'")
        }

        private fun loadImportsString(file: Pair<String , String>) : File {

            val includes = HashMap<String, File?>()
            val classDeclarations = HashMap<String, Declaration.ClassDeclare>()
            val functionDeclarations = HashMap<String, MutableList<Declaration.FunctionDeclare>>()
            val variableDeclarations = HashMap<String, Declaration.VariableDeclaration>()

            val lexer = Lexer(file.second)

            getImports(lexer).forEach {
                includes[it] = null
            }

            Parser(lexer).ParsingStart().forEach { d ->
                when(d){
                    is Declaration.ClassDeclare -> {
                        if(classDeclarations.containsKey(d.className))
                            throw TypeCheckerDuplicateClassException(d.LineOfCode,d.className)
                        classDeclarations[d.className] = d
                    }
                    is Declaration.FunctionDeclare -> functionDeclarations.getOrPut(d.functionName, ::mutableListOf).add(d)
                    is Declaration.VariableDeclaration -> variableDeclarations[d.name] = d
                }
            }

            return File(file.first, includes ,classDeclarations , functionDeclarations ,variableDeclarations, hashMapOf())
        }

        private fun loadImportsFile(filePath: String) : File {
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
                val name = it.substringAfterLast('/').substringBeforeLast('.')
                if(!loadedFiles.containsKey(name)){
                    val loadedFile = loadImportsFile(file.path.substringBeforeLast('\\') + '\\' + it)
                    includes[name] = loadedFile
                    loadedFiles[name] = loadedFile
                }else{
                    includes[name] = loadedFiles[name]
                }
            }

            val parserOutput = Parser(lexer).ParsingStart()

            parserOutput.forEach { d ->
                when(d){
                    is Declaration.ClassDeclare -> {
                        if(classDeclarations.containsKey(d.className))
                            throw TypeCheckerDuplicateClassException(d.LineOfCode,d.className)
                        classDeclarations[d.className] = d
                    }
                    is Declaration.FunctionDeclare -> functionDeclarations.getOrPut(d.functionName, ::mutableListOf).add(d)
                    is Declaration.VariableDeclaration -> variableDeclarations[d.name] = d
                }
            }

            return File(file.nameWithoutExtension, includes ,classDeclarations , functionDeclarations ,variableDeclarations, hashMapOf())
        }

        private fun getImports(lexer: Lexer) : List<String>{
            val fileNames = mutableListOf<String>()

            while (lexer.peek() is LexerToken.Import){
                val importToken = lexer.next()

                val fileName = (lexer.next() as? LexerToken.String_Literal) ?: throw Exception("")

                fileNames.add(fileName.s.replace('.','/') + ".c3d")
            }
            return fileNames
        }

    }
}