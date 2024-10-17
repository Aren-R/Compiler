import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

class SymbolInfo {
    String type;  // Type of symbol, e.g., 'variable', 'function'
    int nodeId;   // Unique ID from the XML tree

    public SymbolInfo(String type, int nodeId) {
        this.type = type;
        this.nodeId = nodeId;
    }
}

class Scope {
    public Map<String, SymbolInfo> variableTable;  // Symbol table for variables
    public Map<String, SymbolInfo> functionTable;  // Symbol table for functions
    public Scope parentScope;  // Reference to the parent scope

    public Scope(Scope parentScope) {
        this.variableTable = new HashMap<>();
        this.functionTable = new HashMap<>();
        this.parentScope = parentScope;
    }

    // Add a variable to the current scope
    public boolean addVariable(String name, SymbolInfo info) {
        if (variableTable.containsKey(name)) {
            return false;  // Duplicate declaration
        }
        variableTable.put(name, info);
        return true;
    }

    // Add a function to the current scope
    public boolean addFunction(String name, SymbolInfo info) {
        if (functionTable.containsKey(name)) {
            return false;  // Duplicate declaration
        }
        functionTable.put(name, info);
        return true;
    }

    // Lookup a variable in the current scope or parent scopes
    public SymbolInfo lookupVariable(String name) {
        SymbolInfo info = variableTable.get(name);
        if (info != null) {
            return info;
        } else if (parentScope != null) {
            return parentScope.lookupVariable(name);  // Lookup in the parent scope
        }
        return null;  // Variable not found
    }

    // Lookup a function in the current scope or parent scopes
    public SymbolInfo lookupFunction(String name) {
        SymbolInfo info = functionTable.get(name);
        if (info != null) {
            return info;
        } else if (parentScope != null) {
            return parentScope.lookupFunction(name);  // Lookup in the parent scope
        }
        return null;  // Function not found
    }
}

public class ScopeAnalyser {
    private Node root;
    private NodeList innerNodes;
    private NodeList leafNodes;
    private Scope globalScope;  // Global scope for the 'main' program
    private Stack<Scope> scopeStack;  // Tracks current scope

    public ScopeAnalyser() {
        scopeStack = new Stack<>();
        globalScope = new Scope(null);  // Create the global scope
        scopeStack.push(globalScope);  // Start in the global scope
        getNodes("resources/SyntaxTree.xml");
        traverseTree(root);
    }

    // Traversing the syntax tree and handling scope
    public void traverseTree(Node currentNode) {
        // System.out.println("=== IN NEW NODE ===");
        // System.out.println("Node: " + currentNode.getNodeName());
    
        // Check if the current node is a leaf node
        if (currentNode.getNodeName().equals("LEAF")) {
            handleLeafNode(currentNode);
            return;
        }
    
        Element symbElement = (Element) ((Element) currentNode).getElementsByTagName("SYMB").item(0);
        Element childrenElement = (Element) ((Element) currentNode).getElementsByTagName("CHILDREN").item(0);
    
        if (symbElement != null) {
            String symb = symbElement.getTextContent();
            // System.out.println("Symbol: " + symb);
            handleSymbol(symb, currentNode);
        }
    
        if (childrenElement == null) {
            // System.out.println("No children element found.");
            return;
        }
    
        NodeList children = childrenElement.getChildNodes();
        if (children.getLength() == 0) {
            // System.out.println("No children of node");
            return;
        }
    
        // Traverse children
        List<String> childrenIDs = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                childrenIDs.add(childElement.getTextContent());
                // System.out.println("Child ID: " + childElement.getTextContent());
            }
        }
    
        for (String childID : childrenIDs) {
            Element child = (Element) getNodeByUNID(childID);
            if (child != null) {
                // System.out.println("Traversing Child: " + child.getElementsByTagName("UNID").item(0).getTextContent());
                traverseTree(child);
            }
        }
    }

    private void handleSymbol(String symb, Node currentNode) {
        // Handle variable and function declarations
        if (symb.equals("VNAME")) {
            handleVariableDeclaration(currentNode);
        } else if (symb.equals("FNAME")) {
            handleFunctionDeclaration(currentNode);
        } else if (symb.equals("CALL")) {
            handleFunctionCall(currentNode);
        }
    }

    private void handleVariableDeclaration(Node currentNode) {
        // Extract the variable name as usual
        String varName = extractNodeTextContent(currentNode);
    
        // Get the unique UNID for this node (the leaf node's UNID, not the parent)
        int nodeId = Integer.parseInt(((Element) currentNode).getElementsByTagName("UNID").item(0).getTextContent());
    
        // Get the variable type from the preceding sibling (VTYP)
        String varType = extractTypeFromPrecedingVTYP(currentNode);
    
        Scope currentScope = scopeStack.peek();
    
        SymbolInfo varInfo = new SymbolInfo(varType, nodeId);
        if (!currentScope.addVariable(varName, varInfo)) {
            throw new RuntimeException("Duplicate variable declaration: " + varName);
        }
    
        System.out.println("Variable declared: " + varName + " (Type: " + varType + ", ID: " + nodeId + ")");
    }

    private String extractTypeFromPrecedingVTYP(Node currentNode) {
        // Assuming currentNode is a VNAME node, traverse to its previous sibling
        Node previousSibling = currentNode.getPreviousSibling();
        
        // Traverse backward to find the preceding VTYP element
        while (previousSibling != null && !"VTYP".equals(previousSibling.getNodeName())) {
            previousSibling = previousSibling.getPreviousSibling();
        }
    
        if (previousSibling != null) {
            // Now extract the word from the preceding VTYP element
            Element vtypElement = (Element) previousSibling;
            Node wordNode = vtypElement.getElementsByTagName("WORD").item(0);
            if (wordNode != null) {
                return wordNode.getTextContent();  // Return the type, e.g., 'num', 'str', etc.
            }
        }
    
        return "unknown";  // Fallback if VTYP not found
    }
    
    

    private void handleFunctionDeclaration(Node currentNode) {
        String funcName = extractNodeTextContent(currentNode);
        int nodeId = Integer.parseInt(((Element) currentNode).getElementsByTagName("UNID").item(0).getTextContent());
        Scope currentScope = scopeStack.peek();

        // Create new scope for the function
        Scope funcScope = new Scope(currentScope);
        scopeStack.push(funcScope);

        SymbolInfo funcInfo = new SymbolInfo("function", nodeId);
        if (!currentScope.addFunction(funcName, funcInfo)) {
            throw new RuntimeException("Duplicate function declaration: " + funcName);
        }

        System.out.println("Function declared: " + funcName + " (ID: " + nodeId + ")");
    }

    private void handleFunctionCall(Node currentNode) {
        String funcName = extractNodeTextContent(currentNode);
        Scope currentScope = scopeStack.peek();

        SymbolInfo funcInfo = currentScope.lookupFunction(funcName);
        if (funcInfo == null) {
            throw new RuntimeException("Function not found: " + funcName);
        }

        System.out.println("Function call: " + funcName);
    }

    private void handleLeafNode(Node currentNode) {
        // Handle terminals and tokens (e.g., variable usage)
        String leafContent = currentNode.getTextContent();
        // System.out.println("Leaf Node Content: " + leafContent);
    }

    private String extractNodeTextContent(Node node) {

        Node childElement = ((Element) node).getElementsByTagName("CHILDREN").item(0);

        String childID = ((Element) childElement).getElementsByTagName("ID").item(0).getTextContent();

        Element child = (Element) getNodeByUNID(childID);
    
        Node wordNode = child.getElementsByTagName("WORD").item(0);
    
        if (wordNode != null) {
            return wordNode.getTextContent();
        } else {
            System.out.println("Warning: WORD node not found in child with ID " + childID);
            return "";
        }
    }
    

    public Node getNodeByUNID(String unid) {
        for (int i = 0; i < innerNodes.getLength(); i++) {
            Element node = (Element) innerNodes.item(i);
            if (unid.equals(node.getElementsByTagName("UNID").item(0).getTextContent())) {
                return node;
            }
        }

        for (int i = 0; i < leafNodes.getLength(); i++) {
            Element node = (Element) leafNodes.item(i);
            if (unid.equals(node.getElementsByTagName("UNID").item(0).getTextContent())) {
                return node;
            }
        }

        return null;  // Return null if no matching node is found
    }

    // Method to print the variable and function tables for each scope
    public void printSymbolTables() {
        Scope currentScope = scopeStack.peek();  // Get the current scope at the top of the stack
        System.out.println("======= Symbol Tables =======");

        int scopeLevel = 0;
        while (currentScope != null) {
            System.out.println("Scope Level: " + scopeLevel);

            // Print variable table
            System.out.println("--- Variables ---");
            for (Map.Entry<String, SymbolInfo> entry : currentScope.variableTable.entrySet()) {
                String varName = entry.getKey();
                SymbolInfo info = entry.getValue();
                System.out.println("Variable: " + varName + ", Type: " + info.type + ", Node ID: " + info.nodeId);
            }

            // Print function table
            System.out.println("--- Functions ---");
            for (Map.Entry<String, SymbolInfo> entry : currentScope.functionTable.entrySet()) {
                String funcName = entry.getKey();
                SymbolInfo info = entry.getValue();
                System.out.println("Function: " + funcName + ", Type: " + info.type + ", Node ID: " + info.nodeId);
            }

            // Move to parent scope
            currentScope = currentScope.parentScope;
            scopeLevel++;
        }

        System.out.println("==========================");
    }


    public void getNodes(String filePath) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc = null;

        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        root = doc.getDocumentElement().getElementsByTagName("ROOT").item(0);
        innerNodes = doc.getDocumentElement().getElementsByTagName("IN");
        leafNodes = doc.getDocumentElement().getElementsByTagName("LEAF");
    }
}
