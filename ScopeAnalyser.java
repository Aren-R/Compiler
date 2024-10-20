import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

public class ScopeAnalyser {
    public TreeNode root;
    public static int uniqueIDCounter = 0;
    public static int uniqueFunctionCounter = 0;
    public static int uniqueScopeCounter = 0;
    public Stack<Scope> scopeStack = new Stack<>();
    public List<Scope> permanentScopeStack = new ArrayList<>();
    public List<String> bannedVariables;
    SyntaxTree syntaxTree = new SyntaxTree();
    public HashMap<String, SymbolTable.SymbolInfo> symbolTable = new HashMap<>();

    public ScopeAnalyser(TreeNode root) {
        this.root = syntaxTree.root;
        bannedVariables = new ArrayList<>(Arrays.asList(
            "mul", "main", "grt", "div", "void", "print", "return",
            ",", ";", "=", "(", ")", "{", "}", "num", "not",
            "text", "then", "begin", "end", "else", "eq", "sqrt",
            "sub", "skip", "halt", "if", "or", "and", "add", "<input>"));
    }

    public void run() {
        Scope globalScope = new Scope(null);
        scopeStack.push(globalScope);
        permanentScopeStack.add(globalScope); 

        traverseTree(root);

        createVTable();
        printVTable();

        syntaxTree.printTree();

        System.out.println("\nScope Analysis Completed");
        System.out.println("Symbol Table saved to file SymbolTable.txt\n");
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
                TreeNode ALGO = node.children.get(2);
                TreeNode MAINFUNCTIONS = node.children.get(3);

                TreeNode DECL = MAINFUNCTIONS.children.get(0);
                TreeNode MOREFUNCTIONS = MAINFUNCTIONS.children.get(1);

                traverseTree(DECL);
                traverseTree(ALGO);
                traverseTree(MOREFUNCTIONS);

                break;
            }

            case "BODY": {
                traverseTree(node.children.get(4));
                traverseTree(node.children.get(1));
                traverseTree(node.children.get(2));
                break;
            }

            case "DECL": {
                // Handle function declaration
                TreeNode FTYP = node.children.get(0).children.get(0);
                TreeNode FNAME = node.children.get(0).children.get(1);

                String type = FTYP.children.get(0).symbol;
                String name = FNAME.children.get(0).symbol;
                String ID = FNAME.children.get(0).id;

                scopeStack.peek().symbolTable.addSymbol(name, type, ID);

                Scope functionScope = new Scope(scopeStack.peek());
                scopeStack.push(functionScope);
                permanentScopeStack.add(functionScope);

                handleHeader(node.children.get(0));
                traverseTree(node.children.get(1));

                scopeStack.pop();
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
        for (Scope scope : scopeStack) {
            printSymbolTable(scope.symbolTable);
        }
    }
    
    public void printSymbolTable(SymbolTable symbolTable) {
        for (Map.Entry<String, SymbolTable.SymbolInfo> entry : symbolTable.table.entrySet()) {
            String symbol = entry.getKey();
            SymbolTable.SymbolInfo info = entry.getValue();
            System.out.println("[ID " + info.id + " | Symbol: " + symbol +" | Old Name "+ info.oldName +" | Type: " + info.type + " | Unique Name: " + info.newName+"]");
        }
        System.out.println();
    }

    public void createVTable() {
        for (Scope scope : permanentScopeStack) {
            for (Map.Entry<String, SymbolTable.SymbolInfo> entry : scope.symbolTable.table.entrySet()) {
                SymbolTable.SymbolInfo info = entry.getValue();
                symbolTable.put(info.newName, info);
            }
        }
    }

    public void printVTable() {
        try {
            FileWriter myWriter = new FileWriter("SymbolTable.txt");
            myWriter.write("Symbol Table\n");
            myWriter.write("-------------------------------------------------------------\n");
            myWriter.write(String.format("%-15s %-15s %-15s%n", "Key", "Name", "Type"));
            myWriter.write("-------------------------------------------------------------\n");
            for (Map.Entry<String, SymbolTable.SymbolInfo> entry : symbolTable.entrySet()) {
                SymbolTable.SymbolInfo info = entry.getValue();
                myWriter.write(String.format("%-15s %-15s %-15s%n", info.newName, info.oldName, info.type));
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        System.out.println("Symbol Table\n");
        System.out.println("-------------------------------------------------------------");
        System.out.println(String.format("%-15s %-15s %-15s", "Key", "Name", "Type"));
        System.out.println("-------------------------------------------------------------");
        for (Map.Entry<String, SymbolTable.SymbolInfo> entry : symbolTable.entrySet()) {
            SymbolTable.SymbolInfo info = entry.getValue();
            System.out.println(String.format("%-15s %-15s %-15s", info.newName, info.oldName, info.type));
        }
    }

    public void handleGlobVars(TreeNode node) {
        if (node.children.size() == 0) {
            return;
        }
    
        TreeNode VTYP = node.children.get(0);
        TreeNode VNAME = node.children.get(1);
    
        String type = VTYP.children.get(0).symbol;
        String name = VNAME.children.get(0).symbol;
        String ID = VNAME.children.get(0).id;
    
        String newName = scopeStack.peek().symbolTable.addSymbol(name, type, ID);
        VNAME.children.get(0).symbol = newName;
    
        if (node.children.size() > 3) {
            handleGlobVars(node.children.get(3));
        }
    }
    
    

    public void handleAtomic(TreeNode node) {
        TreeNode child = node.children.get(0);
        if (child.symbol.equals("CONST")) return;
    
        if (child.symbol.equals("VNAME")) {
            String name = child.children.get(0).symbol;
            try {
                String newName = scopeStack.peek().lookup(name);
                child.children.get(0).symbol = newName;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }
    }
    
    public void handleAssign(TreeNode node) {
        TreeNode child = node.children.get(0);
        String name = child.children.get(0).symbol;
    
        try {
            String newName = scopeStack.peek().lookup(name);
            child.children.get(0).symbol = newName;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    
        traverseTree(node.children.get(2));
    }
    

    public void handleHeader(TreeNode node) {
        TreeNode VNAME1 = node.children.get(3);
        String name1 = VNAME1.children.get(0).symbol;
        String ID1 = VNAME1.children.get(0).id;
    
        String newName1 = scopeStack.peek().symbolTable.addSymbol(name1, "num", ID1);
        VNAME1.children.get(0).symbol = newName1;
    
        TreeNode VNAME2 = node.children.get(5);
        String name2 = VNAME2.children.get(0).symbol;
        String ID2 = VNAME2.children.get(0).id;
    
        String newName2 = scopeStack.peek().symbolTable.addSymbol(name2, "num", ID2);
        VNAME2.children.get(0).symbol = newName2; 
    
        TreeNode VNAME3 = node.children.get(7);
        String name3 = VNAME3.children.get(0).symbol;
        String ID3 = VNAME3.children.get(0).id;
    
        String newName3 = scopeStack.peek().symbolTable.addSymbol(name3, "num", ID3);
        VNAME3.children.get(0).symbol = newName3; 
    }
    
    
    public void handleLocVars(TreeNode node) {
        TreeNode VTYP1 = node.children.get(0);
        TreeNode VNAME1 = node.children.get(1);
        String type1 = VTYP1.children.get(0).symbol;
        String name1 = VNAME1.children.get(0).symbol;
        String ID1 = VNAME1.children.get(0).id;
    
        String newName1 = scopeStack.peek().symbolTable.addSymbol(name1, type1, ID1);
        VNAME1.children.get(0).symbol = newName1;
    
        TreeNode VTYP2 = node.children.get(3);
        TreeNode VNAME2 = node.children.get(4);
        String type2 = VTYP2.children.get(0).symbol;
        String name2 = VNAME2.children.get(0).symbol;
        String ID2 = VNAME2.children.get(0).id;
    
        String newName2 = scopeStack.peek().symbolTable.addSymbol(name2, type2, ID2);
        VNAME2.children.get(0).symbol = newName2;
    
        TreeNode VTYP3 = node.children.get(6);
        TreeNode VNAME3 = node.children.get(7);
        String type3 = VTYP3.children.get(0).symbol;
        String name3 = VNAME3.children.get(0).symbol;
        String ID3 = VNAME3.children.get(0).id;
    
        String newName3 = scopeStack.peek().symbolTable.addSymbol(name3, type3, ID3);
        VNAME3.children.get(0).symbol = newName3;
    }
    

    public void handleCall(TreeNode node) {
        TreeNode FNAME = node.children.get(0);
        String name = FNAME.children.get(0).symbol;

        try {
            String newName = scopeStack.peek().lookup(name);
            FNAME.children.get(0).symbol = newName; 
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
        }
        
    }

    public class SymbolTable {
        public HashMap<String, SymbolInfo> table;
        
        public SymbolTable() {
            table = new HashMap<>();
        }

        public String addSymbol(String name, String type, String id) {
            if (table.containsKey(name)) {
                System.out.println("Error: Double declaration of variable " + name + " in the same scope");
                System.exit(1);
            }
        
            // Banned variables check
            if (bannedVariables.contains(name.substring(2))) {
                System.out.println("Error: Variable name " + name + " is reserved");
                System.exit(1);
            }
        
            // Add the symbol to the current scope
            SymbolInfo symbolInfo = new SymbolInfo(name, type, id);
            table.put(name, symbolInfo);
            return symbolInfo.newName;
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


// PROG -> main GLOBVARS ALGO FUNCTIONS
// GLOBVARS -> ''
// GLOBVARS -> VTYP VNAME , GLOBVARS
// VTYP -> num
// VTYP -> text
// VNAME -> V
// ALGO -> begin INSTRUC end
// INSTRUC -> ''
// INSTRUC -> COMMAND ; INSTRUC
// COMMAND -> skip
// COMMAND -> halt
// COMMAND -> print ATOMIC
// COMMAND -> ASSIGN
// COMMAND -> CALL
// COMMAND -> BRANCH
// ATOMIC -> VNAME
// ATOMIC -> CONST
// CONST -> N
// CONST -> T
// ASSIGN -> VNAME < input
// ASSIGN -> VNAME = TERM
// CALL -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
// BRANCH -> if COND then ALGO else ALGO
// TERM -> ATOMIC
// TERM -> CALL
// TERM -> OP
// OP -> UNOP ( ARG )
// OP -> BINOP ( ARG , ARG )
// ARG -> ATOMIC
// ARG -> OP
// COND -> SIMPLE
// COND -> COMPOSIT
// SIMPLE -> BINOP ( ATOMIC , ATOMIC )
// COMPOSIT -> BINOP ( SIMPLE , SIMPLE )
// COMPOSIT -> UNOP ( SIMPLE )
// UNOP -> not
// UNOP -> sqrt
// BINOP -> or
// BINOP -> and
// BINOP -> eq
// BINOP -> grt
// BINOP -> add
// BINOP -> sub
// BINOP -> mul
// BINOP -> div
// FNAME -> F
// FUNCTIONS -> ''
// FUNCTIONS -> DECL FUNCTIONS
// DECL -> HEADER BODY
// HEADER -> FTYP FNAME ( VNAME , VNAME , VNAME )
// FTYP -> num
// FTYP -> void
// BODY -> PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
// PROLOG -> {
// EPILOG -> }
// LOCVARS -> VTYP VNAME , VTYP VNAME , VTYP VNAME ,
// SUBFUNCS -> FUNCTIONS
// COMMAND -> return ATOMIC