public class Main {
    public static void main(String[] args) {
        // LEXING
        Lexer lexer = new Lexer();
        String resultOfLexer = lexer.runLexer();
        // System.out.println(resultOfLexer);

        // PARSING
        Parser parser = new Parser();
        parser.setTokenStream(resultOfLexer); // Set the token stream for parsing
        parser.parse(); // Start parsing
    }
}
