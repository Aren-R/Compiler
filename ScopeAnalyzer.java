import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class SymbolTable {
    private Map<String, SymbolInfo> table;

    public SymbolTable() {
        table = new HashMap<>();
    }

    public boolean addSymbol(String name, SymbolInfo info) {
        if (table.containsKey(name)) {
            return false; // duplicate declaration
        }
        table.put(name, info);
        return true;
    }

    public SymbolInfo lookup(String name) {
        return table.get(name);
    }
}

class Scope {
    private SymbolTable symbolTable;
    private Scope parentScope;

    public Scope(Scope parent) {
        this.symbolTable = new SymbolTable();
        this.parentScope = parent;
    }

    public SymbolInfo resolve(String name) {
        SymbolInfo symbol = symbolTable.lookup(name);
        if (symbol != null) {
            return symbol;
        } else if (parentScope != null) {
            return parentScope.resolve(name);
        }
        return null;
    }

    public boolean addSymbol(String name, SymbolInfo info) {
        return symbolTable.addSymbol(name, info);
    }

    public Scope getParentScope() {
        return parentScope;
    }
}

class SymbolInfo {
    String type;
    int nodeId;

    public SymbolInfo(String type, int nodeId) {
        this.type = type;
        this.nodeId = nodeId;
    }
}

public class ScopeAnalyzer {

    private Scope globalScope;
    private Stack<Scope> scopeStack;

    public ScopeAnalyzer() {
        this.globalScope = new Scope(null);
        this.scopeStack = new Stack<>();
        this.scopeStack.push(globalScope);
    }

    public void analyzeSyntaxTree(String xmlFilePath) {
        // Load and parse the XML file into a syntax tree representation (DOM, SAX, etc.)
        // Traverse the tree and populate the symbol table by crawling nodes.
        // Implement your XML parser here and pass the root node to the traverse function.
    }

    public void traverse(Node currentNode) {
        // Perform the tree traversal, checking for rules
        if (currentNode.getNodeName().equals("FUNCTION")) {
            enterScope(currentNode);
            handleFunctionDeclaration(currentNode);
        } else if (currentNode.getNodeName().equals("VARIABLE")) {
            handleVariableDeclaration(currentNode);
        }
        
        // Recursively traverse child nodes
        for (Node child : currentNode.getChildren()) {
            traverse(child);
        }
        
        if (currentNode.getNodeName().equals("FUNCTION")) {
            exitScope(); // exit function scope
        }
    }

    private void enterScope(Node functionNode) {
        Scope newScope = new Scope(scopeStack.peek()); // new scope with parent as current scope
        scopeStack.push(newScope);
    }

    private void exitScope() {
        scopeStack.pop(); // pop the current scope and go back to the parent
    }

    private void handleFunctionDeclaration(Node functionNode) {
        String functionName = extractFunctionName(functionNode);
        if (functionName.equals("main")) {
            // Special handling for 'main', no recursion allowed
            checkForRecursiveMainCall(functionNode);
        }
        Scope currentScope = scopeStack.peek();
        SymbolInfo symbolInfo = new SymbolInfo("function", getNodeId(functionNode));
        if (!currentScope.addSymbol(functionName, symbolInfo)) {
            throw new RuntimeException("Function already declared in the same scope: " + functionName);
        }
    }

    private void handleVariableDeclaration(Node variableNode) {
        String variableName = extractVariableName(variableNode);
        Scope currentScope = scopeStack.peek();
        SymbolInfo symbolInfo = new SymbolInfo("variable", getNodeId(variableNode));
        if (!currentScope.addSymbol(variableName, symbolInfo)) {
            throw new RuntimeException("Variable already declared in the same scope: " + variableName);
        }
    }

    private void checkForRecursiveMainCall(Node functionCallNode) {
        String functionName = extractFunctionName(functionCallNode);
        if (functionName.equals("main")) {
            throw new RuntimeException("Recursive call to 'main' is not allowed.");
        }
    }

    private String extractFunctionName(Node functionNode) {
        // Extract the function name from the XML node (e.g., <FNAME>)
        return ""; // placeholder
    }

    private String extractVariableName(Node variableNode) {
        // Extract the variable name from the XML node (e.g., <VNAME>)
        return ""; // placeholder
    }

    private int getNodeId(Node node) {
        // Extract the unique ID from the XML node (<UNID>)
        return Integer.parseInt(node.getElementsByTagName("UNID").item(0).getTextContent());
    }

    // Add other necessary utility methods for XML parsing and node handling.
}
