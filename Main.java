public class Main {
    public static void main(String[] args) {
        //CODE TO LEX
        String inputStream = "main \n ( ) ";
        //===========
        //CREATE LEXER
        Lexer lexer = new Lexer();
        lexer.setInputStream(inputStream);  //set the code that has to be lexed
        //===========
        //RUN THE LEXER
        String resultOfLexer = lexer.runLexer();
        System.out.println(resultOfLexer);
        
    }
}