public class Main {
    public static void main(String[] args) {
        try {


            //Lexing
            Lexer lexer = new Lexer();
            String resultOfLexer = lexer.run();


            //Parsing
            Parser parser = new Parser();
            parser.setTokenStream(resultOfLexer);
            parser.run();

            //Scope Analysis
            SyntaxTree syntaxTree = new SyntaxTree();
            ScopeAnalyser scopeAnalyser = new ScopeAnalyser(syntaxTree.root);
            scopeAnalyser.runScopeAnalyser();

            //Type Checking
            // ScopeAnalyser.runTypeChecker();


        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
