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
            SemanticAnalyser SemanticAnalyser = new SemanticAnalyser(syntaxTree.root);
            SemanticAnalyser.runScopeAnalyser();

            //Type Checking
            // SemanticAnalyser.runTypeChecker();


        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
