import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // File path for the input file
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
        
        //LEXING=============================================================================================

        Lexer lexer = new Lexer();
        lexer.setInputStream(inputStream.toString());  // Set the file content as input to the lexer
        String resultOfLexer = lexer.runLexer();
        System.out.println(resultOfLexer);
        
        // Save to XML
        try (FileWriter fileWriter = new FileWriter("Tokens.xml")) {
            fileWriter.write(resultOfLexer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //LEXING=============================================================================================

        //PARSING=============================================================================================

        //PARSING=============================================================================================
    }
}
