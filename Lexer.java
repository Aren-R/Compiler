import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Lexer{
    public String inputStream;
    public States DFA; 
    private int lineNumber;
    String ANSI_GREEN = "\u001B[32m";
    String ANSI_RESET = "\u001B[0m";
    String RED = "\u001B[31m";


    public Lexer() {
        String inputFilePath = "input.txt";
        StringBuilder inputStream = new StringBuilder();
        
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                inputStream.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        this.inputStream = inputStream.toString();
        this.DFA = new States();
        this.lineNumber = 1;
    }

    public void setInputStream(String inputStream) {
        this.inputStream = inputStream;
    }

    public String run() throws InvalidTokenException {
        System.out.println("\nRunning Lexer...");
        String tokenisedInputStream = "<TOKENSTREAM>\n";
        Integer indexOfInput = 0;
        Token token = new Token();
        States.State curState = DFA.currentState;

        while (indexOfInput < inputStream.length()) {

            String currentChar = inputStream.substring(indexOfInput, indexOfInput + 1);

            if (currentChar.equals("\n")) {
                lineNumber++;
            }

            if ((currentChar.equals("\r") || currentChar.equals("\t") || currentChar.equals("\n") || currentChar.equals(" ")) && token.contents.equals("")) {
                indexOfInput++;
                continue;
            }

            curState = DFA.transition(currentChar);

            if (curState.classType.equals(RED + "Error: Transition not found" + ANSI_RESET)) {
                throw new InvalidTokenException(RED + "Error at line " + lineNumber + ": Invalid token '" + token.contents + currentChar + "'" + ANSI_RESET);
            }

            if (currentChar.equals(" ") || currentChar.equals("\t") || currentChar.equals("\n") || currentChar.equals("\r")) {
                if (curState.isAccepting) {
                    token.setType(curState.classType);
                    tokenisedInputStream += token.toXML();
                    token.clearToken();
                } else {
                    throw new InvalidTokenException(RED + "Error at line " + lineNumber + ": Invalid token '" + token.contents + "'" + ANSI_RESET);
                }
            } else {
                token.addToToken(currentChar);
            }

            indexOfInput++;
        }

        if (!token.contents.isEmpty()) {
            if (curState.isAccepting) {
                token.setType(curState.classType);
                tokenisedInputStream += token.toXML();
            } else {
                throw new InvalidTokenException(RED + "\nError at line " + lineNumber + ": Invalid token '" + token.contents + "'" + ANSI_RESET);
            }
        }

        token.setType("$");
        token.contents = "$";
        tokenisedInputStream += token.toXML();
        tokenisedInputStream += "</TOKENSTREAM>\n";


        try (FileWriter fileWriter = new FileWriter("Tokens.xml")) {
            fileWriter.write(tokenisedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(ANSI_GREEN + "Lexing Completed" + ANSI_RESET);
        System.out.println(ANSI_GREEN + "Tokens saved to file Tokens.xml\n" + ANSI_RESET);

        return tokenisedInputStream;
    }

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

    public class InvalidTokenException extends Exception {
        public InvalidTokenException(String message) {
            super(message);
        }
    }

}
