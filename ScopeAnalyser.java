import java.util.*;

public class ScopeAnalyser {
    public TreeNode root;
    public static int uniqueIDCounter = 0;
    public static int uniqueFunctionCounter = 0;
    public static int uniqueScopeCounter = 0;
    public Stack<Scope> scopeStack = new Stack<>();
    public List<String> bannedVariables;
    SyntaxTree syntaxTree = new SyntaxTree();
    public HashMap<String, SymbolTable.SymbolInfo> symbolTable = new HashMap<>();
    
    public ScopeAnalyser(TreeNode root) {
        this.root = syntaxTree.root;
        // syntaxTree.printTree();
        bannedVariables = new ArrayList<>(Arrays.asList(
            "mul", "main", "grt", "div", "void", "print", "return",
            ",", ";", "=", "(", ")", "{", "}", "num", "not",
            "text", "then", "begin", "end", "else", "eq", "sqrt",
            "sub", "skip", "halt", "if", "or", "and", "add", "<input"));
    }

    public void runScopeAnalyser() {
        scopeStack.push(new Scope(null));
        traverseTree(root);
        // printScopeStack();
        createVTable();
        printVTable();
        System.out.println("Scope Analysis Completed");
        // syntaxTree.renameTree(symbolTable);
        // syntaxTree.printTree();
    }

    public void runTypeChecker() {
        TypeChecker typeChecker = new TypeChecker(root);
        typeChecker.run();
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

            case "PROG": {
                traverseTree(node.children.get(1));
                traverseTree(node.children.get(3));
                traverseTree(node.children.get(2));
                break;
            }

            case "DECL": {
                TreeNode FTYP = node.children.get(0).children.get(0);
                TreeNode FNAME = node.children.get(0).children.get(1);
        
                String type = FTYP.children.get(0).symbol;
                String name = FNAME.children.get(0).symbol;
                String ID = FNAME.children.get(0).id;
                scopeStack.peek().symbolTable.addSymbol(name, type, ID);

                scopeStack.push(new Scope(scopeStack.peek()));
                // printScopeStack();
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
        // System.out.println("Current Scope Stack:");
        for (Scope scope : scopeStack) {
            // System.out.println("Scope ID: " + scope.id);
            printSymbolTable(scope.symbolTable);
        }
    }
    
    public void printSymbolTable(SymbolTable symbolTable) {
        // System.out.println("Symbol Table:");
        for (Map.Entry<String, SymbolTable.SymbolInfo> entry : symbolTable.table.entrySet()) {
            String symbol = entry.getKey();
            SymbolTable.SymbolInfo info = entry.getValue();
            System.out.println("[ID " + info.id + " | Symbol: " + symbol +" | Old Name "+ info.oldName +" | Type: " + info.type + " | Unique Name: " + info.newName+"]");
        }
        System.out.println();
    }

    public void createVTable() {
        for (Scope scope : scopeStack) {
            for (Map.Entry<String, SymbolTable.SymbolInfo> entry : scope.symbolTable.table.entrySet()) {
                SymbolTable.SymbolInfo info = entry.getValue();
                symbolTable.put(info.id, info);
            }
        }
    }

    public void printVTable() {
        for (Map.Entry<String, SymbolTable.SymbolInfo> entry : symbolTable.entrySet()) {
            String symbol = entry.getKey();
            SymbolTable.SymbolInfo info = entry.getValue();
            System.out.println("[ID " + info.id + " | NewName: " + info.newName +" | OldName: "+ info.oldName +" | Type: " + info.type + "]");
        }
    }

    // public void renameSyntaxTree {

    // }

    public void handleGlobVars(TreeNode node) {
        if (node.children.size() == 0) {
            return;
        }

        TreeNode VTYP = node.children.get(0);
        TreeNode VNAME = node.children.get(1);
        String type = VTYP.children.get(0).symbol;
        String name = VNAME.children.get(0).symbol;
        String ID = VNAME.children.get(0).id;
        scopeStack.peek().symbolTable.addSymbol(name, type, ID);

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

        traverseTree(node.children.get(2));
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
        String ID1 = VNAME1.children.get(0).id;

        String type2 = VTYP2.children.get(0).symbol;
        String name2 = VNAME2.children.get(0).symbol;
        String ID2 = VNAME2.children.get(0).id;

        String type3 = VTYP3.children.get(0).symbol;
        String name3 = VNAME3.children.get(0).symbol;
        String ID3 = VNAME3.children.get(0).id;

        scopeStack.peek().symbolTable.addSymbol(name1, type1, ID1);
        scopeStack.peek().symbolTable.addSymbol(name2, type2, ID2);
        scopeStack.peek().symbolTable.addSymbol(name3, type3, ID3);
    }

    public void handleHeader(TreeNode node) {
        TreeNode FNAME = node.children.get(1);
        String name = FNAME.children.get(0).symbol;

        try {
            scopeStack.peek().lookup(name);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }


        TreeNode VNAME1 = node.children.get(3);
        TreeNode VNAME2 = node.children.get(5);
        TreeNode VNAME3 = node.children.get(7);

        String name1 = VNAME1.children.get(0).symbol;
        String name2 = VNAME2.children.get(0).symbol;
        String name3 = VNAME3.children.get(0).symbol;

        String ID1 = VNAME1.children.get(0).id;
        String ID2 = VNAME2.children.get(0).id;
        String ID3 = VNAME3.children.get(0).id;

        scopeStack.peek().symbolTable.addSymbol(name1, "num", ID1);
        scopeStack.peek().symbolTable.addSymbol(name2, "num", ID2);
        scopeStack.peek().symbolTable.addSymbol(name3, "num", ID3);

    }

    public void handleCall(TreeNode node) {
        TreeNode FNAME = node.children.get(0);
        String name = FNAME.children.get(0).symbol;

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

            // if (name.startsWith("F")) {
            //     if (this.symbolTable.table.containsKey(name)) {
            //         return this.symbolTable.table.get(name).newName;
            //     }
            //     throw new Exception("Function " + name + " not declared");
            // } else {

                Scope currentScope = this;
                while (currentScope != null) {
                    if (currentScope.symbolTable.table.containsKey(name)) {
                        return currentScope.symbolTable.table.get(name).newName;
                    }
                    currentScope = currentScope.parent;
                }
                if (name.startsWith("F")) {
                    throw new Exception("Function " + name + " not declared");
                }
                throw new Exception("Variable " + name + " not declared");
            // }
        }

        public String lookupType(String name) throws Exception {
            Scope currentScope = this;
            while (currentScope != null) {
                if (currentScope.symbolTable.table.containsKey(name)) {
                    return currentScope.symbolTable.table.get(name).type;
                }
                currentScope = currentScope.parent;
            }
            throw new Exception("Type not found");
        }
    }

    public class SymbolTable {
        public HashMap<String, SymbolInfo> table;
        
        public SymbolTable() {
            table = new HashMap<>();
        }

        public void addSymbol(String name, String type, String id) {
            if (name.startsWith("F")) {
                if (table.containsKey(name)) {
                    System.out.println("Error: Double decleration of function " + name);
                    System.exit(1);
                }
                table.put(name, new SymbolInfo(name, type, id));
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

            table.put(name, new SymbolInfo(name, type, id));
        }
        
        public class SymbolInfo {
            public String type;
            public String newName;
            public String id;
            public String oldName;

            public SymbolInfo(String name, String type, String id) {
                if (name.startsWith("F")) {
                    this.type = type;
                    this.newName = "func_" + uniqueFunctionCounter++;
                    this.id = id;
                    this.oldName = name;
                } else {
                    this.type = type;
                    this.newName = "var_" + uniqueIDCounter++;
                    this.id = id;
                    this.oldName = name;
                }

            }

        }
    }
}
