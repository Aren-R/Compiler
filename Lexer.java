import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Lexer {
    public String inputStream; // The code that has to be lexed
    public States DFA; 
    private int lineNumber; // Counter for tracking the line number

    public Lexer() {
        String inputFilePath = "resources/input.txt";  // Update this with your file path
        StringBuilder inputStream = new StringBuilder();
        
        // Read the file contents
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                inputStream.append(line).append("\n");  // Add a newline character after each line
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        this.inputStream = inputStream.toString();
        this.DFA = new States();
        this.lineNumber = 1; // Start from line 1
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

        // Continuously read the input stream
        while (indexOfInput < inputStream.length()) {

            String currentChar = inputStream.substring(indexOfInput, indexOfInput + 1);
            System.out.println("Processing character: " + currentChar);

            // Track line numbers (newline character increases the line number)
            if (currentChar.equals("\n")) {
                lineNumber++;
            }

            // Skip leading whitespace characters (only if token is empty)
            if ((currentChar.equals("\r") || currentChar.equals("\t") || currentChar.equals("\n") || currentChar.equals(" ")) && token.contents.equals("")) {
                indexOfInput++;
                continue;
            }

            curState = DFA.transition(currentChar);
            System.out.println("Current state: " + curState.classType);

            if (curState.classType.equals("Error: Transition not found")) {
                return "Error at line " + lineNumber + ": Invalid token '" + token.contents + currentChar + "'";
            }

            if (currentChar.equals(" ") || currentChar.equals("\t") || currentChar.equals("\n") || currentChar.equals("\r")) {
                if (curState.isAccepting) {
                    token.setType(curState.classType);
                    tokenisedInputStream += token.toXML();
                    token.clearToken();
                } else {
                    return "Error at line " + lineNumber + ": Invalid token '" + token.contents + "'";
                }
            } else {
                token.addToToken(currentChar);
            }

            indexOfInput++;
        }

        // Check if there's an incomplete token at the end of input
        if (!token.contents.isEmpty()) {
            if (curState.isAccepting) {
                token.setType(curState.classType);
                tokenisedInputStream += token.toXML();
            } else {
                return "Error at line " + lineNumber + ": Incomplete or invalid token '" + token.contents + "'";
            }
        }

        tokenisedInputStream += "</TOKENSTREAM>\n";

        // Save to XML
        try (FileWriter fileWriter = new FileWriter("Tokens.xml")) {
            fileWriter.write(tokenisedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokenisedInputStream;
    }

    //================================================================================================
    //THIS IS THE TOKEN CLASS WHICH RETURNS XML'ified TOKENS
    public class Token {
        public static int ID = 1;
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
