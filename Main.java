public class Main {
    public static void main(String[] args) {
        try {


            Lexer lexer = new Lexer();
            String resultOfLexer = lexer.run();
            System.out.println("\nLexing Completed\n");


            Parser parser = new Parser();
            parser.setTokenStream(resultOfLexer);
            parser.run();

            
            ScopeAnalyser ScopeAnalyser = new ScopeAnalyser();
            ScopeAnalyser.run();
            System.out.println("\nScope Analysis Completed\n");



        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
