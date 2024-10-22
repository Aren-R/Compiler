import java.util.ArrayList;
import java.util.HashMap;

public class IntermediateCodeGenerator {
    public TreeNode root;
    public HashMap<String, ScopeAnalyser.SymbolTable.SymbolInfo> symbolTable = new HashMap<>();
    public int labelCounter = 0;
    public int variableCounter = 0;
    
    public IntermediateCodeGenerator(TreeNode root, HashMap<String, ScopeAnalyser.SymbolTable.SymbolInfo> symbolTable) {
        this.root = root;
        this.symbolTable = symbolTable;
    }

    public void run() {
        System.out.println("Running Intermediate Code Generator...");
        System.out.println(translate(root));
    }

    public String translate(TreeNode node) {
        System.out.println("Translating: " + node.symbol);
        switch (node.symbol) {
            case "GLOBVARS": {
                return "";
            }

            case "VTYP": {
                return "";
            }

            case "VNAME": {
                return node.children.get(0).symbol;
            }

            case "FNAME": {
                return node.children.get(0).children.get(0).symbol;
            }

            case "PROG": {
                String aCode = translate(node.children.get(2));
                String fCode = translate(node.children.get(3));
                return aCode + " STOP " + fCode;
            }

            case "ALGO": {
                return translate(node.children.get(1));
            }

            case "INSTRUC": {
                if (node.children.size() == 0) {
                    return " REM END ";
                }
                String code1 = translate(node.children.get(0));
                String code2 = translate(node.children.get(2));
                return code1 + code2;
            }

            case "COMMAND": {
                if (node.children.get(0).symbol.equals("skip")) {
                    return " REM DO NOTHING ";
                } else if (node.children.get(0).symbol.equals("halt")) {
                    return " STOP ";
                } else if (node.children.get(0).symbol.equals("print")) {
                    return "PRINT " + translate(node.children.get(1));
                } else if (node.children.get(0).symbol.equals("ASSIGN")) {
                    return translate(node.children.get(0));
                } else if (node.children.get(0).symbol.equals("CALL")) {
                    return translate(node.children.get(0));
                } else if (node.children.get(0).symbol.equals("BRANCH")) {
                    return translate(node.children.get(0));
                } else if (node.children.get(0).symbol.equals("return")) {
                    return " PHASE 5b NOT DONE ";
                }
            }

            case "ATOMIC": {
                if (node.children.get(0).symbol.equals("VNAME")) {
                    return translate(node.children.get(0));
                } else if (node.children.get(0).symbol.equals("CONST")) {
                    return translate(node.children.get(0));
                }
            }


            case "ASSIGN": {
                if (node.children.get(1).symbol.equals("<")) {
                    return "INPUT " + translate(node.children.get(0));
                } else if (node.children.get(1).symbol.equals("=")) {
                    String place = newVar();
                    String x = translate(node.children.get(0));
                    String TERM = translate(node.children.get(2), place);
                    return TERM + "\n" + x + " := " + place + "\n";
                }
            }

            case "CALL": {
                String FNAME = translate(node.children.get(0));
                String p1 = translate(node.children.get(2));
                String p2 = translate(node.children.get(4));
                String p3 = translate(node.children.get(6));
                return "CALL_ " + FNAME + "(" + p1 + "," + p2 + "," + p3 + ")";
            }

            case "UNOP": {
                if (node.children.get(0).symbol.equals("not")) {
                    return "!"; //fix
                } else if (node.children.get(0).symbol.equals("sqrt")) {
                    return "SQR";
                }
            }

            case "BINOP": {
                if (node.children.get(0).symbol.equals("or")) {
                    return " || ";
                } else if (node.children.get(0).symbol.equals("and")) {
                    return " && ";
                } else if (node.children.get(0).symbol.equals("eq")) {
                    return " = "; 
                } else if (node.children.get(0).symbol.equals("grt")) {
                    return " > ";
                } else if (node.children.get(0).symbol.equals("add")) {
                    return " + ";
                } else if (node.children.get(0).symbol.equals("sub")) {
                    return " - ";
                } else if (node.children.get(0).symbol.equals("mul")) {
                    return " * ";
                } else if (node.children.get(0).symbol.equals("div")) {
                    return " / ";
                }
            }

            case "BRANCH": {
                if (node.children.get(1).children.get(0).symbol.equals("SIMPLE")) {
                    String L1 = newLabel();
                    String L2 = newLabel();
                    String L3 = newLabel();
                    String code1 = translateCond(node.children.get(1), L1, L2);
                    String code2 = translate(node.children.get(3));
                    String code3 = translate(node.children.get(5));
                    return code1 + " LABEL " + L1 + "\n" + code2 + " GOTO " + L3 + "\n" + " LABEL " + L2 + "\n" + code3 + " LABEL " + L3 + "\n";
                }
                if (node.children.get(0).symbol.equals("COMPOSIT")) {
                    //
                }
                
            }

            default: {
                return "";
            }
        }
    }

    public String translate(TreeNode node, String place) {
        switch (node.symbol) {
            case "TERM": {
                return translate(node.children.get(0), place);
            }

            case "OP": {
                if (node.children.get(0).symbol.equals("UNOP")) {
                    String place1 = newVar();
                    String code1 = translate(node.children.get(2));
                    String op = translate(node.children.get(0));
                    return code1 + " " + place + " := " + op + " " + place1 + "\n";

                } else if (node.children.get(0).symbol.equals("BINOP")) {
                    String place1 = newVar();
                    String place2 = newVar();
                    String code1 = translate(node.children.get(2), place1);
                    String code2 = translate(node.children.get(4), place2);
                    String op = translate(node.children.get(0));
                    return code1 + code2 + " " + place + " := " + place1 + " " + op + " " + place2;
                }
            }



            case "ARG": {
                if (node.children.get(0).symbol.equals("ATOMIC")) {
                    return translate(node.children.get(0), place);
                } else if (node.children.get(0).symbol.equals("OP")) {
                    return translate(node.children.get(0), place);
                }
            }

            case "ATOMIC": {
                if (node.children.get(0).symbol.equals("VNAME")) {
                    return translate(node.children.get(0));
                } else if (node.children.get(0).symbol.equals("CONST")) {
                    return translate(node.children.get(0), place);
                }
            }

            case "CONST": {
                if (node.children.get(0).tokenClass.equals("N")) {
                    return "\n" + place + " := " +  node.children.get(0).symbol+ "\n";
                } else if (node.children.get(0).tokenClass.equals("T")) {
                    return "\n" + place + " := " + node.children.get(0).symbol + "\n";
                }
            }
        }
        return "";
    }

    public String translateCond(TreeNode node, String L1, String L2) {
        switch (node.children.get(0).symbol) {
            case "SIMPLE": {
                TreeNode SIMPLE = node.children.get(0);
                String op = translate(SIMPLE.children.get(0));
                String place1 = newVar();
                String place2 = newVar();
                String code1 = translate(SIMPLE.children.get(2), place1);
                String code2 = translate(SIMPLE.children.get(4), place2);
                return code1 + " " + code2 + "\nIF " + place1 + " " + op + " " + place2 + " THEN " + L1 + " ELSE " + L2 + "\n";
        

                // if (op.equals(" && ")) {
                //     String arg = newLabel();
                //     String code1 = translateCond(node.children.get(2), arg, L2);
                //     String code2 = translateCond(node.children.get(4), L1, L2);
                //     return code1 + " LABEL " + arg + "\n" + code2;
                // }

                // if (op.equals(" || ")) {
                //     String arg = newLabel();
                //     String code1 = translateCond(node.children.get(2), L1, arg);
                //     String code2 = translateCond(node.children.get(4), L1, L2);
                //     return code1 + " LABEL " + arg + "\n" + code2;
                // }

            }

            case "COMPOSIT": {
                // if ()
            }

            default : {
                return "";
            }
        }
    }

    public String newLabel() {
        return "L_" + labelCounter++;
    }

    public String newVar() {
        return "v_" + variableCounter++;
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