public class Main {
    public static void main(String[] args) {
        try {
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
            parser.setTokenStream(resultOfLexer);
            parser.parse();
            System.out.println("\nParsing Completed");

            // SEMANTIC ANALYSIS (Scope Analyzer)
            SyntaxTree syntaxTree = new SyntaxTree("resources/SyntaxTree.xml");
            ScopeAnalyser ScopeAnalyser = new ScopeAnalyser(syntaxTree.root);
            ScopeAnalyser.analyse();
            System.out.println("\nSemantic Analysis Completed");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }
}
