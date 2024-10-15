import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SLRParserTableCodeGenerator {
    public static void main(String[] args) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        // List of terminals for the terminal table
        List<String> terminals = Arrays.asList("main", ",", "num", "text", "V", "begin", "end", ";", "skip", "halt", "print", "N", "T", "<", "input", "=", "(", ")", "if", "then", "else", "not", "sqrt", "or", "and", "eq", "grt", "add", "sub", "mul", "div", "F", "void", "{", "}", "return", "$");

        // List of non-terminals for the nonTerminal table
        List<String> nonTerminals = Arrays.asList("PROG", "GLOBVARS", "VTYP", "VNAME", "ALGO", "INSTRUC", "COMMAND", "ATOMIC", "CONST", "ASSIGN", "CALL", "BRANCH", "TERM", "OP", "ARG", "COND", "SIMPLE", "COMPOSIT", "UNOP", "BINOP", "FNAME", "FUNCTIONS", "DECL", "HEADER", "FTYP", "BODY", "PROLOG", "EPILOG", "LOCVARS", "SUBFUNCS");

        // Path to your CSV file
        String csvFile = "ParseTable.csv";
        String outputFile = "output_file.txt";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            // Read the CSV file line by line
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");  // Assuming comma-separated values
                int state = Integer.parseInt(row[0].trim());  // First column is the state number

                // Collect terminal and non-terminal rows
                List<String> terminalRow = new ArrayList<>();
                List<String> nonTerminalRow = new ArrayList<>();

                // Loop only over terminal symbols for terminal table
                for (int i = 1; i <= terminals.size(); i++) {
                    if (i < row.length) {  // Ensure we're within bounds of the row
                        String terminal = row[i].trim();
                        if (!terminal.isEmpty()) {
                            terminalRow.add(terminals.get(i - 1)); // Add terminal name to the list
                            terminalRow.add(terminal); // Add corresponding action
                        }
                    }
                }

                // Loop only over non-terminal symbols for nonTerminal table
                for (int i = terminals.size() + 1; i < row.length; i++) {
                    String nonTerminalState = row[i].trim();
                    if (!nonTerminalState.isEmpty()) {
                        nonTerminalRow.add(nonTerminals.get(i - terminals.size() - 1)); // Add non-terminal name
                        nonTerminalRow.add(nonTerminalState); // Add corresponding action
                    }
                }

                // Write terminal row if it contains data
                if (!terminalRow.isEmpty()) {
                    writer.write("addTerminalRow(terminalTable, " + state + ", ");
                    for (int i = 0; i < terminalRow.size(); i++) {
                        writer.write("\"" + terminalRow.get(i) + "\"");
                        if (i < terminalRow.size() - 1) {
                            writer.write(", ");
                        }
                    }
                    writer.write(");\n");
                }

                // Write non-terminal row if it contains data
                if (!nonTerminalRow.isEmpty()) {
                    writer.write("addNonTerminalRow(nonTerminalTable, " + state + ", ");
                    for (int i = 0; i < nonTerminalRow.size(); i++) {
                        writer.write("\"" + nonTerminalRow.get(i) + "\"");
                        if (i < nonTerminalRow.size() - 1) {
                            writer.write(", ");
                        }
                    }
                    writer.write(");\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Code generation completed. Check the output file.");
    }
}
