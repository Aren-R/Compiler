import java.util.HashMap;

public class TypeChecker {
    private TreeNode root;
    public HashMap<String, ScopeAnalyser.SymbolTable> symbolTables = new HashMap<>();

    public TypeChecker(TreeNode root) {
        this.root = root;
    }

    public boolean run() {
        return typecheck(root);
    }

    public boolean typecheck(TreeNode node) {
        switch (node.symbol) {
            case "PROG": {
                return typecheck(node.children.get(1)) && typecheck(node.children.get(2)) && typecheck(node.children.get(3));
            }

            case "GLOBVARS": {
                return handleGlobVars(node);
            }
        
            default: {
                break;
            }
        }

        return false;
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
                    String type = scopeStack.peek().lookupType(node.children.get(0).symbol);
                    return type;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            }

            default: {
                return "type not found";
            }
        }
    }
}