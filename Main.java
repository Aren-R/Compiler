import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // File path for the input file
        String inputFilePath = "input.txt";  // Update this with your file path
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
        
        //===========  
        //CREATE LEXER
        Lexer lexer = new Lexer();
        lexer.setInputStream(inputStream.toString());  // Set the file content as input to the lexer
        //===========  
        //RUN THE LEXER
        String resultOfLexer = lexer.runLexer();
        System.out.println(resultOfLexer);
        
        // Save the result to an XML file
        try (FileWriter fileWriter = new FileWriter("result.xml")) {
            fileWriter.write(resultOfLexer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
