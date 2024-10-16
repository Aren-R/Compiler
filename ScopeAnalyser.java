import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;


class SymbolTable {
    private Map<String, Symbol> table;

    public SymbolTable() {
        table = new HashMap<>();
    }

    public void addSymbol(String name, Symbol symbol) throws Exception {
        if (table.containsKey(name)) {
            throw new Exception("Symbol " + name + " already declared in this scope.");
        }
        table.put(name, symbol);
    }

    public Symbol getSymbol(String name) {
        return table.get(name);
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }

    public Map<String, Symbol> getSymbols() {
        return table;
    }
}

class Symbol {
    private String name;
    private String type;
    private int scopeId;

    public Symbol(String name, String type, int scopeId) {
        this.name = name;
        this.type = type;
        this.scopeId = scopeId;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public int getScopeId() { return scopeId; }
}


class ScopeManager {
    private Stack<Integer> scopeStack;
    private Map<Integer, SymbolTable> scopeTables;
    private int currentScopeId;

    public ScopeManager() {
        scopeStack = new Stack<>();
        scopeTables = new HashMap<>();
        currentScopeId = 0;
        openScope();
    }

    public void openScope() {
        currentScopeId++;
        scopeStack.push(currentScopeId);
        scopeTables.put(currentScopeId, new SymbolTable());
    }

    public void closeScope() {
        scopeStack.pop();
    }

    public SymbolTable getCurrentScope() {
        return scopeTables.get(scopeStack.peek());
    }

    public int getCurrentScopeId() {
        return scopeStack.peek();
    }

    public boolean symbolExistsInCurrentScope(String name) {
        return getCurrentScope().contains(name);
    }
    
    public boolean symbolExistsInAncestorScopes(String name) {
        for (int scopeId : scopeStack) {
            if (scopeTables.get(scopeId).contains(name)) {
                return true;
            }
        }
        return false;
    }

    public void printSymbolTable() {
        System.out.println("\n--- Symbol Table ---");
        for (Map.Entry<Integer, SymbolTable> entry : scopeTables.entrySet()) {
            int scopeId = entry.getKey();
            SymbolTable table = entry.getValue();
            System.out.println("Scope ID: " + scopeId);
            
            // Use the getter method to access the symbol map
            for (Map.Entry<String, Symbol> symbolEntry : table.getSymbols().entrySet()) {
                Symbol symbol = symbolEntry.getValue();
                System.out.println("  Symbol Name: " + symbol.getName() + ", Type: " + symbol.getType() + ", Scope ID: " + symbol.getScopeId());
            }
        }
        System.out.println("--------------------");
    }

    public void addSymbolToCurrentScope(String name, Symbol symbol) throws Exception {
        getCurrentScope().addSymbol(name, symbol);
    }
}


class ScopeAnalyzer {
    private ScopeManager scopeManager;

    public ScopeAnalyzer() {
        scopeManager = new ScopeManager();
    }

    public ScopeManager getScopeManager() {
        return scopeManager;
    }

    public void analyze(String filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(filePath);
        Element root = document.getDocumentElement();
        
        traverseSyntaxTree(root);
    }

    private void traverseSyntaxTree(Node node) throws Exception {
        String nodeName = node.getNodeName();

        if (nodeName.equals("PROG") || nodeName.equals("FUNCTIONS")) {
            handleFunctionScope(node);
        } else if (nodeName.equals("VNAME")) {
            handleVariableDeclaration(node);
        } else if (nodeName.equals("CALL")) {
            handleFunctionCall(node);
        }

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            traverseSyntaxTree(children.item(i));
        }
    }

    private void handleFunctionScope(Node node) throws Exception {
        scopeManager.openScope();
        
        String functionName = getChildNodeValue(node, "FNAME");
        if (scopeManager.symbolExistsInCurrentScope(functionName)) {
            throw new Exception("Function " + functionName + " already declared in the current scope.");
        }
        Symbol functionSymbol = new Symbol(functionName, "function", scopeManager.getCurrentScopeId());
        scopeManager.addSymbolToCurrentScope(functionName, functionSymbol);

        scopeManager.closeScope();
    }

    private void handleVariableDeclaration(Node node) throws Exception {
        String variableName = getChildNodeValue(node, "VNAME");
        if (scopeManager.symbolExistsInCurrentScope(variableName)) {
            throw new Exception("Variable " + variableName + " already declared in this scope.");
        }
        Symbol variableSymbol = new Symbol(variableName, "variable", scopeManager.getCurrentScopeId());
        scopeManager.addSymbolToCurrentScope(variableName, variableSymbol);
    }

    private void handleFunctionCall(Node node) throws Exception {
        String functionName = getChildNodeValue(node, "FNAME");
        if (!scopeManager.symbolExistsInAncestorScopes(functionName)) {
            throw new Exception("Function " + functionName + " not declared.");
        }
        if (functionName.equals("main")) {
            throw new Exception("Recursive call to main function is not allowed.");
        }
    }

    private String getChildNodeValue(Node node, String childName) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(childName)) {
                return child.getTextContent();
            }
        }
        return null;
    }
}
