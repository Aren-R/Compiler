public class Lexer {
    public String inputStream; // The code that has to be lexed

    public Lexer() {
        this.inputStream = "";
    }

    // Set the code that has to be lexed
    public void setInputStream(String inputStream) {
        this.inputStream = inputStream;
    }

    // Run the lexer
    public String runLexer() {
        String tokenisedInputStream = "<TOKENSTREAM>\n";
        Integer indexOfInput = 0;

        //Continuously read the input stream
        while (indexOfInput < inputStream.length() ) {
            //get the current character
            char currentChar = inputStream.charAt(indexOfInput);

        }

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
    
        public void setToken(String contents, String classType) {
            this.contents = contents;
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