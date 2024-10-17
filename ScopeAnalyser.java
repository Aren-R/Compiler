import java.util.*;

public class ScopeAnalyser {
    public TreeNode root;
    public static int uniqueIDCounter = 0;
    public static int uniqueFunctionCounter = 0;
    public static int uniqueScopeCounter = 0;
    public Stack<Scope> scopeStack = new Stack<>();
    public List<String> bannedVariables;
    
    public ScopeAnalyser(TreeNode root) {
        bannedVariables = new ArrayList<>(Arrays.asList(
            "mul", "main", "grt", "div", "void", "print", "return",
            ",", ";", "=", "(", ")", "{", "}", "num", "not",
            "text", "then", "begin", "end", "else", "eq", "sqrt",
            "sub", "skip", "halt", "if", "or", "and", "add", "<input"));
        this.root = root;
    }

    public void analyse() {
        scopeStack.push(new Scope(null));
        traverseTree(root);
    }

    public void traverseTree(TreeNode node) {
        switch (node.symbol) {
            case "GLOBVARS": {
                handleGlobVars(node);
                break;
            }

            case "ATOMIC": {
                handleAtomic(node);
                break;
            }

            case "ASSIGN": {
                handleAssign(node);
                break;
            }

            case "LOCVARS": {
                handleLocVars(node);
                break;
            }

            case "HEADER": {
                handleHeader(node);
                break;
            }

            case "CALL": {
                handleCall(node);
                break;
            }

            case "DECL": {
                scopeStack.push(new Scope(scopeStack.peek()));
                printScopeStack();
                handleHeader(node.children.get(0));
                traverseTree(node.children.get(1));
                break;
            }
            
            default: {
                for (TreeNode child : node.children) {
                    traverseTree(child);
                }
                break;
            }
        }
    }

    public void printScopeStack() {
        System.out.println("Current Scope Stack:");
        for (Scope scope : scopeStack) {
            System.out.println("Scope ID: " + scope.id);
            printSymbolTable(scope.symbolTable);
        }
    }
    
    public void printSymbolTable(Scope.SymbolTable symbolTable) {
        System.out.println("Symbol Table:");
        for (Map.Entry<String, Scope.SymbolTable.SymbolInfo> entry : symbolTable.table.entrySet()) {
            String symbol = entry.getKey();
            Scope.SymbolTable.SymbolInfo info = entry.getValue();
            System.out.println("[Symbol: " + symbol + ", type: " + info.type + ", unique name: " + info.newName+"]");
        }
        System.out.println();
    }

    public void handleGlobVars(TreeNode node) {
        if (node.children.size() == 0) {
            return;
        }

        TreeNode VTYP = node.children.get(0);
        TreeNode VNAME = node.children.get(1);
        String type = VTYP.children.get(0).symbol;
        String name = VNAME.children.get(0).symbol;
        scopeStack.peek().symbolTable.addSymbol(name, type);

        handleGlobVars(node.children.get(3));
    }

    public void handleAtomic(TreeNode node) {
        TreeNode child = node.children.get(0); // Either CONST or VNAME
        if (child.symbol.equals("CONST")) {
            return;
        }
        
        if (child.symbol.equals("VNAME")) {
            String name = child.children.get(0).symbol;
            try {
                scopeStack.peek().lookup(name);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }
        
    }

    public void handleAssign(TreeNode node) {
        TreeNode child = node.children.get(0); // VNAME
        String name = child.children.get(0).symbol;

        try {
            scopeStack.peek().lookup(name);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public void handleLocVars(TreeNode node) {

        TreeNode VTYP1 = node.children.get(0);
        TreeNode VNAME1 = node.children.get(1);

        TreeNode VTYP2 = node.children.get(3);
        TreeNode VNAME2 = node.children.get(4);

        TreeNode VTYP3 = node.children.get(6);
        TreeNode VNAME3 = node.children.get(7);

        String type1 = VTYP1.children.get(0).symbol;
        String name1 = VNAME1.children.get(0).symbol;

        String type2 = VTYP2.children.get(0).symbol;
        String name2 = VNAME2.children.get(0).symbol;

        String type3 = VTYP3.children.get(0).symbol;
        String name3 = VNAME3.children.get(0).symbol;

        scopeStack.peek().symbolTable.addSymbol(name1, type1);
        scopeStack.peek().symbolTable.addSymbol(name2, type2);
        scopeStack.peek().symbolTable.addSymbol(name3, type3);
    }

    public void handleHeader(TreeNode node) {
        TreeNode FTYP = node.children.get(0);
        TreeNode FNAME = node.children.get(1);

        String type = FTYP.children.get(0).symbol;
        String name = FNAME.children.get(0).symbol;

        scopeStack.peek().symbolTable.addSymbol(name, type);

        TreeNode VNAME1 = node.children.get(3);
        TreeNode VNAME2 = node.children.get(5);
        TreeNode VNAME3 = node.children.get(7);

        String name1 = VNAME1.children.get(0).symbol;
        String name2 = VNAME2.children.get(0).symbol;
        String name3 = VNAME3.children.get(0).symbol;

        scopeStack.peek().symbolTable.addSymbol(name1, "num");
        scopeStack.peek().symbolTable.addSymbol(name2, "num");
        scopeStack.peek().symbolTable.addSymbol(name3, "num");

    }

    public void handleCall(TreeNode node) {
        TreeNode FNAME = node.children.get(0);
        String name = FNAME.children.get(0).symbol;
        System.out.println(name);

        try {
            scopeStack.peek().lookup(name);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        handleAtomic(node.children.get(2));
        handleAtomic(node.children.get(4));
        handleAtomic(node.children.get(6));
    }
    
    public class Scope {
        public Integer id;
        public Scope parent;
        public SymbolTable symbolTable;

        public Scope(Scope parent) {
            this.id = uniqueScopeCounter++;
            this.parent = parent;
            this.symbolTable = new SymbolTable();
        }

        public String lookup(String name) throws Exception {

            if (name.startsWith("F")) {
                if (this.symbolTable.table.containsKey(name)) {
                    return this.symbolTable.table.get(name).newName;
                }
                throw new Exception("Function " + name + " not declared");
            } else {
                Scope currentScope = this;
                while (currentScope != null) {
                    if (currentScope.symbolTable.table.containsKey(name)) {
                        return currentScope.symbolTable.table.get(name).newName;
                    }
                    currentScope = currentScope.parent;
                }
                throw new Exception("Variable " + name + " not declared");
            }
        }

        public class SymbolTable {
            public HashMap<String, SymbolInfo> table;
            
            public SymbolTable() {
                table = new HashMap<>();
            }

            public void addSymbol(String name, String type) {
                if (name.startsWith("F")) {
                    if (table.containsKey(name)) {
                        System.out.println("Error: Double decleration of function " + name);
                        System.exit(1);
                    }
                    table.put(name, new SymbolInfo(name, type));
                    return;
                }


                if (bannedVariables.contains(name.substring(2))) {
                    System.out.println("Error: Variable name " + name + " is reserved");
                    System.exit(1);
                }

                if (table.containsKey(name)) {
                    System.out.println("Error: Double decleration of variable " + name);
                    System.exit(1);
                }

                table.put(name, new SymbolInfo(name, type));
            }
            
            public class SymbolInfo {
                public String type;
                public String newName;
                
                public SymbolInfo(String name, String type) {
                    if (name.startsWith("F")) {
                        this.type = type;
                        this.newName = "func_" + uniqueFunctionCounter++;
                    } else {
                        this.type = type;
                        this.newName = "var_" + uniqueIDCounter++;
                    }

                }
            }
        }
    }
}
