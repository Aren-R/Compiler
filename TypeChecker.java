import java.util.HashMap;

public class TypeChecker {
    private TreeNode root;
    public HashMap<String, ScopeAnalyser.SymbolTable.SymbolInfo> symbolTable = new HashMap<>();
    public String RED = "\u001B[31m";
    public String RESET = "\u001B[0m";
    public String GREEN = "\u001B[32m";

    public TypeChecker(TreeNode root, HashMap<String, ScopeAnalyser.SymbolTable.SymbolInfo> symbolTable) {
        this.root = root;
        this.symbolTable = symbolTable;
    }

    public void run() {
        System.out.println("Type Checking Started");
        // System.out.println("Type Checking Completed");
        boolean result = typecheck(root);
        if (result) {
            System.out.println(GREEN + "Type Checking successful" + RESET);
        } else {
            System.out.println(RED + "Program has Type Errors" + RESET);
        }
    }

    public boolean typecheck(TreeNode node) {
        // System.out.println(node.symbol);
        switch (node.symbol) {
            case "PROG": {
                return typecheck(node.children.get(1)) && typecheck(node.children.get(2)) && typecheck(node.children.get(3));
            }

            case "GLOBVARS": {
                return handleGlobVars(node);
            }

            case "ALGO": {
                return typecheck(node.children.get(1));
            }

            case "INSTRUC": {
                if (node.children.size() == 0) {
                    return true;
                }
                return typecheck(node.children.get(0)) && typecheck(node.children.get(2));
            }

            case "COMMAND": {
                if (node.children.get(0).symbol.equals("skip") || node.children.get(0).symbol.equals("halt")) {
                    return true;
                }

                if (node.children.get(0).symbol.equals("print")) {
                    if (typeOf(node.children.get(1)).equals("num") || typeOf(node.children.get(1)).equals("text")) {
                        return true;
                    } else {
                        System.out.println(RED + "Type Error: print statement type mismatch" + RESET);
                        return false;
                    }
                }

                if (node.children.get(0).symbol.equals("return")) {
                    String FTYPofFunc = handleReturn(node);
                    // System.out.println(FTYPofFunc);
                    if (FTYPofFunc.equals("void")) {
                        System.err.println(RED + "Error: \"return\" in void function" + RESET);
                        System.err.println();
                        return false;
                    }
                    if (FTYPofFunc.equals(typeOf(node.children.get(1))) && typeOf(node.children.get(1)).equals("num")) {
                        return true;
                    } else {
                        System.out.println(RED + "Error: \"return\" statement type mismatch" + RESET);
                        return false;
                    }
                }

                if (node.children.get(0).symbol.equals("ASSIGN")) {
                    return typecheck(node.children.get(0));
                }

                if (node.children.get(0).symbol.equals("CALL")) {
                    if (typeOf(node.children.get(0)).equals("void")) {
                        return true;
                    } else {
                        return false;
                    }
                }

                if (node.children.get(0).symbol.equals("BRANCH")) {
                    return typecheck(node.children.get(0));
                }
            }

            case "ASSIGN": {
                if (node.children.get(1).symbol.equals("=")) {
                    String VNAMEtype = typeOf(node.children.get(0));
                    String TERMtype = typeOf(node.children.get(2));
                    if (VNAMEtype.equals(TERMtype)) {
                        return true;
                    } else {
                        System.out.println(RED + "Type Error: assignment type mismatch" + RESET);
                        System.out.println(RED + "\""+ lookup(node.children.get(0).children.get(0).symbol) + "\" is not of type " + "\"" + TERMtype + "\"" + RESET);    
                        return false;
                    }
                }
                if (typeOf(node.children.get(0)).equals("num")) {
                    return true;
                }
                return false;
            }

            case "BRANCH": {
                if (typeOf(node.children.get(1)).equals("bool")) {
                    return typecheck(node.children.get(3)) && typecheck(node.children.get(5));
                } else {
                    System.out.println(RED + "Type Error: condition in branch statement is not a boolean" + RESET);
                    System.out.println(RED + "At: \"if" + node.children.get(1).children.get(0).symbol + RESET);
                    return false;
                }
            }

            case "FUNCTIONS": {
                if (node.children.size() == 0) {
                    return true;
                }
                return typecheck(node.children.get(0)) && typecheck(node.children.get(1));
            }

            case "DECL": {
                return typecheck(node.children.get(0)) && typecheck(node.children.get(1));
            }

            case "HEADER": {
                String t3 = typeOf(node.children.get(3));
                String t5 = typeOf(node.children.get(5));
                String t7 = typeOf(node.children.get(7));

                if (t3.equals(t5) && t5.equals(t7) && t3.equals("num")) {
                    return true;
                } else {
                    System.out.println(RED + "Type Error: function arguments type mismatch" + RESET);
                    return false;
                }   
            }

            case "BODY": {
                return typecheck(node.children.get(2)) && typecheck(node.children.get(4));
            }

            case "SUBFUNCS": {
                return typecheck(node.children.get(0));
            }
        
            default: {
                break;
            }
        }

        return false;
    }

    public String handleReturn(TreeNode node) {
        //go to parent until you find symbol of HEADER
        TreeNode parent = node.parent;
        while (true) {
            if (parent.symbol.equals("DECL")) {
                TreeNode header = parent.children.get(0);
                return typeOf(header.children.get(0));
            }
            if (parent.symbol.equals("PROG")) {
                return "void";
            }
            parent = parent.parent;
        }
    }

    public boolean handleGlobVars(TreeNode node) {
        if (node.children.size() == 0) {
            return true;
        }

        TreeNode VTYP = node.children.get(0);
        TreeNode VNAME = node.children.get(1);
        TreeNode GLOBVARS = node.children.get(3);

        String VTYPtype = typeOf(VTYP);
        String VNAMEtype = typeOf(VNAME);
        return VTYPtype.equals(VNAMEtype) && typecheck(GLOBVARS);
    }

    public String typeOf(TreeNode node) {
        switch (node.symbol) {
            case "VTYP": {
                try {
                    String type = node.children.get(0).symbol;
                    return type;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            }

            case "VNAME": {
                try {
                    String type = symbolTable.get(node.children.get(0).symbol).type;
                    return type;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            }

            case "ATOMIC": {
                return typeOf(node.children.get(0));
            }

            case "CONST": {
                if (node.children.get(0).tokenClass.equals("N")) {
                    return "num";
                } else {
                    return "text";
                }
            }

            case "TERM": {
                return typeOf(node.children.get(0));
            }

            case "CALL": {
                String t1 = typeOf(node.children.get(2));
                String t2 = typeOf(node.children.get(4));
                String t3 = typeOf(node.children.get(6));
                if (t1.equals("num") && t2.equals("num") && t3.equals("num")) {
                    return typeOf(node.children.get(0));
                } else {
                    if (!t1.equals("num")) parameterError(node.children.get(2));
                    if (!t2.equals("num")) parameterError(node.children.get(4));
                    if (!t3.equals("num")) parameterError(node.children.get(6));
                    return "undefined";
                }
            }

            case "OP": {
                if (node.children.get(0).symbol.equals("UNOP")) {
                    String t1 = typeOf(node.children.get(0));
                    String t2 = typeOf(node.children.get(2));
                    if (t1.equals(t2) && t1.equals("num")) {
                        return "num";
                    } else {
                        if (t1.equals(t2) && t1.equals("bool")) {
                            return "bool";
                        } else {
                            return "undefined";
                        }
                    }
                }

                if (node.children.get(0).symbol.equals("BINOP")) {
                    String t0 = typeOf(node.children.get(0));
                    String t1 = typeOf(node.children.get(2));
                    String t2 = typeOf(node.children.get(4));
                    
                    if (t0.equals("comparison") && t1.equals("num") && t2.equals("num")) {
                        return "bool";
                    } else if (t0.equals("comparison")) {
                        System.out.println(RED + "Type Error: Expected 'num' for both operands, got '" + t1 + "' and '" + t2 + "'" + RESET);
                        System.exit(1);
                    }
                    
                    if (t0.equals("bool") && t1.equals("bool") && t2.equals("bool")) {
                        return "bool";
                    } else if (t0.equals("bool")) {
                        System.out.println(RED + "Type Error: Expected 'bool' for both operands, got '" + t1 + "' and '" + t2 + "'" + RESET);
                        System.exit(1);
                    }
                    
                    if (t0.equals("num") && t1.equals("num") && t2.equals("num")) {
                        return "num";
                    } else if (t0.equals("num")) {
                        System.out.println(RED + "Type Error: Expected 'num' for both operands, got '" + t1 + "' and '" + t2 + "'" + RESET);
                        System.exit(1);
                    }
                
                    return "undefined";
                }
                
            }

            case "ARG": {
                return typeOf(node.children.get(0));
            }

            case "UNOP": {
                if (node.children.get(0).symbol.equals("not")) {
                    return "bool";
                }
                if (node.children.get(0).symbol.equals("sqrt")) {
                    return "num";
                }
            }

            case "BINOP": {
                if (node.children.get(0).symbol.equals("or") || node.children.get(0).symbol.equals("and")) {
                    return "bool";
                }
                if (node.children.get(0).symbol.equals("eq") || node.children.get(0).symbol.equals("grt")) {
                    return "comparison";
                }
                if (node.children.get(0).symbol.equals("add") || node.children.get(0).symbol.equals("sub") || node.children.get(0).symbol.equals("mul") || node.children.get(0).symbol.equals("div")) {
                    return "num";
                }
            }

            case "COND": {
                return typeOf(node.children.get(0));
            }

            case "SIMPLE": {
                if (typeOf(node.children.get(0)).equals("bool") && typeOf(node.children.get(2)).equals("bool") && typeOf(node.children.get(4)).equals("bool")) {
                    return "bool";
                }
                if (typeOf(node.children.get(0)).equals("comparison") && typeOf(node.children.get(2)).equals("num") && typeOf(node.children.get(4)).equals("num")) {
                    return "bool";
                }
                return "undefined";
            }

            case "COMPOSIT": {
                if (node.children.get(0).symbol.equals("BINOP")) {
                    if (typeOf(node.children.get(0)).equals("bool") && typeOf(node.children.get(2)).equals("bool") && typeOf(node.children.get(4)).equals("bool")) {
                        return "bool";
                    } else {
                        return "undefined";
                    }
                }
                if (node.children.get(0).symbol.equals("UNOP")) {
                    if (typeOf(node.children.get(0)).equals("bool") && typeOf(node.children.get(2)).equals("bool")) {
                        return "bool";
                    } else {
                        System.out.println(RED + "Type Error: Expected 'bool' for both operands, got '" + typeOf(node.children.get(0)) + "' and '" + typeOf(node.children.get(2)) + "'" + RESET);
                        System.exit(1);
                        return "undefined";
                    }
                }
            }

            case "FNAME": {
                try {
                    String type = symbolTable.get(node.children.get(0).symbol).type;
                    return type;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            }

            case "FTYP": {
                try {
                    String type = node.children.get(0).symbol;
                    return type;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            }


            default: {
                return "undefined";
            }
        }
    }

    public String lookup(String symbol) {
        return symbolTable.get(symbol).oldName;
    }

    public void parameterError(TreeNode node) {
        String symbol;
        TreeNode VNAME = node.children.get(0);
        TreeNode V = VNAME.children.get(0);
        if (V.tokenClass.equals("T") || V.tokenClass.equals("N")) {
            symbol = V.symbol;
        } else {
            symbol = lookup(V.symbol);
        }
        System.out.println(RED + "Type Error: function argument " + symbol + " is not of type num" + RESET);
        System.exit(1);
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