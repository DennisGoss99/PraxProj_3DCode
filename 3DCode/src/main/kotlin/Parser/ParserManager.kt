package Parser

import Lexer.Lexer
import Lexer.LexerToken
import Parser.ParserToken.Declaration
import Parser.ParserToken.File
import TypeChecker.Exceptions.TypeCheckerDuplicateClassException
import openGLOutput.exercise.codeObjects.Object
import java.util.HashMap

class ParserManager{
    companion object {

        private var loadedFiles = HashMap<String,File?>()

        fun beforeLoad(){
            loadedFiles = HashMap<String, File?>()

            loadedFiles["Array"] = ArrayImplementation.file
            loadedFiles["Object"] = Object.file
        }


        fun loadFromString(list: MutableList<Pair<String , String>>) : File {
            beforeLoad()

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

            val lexer = Lexer(file.second, file.first)

            getImports(lexer).forEach {
                includes[it] = null
            }

            Parser(lexer, file.first).parsingStart().forEach { d ->
                when(d){
                    is Declaration.ClassDeclare -> {
                        if(classDeclarations.containsKey(d.className))
                            throw TypeCheckerDuplicateClassException(d.LineOfCode, file.first, d.className)
                        classDeclarations[d.className] = d
                    }
                    is Declaration.FunctionDeclare -> functionDeclarations.getOrPut(d.functionName, ::mutableListOf).add(d)
                    is Declaration.VariableDeclaration -> variableDeclarations[d.name] = d
                }
            }

            return File(file.first, includes ,classDeclarations , functionDeclarations ,variableDeclarations, hashMapOf())
        }


        fun loadFromDisk(path: String): File {
            beforeLoad()

            val file = loadImportsFile(path)
            loadedFiles[path.substringAfterLast('/').substringBeforeLast('.')] = file

            loadedFiles.forEach { (_, f) ->
                f?.includes?.forEach { (n, i) ->
                    if (i == null)
                        f.includes[n] = loadedFiles[n]
                }
            }
            return file
        }

        private fun loadImportsFile(filePath: String) : File {
            val file = java.io.File(filePath)
            val inputStream = file.inputStream()
            val inputString = inputStream.bufferedReader().use { it.readText() }

            val lexer = Lexer(inputString, file.name)
            val rawImports = getImports(lexer)

            val includes = HashMap<String, File?>()
            val classDeclarations = HashMap<String, Declaration.ClassDeclare>()
            val functionDeclarations = HashMap<String, MutableList<Declaration.FunctionDeclare>>()
            val variableDeclarations = HashMap<String, Declaration.VariableDeclaration>()

            loadedFiles[file.nameWithoutExtension] = null

            rawImports.forEach {
                val name = it.substringAfterLast('.')
                if(!loadedFiles.containsKey(name)){
                    val loadedFile = loadImportsFile(file.path.substringBeforeLast('\\') + '\\' + it.replace('.','/') +".3dc")
                    includes[name] = loadedFile
                    loadedFiles[name] = loadedFile
                }else{
                    includes[name] = loadedFiles[name]
                }
            }

            val parserOutput = Parser(lexer, file.name).parsingStart()

            parserOutput.forEach { d ->
                when(d){
                    is Declaration.ClassDeclare -> {
                        if(classDeclarations.containsKey(d.className))
                            throw TypeCheckerDuplicateClassException(d.LineOfCode, file.name, d.className)
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
                lexer.next()

                val fileName = (lexer.next() as? LexerToken.String_Literal) ?: throw Exception("")

                fileNames.add(fileName.s)
            }
            return fileNames
        }

    }
}