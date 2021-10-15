import Evaluator.Evaluator
import Lexer.Lexer
import Parser.Parser
import Parser.ParserToken.*
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.IValue
import TypeChecker.TypeChecker

private fun executeCode(code : String, args: List<Expression.Value>? = null): IValue? {

    val parserOutput = Parser(Lexer(code)).ParsingStart()

    //TypeChecker(parserOutput, args).check()

    return Evaluator().eval(parserOutput,args)?.value

}
fun main(){

    val code = """   
            
            class OpenGL{     
                int §GG = 69;
                string §name = §GG.ToString();
            
                void A(string §name){
                    Println(§name);
                    {
                        string §name = "A";
                        Println(§name);
                    }
                }
                
                void B(int §a){
                    string §c = (§a.ToString() + " Hallo " + §name);
                    Println(§c);
                }
                
                int C(int §a){
                    §GG = §GG + 4200;
                    return §a + 3;
                }
            }
                             
            string Main(){
                openGL §b = OpenGL();
                int §r = §b.C(5) + 5;
                
                Println(§b.§name);
                
                Println(§r);
                §b.B(1);
                                
                return §b.§GG.ToString(); 
            }
            
        """.trimIndent()

    println(executeCode(code))

}