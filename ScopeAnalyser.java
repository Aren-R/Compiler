import java.util.*;

public class ScopeAnalyser {
    private Scope globalScope; // The main scope
    private Scope currentScope; // Tracks the current scope during traversal
    private Map<String, Scope> scopeMap; // For quickly accessing scopes by function names (FNAME)
    
    public ScopeAnalyser() {
        this.globalScope = new Scope("main", null); // Main program's highest scope
        this.currentScope = globalScope;
        this.scopeMap = new HashMap<>();
        scopeMap.put("main", globalScope);
    }

    // Start the scope analysis by traversing the tree
    public void analyze(TreeNode root) {
        traverse(root);  // Start traversal from the root node
    }

    // Recursive method to traverse the tree and handle scope changes
    private void traverse(TreeNode node) {
        String symbol = node.getSymbol();

        switch (symbol) {
            case "PROG":
                // Root of the program, process global variables and functions
                for (TreeNode child : node.getChildren()) {
                    traverse(child);
                }
                break;

            case "DECL": // Function declaration
                TreeNode header = node.getChildren().get(0); // HEADER node
                TreeNode body = node.getChildren().get(1); // BODY node
                processFunctionDeclaration(header, body);
                break;

            case "VNAME": // Variable declaration or usage
                processVariable(node);
                break;

            case "CALL": // Function call
                processFunctionCall(node);
                break;

            case "ASSIGN": // Assignment
                processAssignment(node);
                break;

            default:
                // Traverse the rest of the tree recursively
                for (TreeNode child : node.getChildren()) {
                    traverse(child);
                }
                break;
        }
    }

    // Process function declaration (opens a new scope)
    private void processFunctionDeclaration(TreeNode header, TreeNode body) {
        String functionName = header.getChildren().get(1).getSymbol(); // FNAME
        if (scopeMap.containsKey(functionName)) {
            throw new IllegalArgumentException("Function " + functionName + " is already declared.");
        }

        // Open a new scope for this function
        Scope functionScope = new Scope(functionName, currentScope);
        currentScope.addChildScope(functionScope);
        scopeMap.put(functionName, functionScope);
        currentScope = functionScope;

        // Process function parameters (VNAMEs in HEADER)
        for (int i = 2; i < 5; i++) {
            TreeNode param = header.getChildren().get(i);
            processVariableDeclaration(param, "param");
        }

        // Traverse the function body within this new scope
        traverse(body);

        // Close the function scope (move back to parent scope)
        currentScope = currentScope.getParent();
    }

    // Process variable declaration
    private void processVariableDeclaration(TreeNode node, String varType) {
        String varName = node.getSymbol();
        if (currentScope.isVariableDeclared(varName)) {
            throw new IllegalArgumentException("Variable " + varName + " is already declared in this scope.");
        }

        // Add the variable to the current scope
        currentScope.addSymbol(new Symbol(varName, varType, currentScope));
    }

    // Process variable usage
    private void processVariable(TreeNode node) {
        String varName = node.getSymbol();
        if (!currentScope.isVariableDeclaredInScope(varName)) {
            throw new IllegalArgumentException("Variable " + varName + " is not declared in this or ancestor scope.");
        }
    }

    // Process function call
    private void processFunctionCall(TreeNode node) {
        String functionName = node.getChildren().get(0).getSymbol(); // FNAME
        if (functionName.equals("main")) {
            throw new IllegalArgumentException("Cannot recursively call 'main' function.");
        }
        if (!scopeMap.containsKey(functionName)) {
            throw new IllegalArgumentException("Function " + functionName + " is not declared.");
        }
    }

    // Process variable assignment
    private void processAssignment(TreeNode node) {
        String varName = node.getChildren().get(0).getSymbol(); // VNAME
        processVariable(node.getChildren().get(0)); // Ensure variable is declared
        traverse(node.getChildren().get(1)); // Traverse the right-hand side of the assignment
    }

    public class Scope {
        private String name;
        private Scope parent;
        private Map<String, Symbol> symbols; // Symbol table for this scope
        private List<Scope> childScopes;
    
        public Scope(String name, Scope parent) {
            this.name = name;
            this.parent = parent;
            this.symbols = new HashMap<>();
            this.childScopes = new ArrayList<>();
        }
    
        // Add a child scope
        public void addChildScope(Scope child) {
            if (childScopes.stream().anyMatch(scope -> scope.getName().equals(child.getName()))) {
                throw new IllegalArgumentException("Child scope " + child.getName() + " already exists under " + name);
            }
            childScopes.add(child);
        }
    
        // Add a symbol to this scope
        public void addSymbol(Symbol symbol) {
            if (symbols.containsKey(symbol.getName())) {
                throw new IllegalArgumentException("Symbol " + symbol.getName() + " is already declared in this scope.");
            }
            symbols.put(symbol.getName(), symbol);
        }
    
        // Check if a variable is declared in this scope
        public boolean isVariableDeclared(String varName) {
            return symbols.containsKey(varName);
        }
    
        // Check if a variable is declared in this scope or any ancestor scope
        public boolean isVariableDeclaredInScope(String varName) {
            if (isVariableDeclared(varName)) {
                return true;
            } else if (parent != null) {
                return parent.isVariableDeclaredInScope(varName);
            }
            return false;
        }
    
        public String getName() {
            return name;
        }
    
        public Scope getParent() {
            return parent;
        }
    }

    public class Symbol {
        private String name;
        private String type;
        private Scope scope;
    
        public Symbol(String name, String type, Scope scope) {
            this.name = name;
            this.type = type;
            this.scope = scope;
        }
    
        public String getName() {
            return name;
        }
    
        public String getType() {
            return type;
        }
    
        public Scope getScope() {
            return scope;
        }
    }
    
}
