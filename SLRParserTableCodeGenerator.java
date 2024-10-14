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

            writer.write("//Populate the TERMINALS and NONTERMINALS\n");
            writer.write("Map<Integer, Map<String, String>> terminals = new HashMap<>();\n");
            writer.write("Map<Integer, Map<String, Integer>> nonTerminals = new HashMap<>();\n");

            String line;
            // Read the CSV file line by line
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");  // Assuming comma-separated values
                int state = Integer.parseInt(row[0].trim());  // First column is the state number

                // terminal Table: Check if there's any non-empty terminal in the row
                boolean hasterminal = false;
                StringBuilder terminalCode = new StringBuilder();
                terminalCode.append("\n// State ").append(state).append(" terminal table\n");
                terminalCode.append("Map<String, String> terminalRow").append(state).append(" = new HashMap<>();\n");

                // Loop only over terminal symbols for terminal table
                for (int i = 1; i <= terminals.size(); i++) {
                    if (i < row.length) {  // Ensure we're within bounds of the row
                        String terminal = row[i].trim();
                        if (!terminal.isEmpty()) {
                            hasterminal = true;
                            terminalCode.append("terminalRow").append(state).append(".put(\"").append(terminals.get(i - 1)).append("\", \"").append(terminal).append("\");\n");
                        }
                    }
                }

                // Write the terminal table code only if there's something to write
                if (hasterminal) {
                    terminalCode.append("terminalTable.put(").append(state).append(", terminalRow").append(state).append(");\n");
                    writer.write(terminalCode.toString());
                }

                // nonTerminal Table: Check if there's any non-empty nonTerminal entry in the row
                boolean hasnonTerminal = false;
                StringBuilder nonTerminalCode = new StringBuilder();
                nonTerminalCode.append("\n// State ").append(state).append(" nonTerminal table\n");
                nonTerminalCode.append("Map<String, Integer> nonTerminalRow").append(state).append(" = new HashMap<>();\n");

                // Loop only over non-terminal symbols for nonTerminal table
                for (int i = terminals.size() + 1; i < row.length; i++) {
                    String nonTerminalState = row[i].trim();
                    if (!nonTerminalState.isEmpty()) {
                        hasnonTerminal = true;
                        nonTerminalCode.append("nonTerminalRow").append(state).append(".put(\"").append(nonTerminals.get(i - terminals.size() - 1)).append("\", ").append(nonTerminalState).append(");\n");
                    }
                }

                // Write the nonTerminal table code only if there's something to write
                if (hasnonTerminal) {
                    nonTerminalCode.append("nonTerminalTable.put(").append(state).append(", nonTerminalRow").append(state).append(");\n");
                    writer.write(nonTerminalCode.toString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Code generation completed. Check the output file.");
    }
}
