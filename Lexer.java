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
        States.State curState = DFA.currentState;

        //Continuously read the input stream
        while (indexOfInput < inputStream.length() ) {

            String currentChar = inputStream.substring(indexOfInput, indexOfInput + 1);
            curState = DFA.transition(currentChar);
            if (curState.classType.equals("Error: Transition not found")) {
                return "Error: Transition not found";
            }

            if (currentChar.equals(" ")) {
                if (curState.isAccepting) {
                    token.setType(curState.classType);
                    tokenisedInputStream += token.toXML();
                    token.clearToken();
                } else {
                    return "Error: Invalid token";
                }
            } else {
                token.addToToken(currentChar);
            }

            
            indexOfInput++;

            // System.out.println(tokenisedInputStream);
        }

        if (curState.isAccepting) {
            token.setType(curState.classType);
            tokenisedInputStream += token.toXML();
            tokenisedInputStream += "</TOKENSTREAM>\n";
        } else {
            return "Error";
        }

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