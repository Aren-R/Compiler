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
            scopeAnalyser.run();

            // Type Checking
            TypeChecker typeChecker = new TypeChecker(scopeAnalyser.root, scopeAnalyser.symbolTable);
            typeChecker.run();

        } catch (Exception e) {
            System.out.println("\n"+e.getMessage());;
            return;
        }
    }
}
