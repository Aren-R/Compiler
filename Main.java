public class Main {
    public static void main(String[] args) {
        // LEXING
        Lexer lexer = new Lexer();
        String resultOfLexer;
        try {
            resultOfLexer = lexer.runLexer();
            System.out.println("\nLexing Completed");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // PARSING
        Parser parser = new Parser();
        parser.setTokenStream(resultOfLexer); // Set the token stream for parsing
        parser.parse();
        System.out.println("\nParsing Completed");

        // SEMANTIC ANALYSIS (Scope Analyzer)
        ScopeAnalyzer scopeAnalyzer = new ScopeAnalyzer();
        try {
            scopeAnalyzer.analyze("resources/syntaxTree.xml");
            scopeAnalyzer.getScopeManager().printSymbolTable();
            System.out.println("\nScope Analysis Completed");
        } catch (Exception e) {
            System.out.println("Error during scope analysis:");
            e.printStackTrace();
        }

    }
}
