public class Lexer {
    public String inputStream; // The code that has to be lexed
    public States DFA; 

    public Lexer() {
        this.inputStream = "";
        this.DFA = new States();
    }

    // Set the code that has to be lexed
    public void setInputStream(String inputStream) {
        this.inputStream = inputStream;
    }

    // Run the lexer
    public String runLexer() {
        String tokenisedInputStream = "<TOKENSTREAM>\n";
        Integer indexOfInput = 0;
        Token token = new Token();

        //Continuously read the input stream
        while (indexOfInput < inputStream.length() ) {

            String currentChar = inputStream.substring(indexOfInput, indexOfInput + 1);
            States.State nextState = DFA.transition(currentChar);
            if (nextState.classType == "ERROR") {
                System.out.println("ERROR");
                return "ERROR";
            }

            if (currentChar.equals(" ") && nextState.isAccepting) {
                token.addToToken(currentChar);
                token.setType(nextState.classType);
                tokenisedInputStream += token.toXML();
                token.clearToken();
            } else {
                token.addToToken(currentChar);
                indexOfInput++;
            }

        }

        tokenisedInputStream += token.toXML();
        tokenisedInputStream += "</TOKENSTREAM>\n";
        return tokenisedInputStream;
    }

    //================================================================================================
    //THIS IS THE TOKEN CLASS WHICH RETURNS XML'ified TOKENS
    public class Token {
        public static int ID = 0;
        public String contents;
        public String classType;
    
        public Token() {
            contents = "";
            classType = "";
        }

        public void addToToken(String add) {
            this.contents += add;
        }
    
        public void setType(String classType) {
            this.classType = classType;
        }
    
        public String toXML() {
            String xml = "<TOK>\n";
            xml += "<ID>" + Token.ID + "</ID>\n";
            xml += "<CLASS>" + this.classType + "</CLASS>\n";
            xml += "<WORD>" + this.contents + "</WORD>\n";
            xml += "</TOK>\n";
            return xml;
        }
    
        public void clearToken() {
            Token.ID += 1;
            this.contents = "";
            this.classType = "";
        }
    }
    //================================================================================================
}