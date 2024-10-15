import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.StringReader;
import org.xml.sax.InputSource;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class Parser {

    private Map<Integer, Map<String, String>> terminalTable;
    private Map<Integer, Map<String, Integer>> nonTerminalTable;
    private Stack<TokenNode> stack;
    private Document tokenStream;

    // Variables for syntax tree construction
    private Document syntaxTree;          // XML Document to hold the syntax tree
    private Element rootElement;          // ROOT element in the syntax tree
    private Element innerNodesElement;    // INNERNODES element
    private Element leafNodesElement;     // LEAFNODES element
    private int uniqueIdCounter;          // Counter for assigning unique node IDs

    public Parser() {
        terminalTable = new HashMap<>();
        nonTerminalTable = new HashMap<>();
        stack = new Stack<TokenNode>();
        uniqueIdCounter = 1;  // Start IDs at 1

        initializeParsingTable();
        initializeSyntaxTree();
    }

    public void parse() {
        stack.push(new TokenNode(0, "na", "PROG"));

        NodeList tokens = tokenStream.getElementsByTagName("TOK");

        int i = 0;
        while (true) {
            Node token = tokens.item(i);
            Element tokenElement = (Element) token;

            String tokenWord = tokenElement.getElementsByTagName("WORD").item(0).getTextContent();
            String tokenClass = tokenElement.getElementsByTagName("CLASS").item(0).getTextContent();

            int state = stack.peek().state;

            boolean isVar = false;
            String tempWord = "";
            if (tokenClass.equals("V") || tokenClass.equals("F") || tokenClass.equals("N") || tokenClass.equals("T")) {
                isVar = true;
                tempWord = tokenClass;
            }

            String action = isVar ? terminalTable.get(state).get(tempWord) : terminalTable.get(state).get(tokenWord);

            if (action == null) {
                System.out.println("Syntax Error at token: " + tokenWord);
                break;
            }

            if (action.equals("acc")) {
                List<TokenNode> poppedNodes = new ArrayList<>();
                for (int j = 0; j < 4; j++) {
                    TokenNode poppedNode = stack.pop();
                    poppedNodes.add(poppedNode);
                }
                TokenNode root = stack.pop();
                
                for (TokenNode child : poppedNodes) {
                    child.parent = root;
                }

                // Get the <ROOT> node
                Element rootNode = (Element) rootElement.getElementsByTagName("ROOT").item(0);
                
                // Get the <CHILDREN> element under the <ROOT>
                Element rootChildren = (Element) rootNode.getElementsByTagName("CHILDREN").item(0);
                
                // Add each popped node ID as a child ID
                for (TokenNode child : poppedNodes) {
                    Element idElement = syntaxTree.createElement("ID");
                    idElement.appendChild(syntaxTree.createTextNode(child.id.toString()));
                    rootChildren.appendChild(idElement);
                }

                assignParents();
                System.out.println("Accepted");
                break;
            }
            
            if (action.startsWith("s")) {
                // SHIFT action
                int nextState = Integer.parseInt(action.substring(1));
                TokenNode newTokenNode = new TokenNode(nextState, tokenClass, tokenWord);
                stack.push(newTokenNode); // Push the new node onto the stack

                // Add this token as a leaf node in the syntax tree
                addLeafNode(token, newTokenNode);
                i += 1;
            }

            if (action.startsWith("r")) {
                // REDUCE action
                int ruleNumber = Integer.parseInt(action.substring(1));
                applyReduction(ruleNumber);
            }
        }
    }

    private void assignParents() {
        // Get the inner nodes
        NodeList innerNodes = innerNodesElement.getElementsByTagName("IN");
        
        // Get the root and its children
        NodeList root = rootElement.getElementsByTagName("ROOT");
        if (root.getLength() == 0) {
            return; // No root found, exit early
        }
        
        // Get children of the root node
        Element rootNode = (Element) root.item(0);
        NodeList rootChildren = rootNode.getElementsByTagName("CHILDREN").item(0).getChildNodes();
        
        // Assign parents to root's children
        for (int j = 0; j < rootChildren.getLength(); j++) {
            Element childIDElement = (Element) rootChildren.item(j);
            Integer childID = Integer.parseInt(childIDElement.getTextContent());
    
            // Find the child node by its UNID
            Element childNode = findNodeById(childID);
            if (childNode != null) {
                // Create a PARENT element for the child node
                Element parentElement = syntaxTree.createElement("PARENT");
                // Set the parent's UNID (the root's UNID)
                parentElement.appendChild(syntaxTree.createTextNode(rootNode.getElementsByTagName("UNID").item(0).getTextContent()));
                
                // Insert the PARENT element before the UNID element
                Element unidElement = (Element) childNode.getElementsByTagName("UNID").item(0);
                childNode.insertBefore(parentElement, unidElement);
            }
        }
    
        // Now assign parents for inner nodes
        for (int i = 0; i < innerNodes.getLength(); i++) {
            Element innerNode = (Element) innerNodes.item(i);
            NodeList children = innerNode.getElementsByTagName("CHILDREN").item(0).getChildNodes();
            
            for (int j = 0; j < children.getLength(); j++) {
                Element childIDElement = (Element) children.item(j);
                Integer childID = Integer.parseInt(childIDElement.getTextContent());
    
                // Find the child node by its UNID
                Element childNode = findNodeById(childID);
                if (childNode != null) {
                    Element parentElement = syntaxTree.createElement("PARENT");
                    parentElement.appendChild(syntaxTree.createTextNode(innerNode.getElementsByTagName("UNID").item(0).getTextContent()));
                    
                    // Insert the PARENT element before the UNID element
                    Element unidElement = (Element) childNode.getElementsByTagName("UNID").item(0);
                    childNode.insertBefore(parentElement, unidElement);
                }
            }
        }
    }
    
    
    private Element findNodeById(int id) {
        NodeList leaves = leafNodesElement.getElementsByTagName("LEAF");
        for (int i = 0; i < leaves.getLength(); i++) {
            Element leaf = (Element) leaves.item(i);
            if (Integer.parseInt(leaf.getElementsByTagName("UNID").item(0).getTextContent()) == id) {
                return leaf;
            }
        }

        NodeList inners = innerNodesElement.getElementsByTagName("IN");
        for (int i = 0; i < inners.getLength(); i++) {
            Element inner = (Element) inners.item(i);
            if (Integer.parseInt(inner.getElementsByTagName("UNID").item(0).getTextContent()) == id) {
                return inner;
            }
        }
        return null;
    }

    private void applyReduction(int ruleNumber) {
        // System.out.println("Applying reduction rule: " + ruleNumber);

        int symbolsToPop;
        String nonTerminal;
    
        switch (ruleNumber) {
            case 0: // PROG -> main GLOBVARS ALGO FUNCTIONS
                symbolsToPop = 4; // GLOBVARS, ALGO, FUNCTIONS, main
                nonTerminal = "PROG";
                break;
            case 1: // GLOBVARS -> ''
                symbolsToPop = 0; // Epsilon production
                nonTerminal = "GLOBVARS";
                break;
            case 2: // GLOBVARS -> VTYP VNAME , GLOBVARS
                symbolsToPop = 4; // VTYP, VNAME, ',', GLOBVARS
                nonTerminal = "GLOBVARS";
                break;
            case 3: // VTYP -> num
                symbolsToPop = 1; // num
                nonTerminal = "VTYP";
                break;
            case 4: // VTYP -> text
                symbolsToPop = 1; // text
                nonTerminal = "VTYP";
                break;
            case 5: // VNAME -> V
                symbolsToPop = 1; // V
                nonTerminal = "VNAME";
                break;
            case 6: // ALGO -> begin INSTRUC end
                symbolsToPop = 3; // INSTRUC, begin, end
                nonTerminal = "ALGO";
                break;
            case 7: // INSTRUC -> ''
                symbolsToPop = 0; // Epsilon production
                nonTerminal = "INSTRUC";
                break;
            case 8: // INSTRUC -> COMMAND ; INSTRUC
                symbolsToPop = 3; // COMMAND, ;, INSTRUC
                nonTerminal = "INSTRUC";
                break;
            case 9: // COMMAND -> skip
                symbolsToPop = 1; // skip
                nonTerminal = "COMMAND";
                break;
            case 10: // COMMAND -> halt
                symbolsToPop = 1; // halt
                nonTerminal = "COMMAND";
                break;
            case 11: // COMMAND -> print ATOMIC
                symbolsToPop = 2; // print, ATOMIC
                nonTerminal = "COMMAND";
                break;
            case 12: // COMMAND -> ASSIGN
                symbolsToPop = 1; // ASSIGN
                nonTerminal = "COMMAND";
                break;
            case 13: // COMMAND -> CALL
                symbolsToPop = 1; // CALL
                nonTerminal = "COMMAND";
                break;
            case 14: // COMMAND -> BRANCH
                symbolsToPop = 1; // BRANCH
                nonTerminal = "COMMAND";
                break;
            case 15: // ATOMIC -> VNAME
                symbolsToPop = 1; // VNAME
                nonTerminal = "ATOMIC";
                break;
            case 16: // ATOMIC -> CONST
                symbolsToPop = 1; // CONST
                nonTerminal = "ATOMIC";
                break;
            case 17: // CONST -> N
                symbolsToPop = 1; // N
                nonTerminal = "CONST";
                break;
            case 18: // CONST -> T
                symbolsToPop = 1; // T
                nonTerminal = "CONST";
                break;
            case 19: // ASSIGN -> VNAME < input
                symbolsToPop = 3; // VNAME, <, input
                nonTerminal = "ASSIGN";
                break;
            case 20: // ASSIGN -> VNAME = TERM
                symbolsToPop = 3; // VNAME, =, TERM
                nonTerminal = "ASSIGN";
                break;
            case 21: // CALL -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
                symbolsToPop = 8; // FNAME, (, ATOMIC, ,, ATOMIC, ,, ATOMIC, )
                nonTerminal = "CALL";
                break;
            case 22: // BRANCH -> if COND then ALGO else ALGO
                symbolsToPop = 6; // if, COND, then, ALGO, else, ALGO
                nonTerminal = "BRANCH";
                break;
            case 23: // TERM -> ATOMIC
                symbolsToPop = 1; // ATOMIC
                nonTerminal = "TERM";
                break;
            case 24: // TERM -> CALL
                symbolsToPop = 1; // CALL
                nonTerminal = "TERM";
                break;
            case 25: // TERM -> OP
                symbolsToPop = 1; // OP
                nonTerminal = "TERM";
                break;
            case 26: // OP -> UNOP ( ARG )
                symbolsToPop = 4; // UNOP, (, ARG, )
                nonTerminal = "OP";
                break;
            case 27: // OP -> BINOP ( ARG , ARG )
                symbolsToPop = 6; // BINOP, (, ARG, ,, ARG, )
                nonTerminal = "OP";
                break;
            case 28: // ARG -> ATOMIC
                symbolsToPop = 1; // ATOMIC
                nonTerminal = "ARG";
                break;
            case 29: // ARG -> OP
                symbolsToPop = 1; // OP
                nonTerminal = "ARG";
                break;
            case 30: // COND -> SIMPLE
                symbolsToPop = 1; // SIMPLE
                nonTerminal = "COND";
                break;
            case 31: // COND -> COMPOSIT
                symbolsToPop = 1; // COMPOSIT
                nonTerminal = "COND";
                break;
            case 32: // SIMPLE -> BINOP ( ATOMIC , ATOMIC )
                symbolsToPop = 6; // BINOP, (, ATOMIC, ,, ATOMIC, )
                nonTerminal = "SIMPLE";
                break;
            case 33: // COMPOSIT -> BINOP ( SIMPLE , SIMPLE )
                symbolsToPop = 6; // BINOP, (, SIMPLE, ,, SIMPLE, )
                nonTerminal = "COMPOSIT";
                break;
            case 34: // COMPOSIT -> UNOP ( SIMPLE )
                symbolsToPop = 4; // UNOP, (, SIMPLE, )
                nonTerminal = "COMPOSIT";
                break;
            case 35: // UNOP -> not
                symbolsToPop = 1; // not
                nonTerminal = "UNOP";
                break;
            case 36: // UNOP -> sqrt
                symbolsToPop = 1; // sqrt
                nonTerminal = "UNOP";
                break;
            case 37: // BINOP -> or
                symbolsToPop = 1; // or
                nonTerminal = "BINOP";
                break;
            case 38: // BINOP -> and
                symbolsToPop = 1; // and
                nonTerminal = "BINOP";
                break;
            case 39: // BINOP -> eq
                symbolsToPop = 1; // eq
                nonTerminal = "BINOP";
                break;
            case 40: // BINOP -> grt
                symbolsToPop = 1; // grt
                nonTerminal = "BINOP";
                break;
            case 41: // BINOP -> add
                symbolsToPop = 1; // add
                nonTerminal = "BINOP";
                break;
            case 42: // BINOP -> sub
                symbolsToPop = 1; // sub
                nonTerminal = "BINOP";
                break;
            case 43: // BINOP -> mul
                symbolsToPop = 1; // mul
                nonTerminal = "BINOP";
                break;
            case 44: // BINOP -> div
                symbolsToPop = 1; // div
                nonTerminal = "BINOP";
                break;
            case 45: // FNAME -> F
                symbolsToPop = 1; // F
                nonTerminal = "FNAME";
                break;
            case 46: // FUNCTIONS -> ''
                symbolsToPop = 0; // Epsilon production
                nonTerminal = "FUNCTIONS";
                break;
            case 47: // FUNCTIONS -> DECL FUNCTIONS
                symbolsToPop = 2; // DECL, FUNCTIONS
                nonTerminal = "FUNCTIONS";
                break;
            case 48: // DECL -> HEADER BODY
                symbolsToPop = 2; // HEADER, BODY
                nonTerminal = "DECL";
                break;
            case 49: // HEADER -> FTYP FNAME ( VNAME , VNAME , VNAME )
                symbolsToPop = 9; // FTYP, FNAME, (, VNAME, ,, VNAME, ,, VNAME, )
                nonTerminal = "HEADER";
                break;
            case 50: // FTYP -> num
                symbolsToPop = 1; // num
                nonTerminal = "FTYP";
                break;
            case 51: // FTYP -> void
                symbolsToPop = 1; // void
                nonTerminal = "FTYP";
                break;
            case 52: // BODY -> PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
                symbolsToPop = 6; // PROLOG, LOCVARS, ALGO, EPILOG, SUBFUNCS, end
                nonTerminal = "BODY";
                break;
            case 53: // PROLOG -> {
                symbolsToPop = 1; // {
                nonTerminal = "PROLOG";
                break;
            case 54: // EPILOG -> }
                symbolsToPop = 1; // }
                nonTerminal = "EPILOG";
                break;
            case 55: // LOCVARS -> VTYP VNAME , VTYP VNAME , VTYP VNAME ,
                symbolsToPop = 9; // VTYP, VNAME, ,, VTYP, VNAME, ,, VTYP, VNAME, ,
                nonTerminal = "LOCVARS";
                break;
            case 56: // SUBFUNCS -> FUNCTIONS
                symbolsToPop = 1; // FUNCTIONS
                nonTerminal = "SUBFUNCS";
                break;
            case 57: // COMMAND -> return ATOMIC
                symbolsToPop = 2; // return, ATOMIC
                nonTerminal = "COMMAND";
                break;
            default:
                System.out.println("Error: Invalid rule number " + ruleNumber);
                return;
        }

        List<TokenNode> poppedNodes = new ArrayList<>();
        for (int i = 0; i < symbolsToPop; i++) {
            TokenNode poppedNode = stack.pop();
            poppedNodes.add(poppedNode);
        }
    
        // Create a new non-terminal node
        int newState = nonTerminalTable.get(stack.peek().state).get(nonTerminal);
        TokenNode newTokenNode = new TokenNode(newState, nonTerminal, nonTerminal);
        newTokenNode.parent = stack.peek(); // Set the parent to the current top of the stack
    
        // Link all the popped nodes as children to this new non-terminal node
        for (TokenNode child : poppedNodes) {
            child.parent = newTokenNode;
        }
    
        // Push the new non-terminal node onto the stack
        stack.push(newTokenNode);
    
        // Add this non-terminal node as an inner node in the syntax tree
        addInnerNode(newTokenNode, poppedNodes);
    }
    
    public void setTokenStream(String tokenStreamXML) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(tokenStreamXML));
            this.tokenStream = builder.parse(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLeafNode(Node tokenNode, TokenNode t) {
        try {
            Element leafNode = syntaxTree.createElement("LEAF");

            // Assign a unique ID to the leaf
            Element unidElement = syntaxTree.createElement("UNID");
            unidElement.appendChild(syntaxTree.createTextNode(String.valueOf(t.id)));
            leafNode.appendChild(unidElement);

            // Add the terminal (token) as the content of the leaf node
            Element terminalElement = syntaxTree.createElement("TERMINAL");
            Node importedNode = syntaxTree.importNode(tokenNode, true);
            terminalElement.appendChild(importedNode);
            leafNode.appendChild(terminalElement);

            leafNodesElement.appendChild(leafNode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addInnerNode(TokenNode nonTerminal, List<TokenNode> children) {
        Element innerNode = syntaxTree.createElement("IN");

        Element unidElement = syntaxTree.createElement("UNID");
        unidElement.appendChild(syntaxTree.createTextNode(String.valueOf(nonTerminal.id)));
        innerNode.appendChild(unidElement);

        Element symbElement = syntaxTree.createElement("SYMB");
        symbElement.appendChild(syntaxTree.createTextNode(nonTerminal.word));
        innerNode.appendChild(symbElement);

        Element childrenElement = syntaxTree.createElement("CHILDREN");
        for (TokenNode child : children) {
            Element idElement = syntaxTree.createElement("ID");
            idElement.appendChild(syntaxTree.createTextNode(child.id.toString()));
            childrenElement.appendChild(idElement);
        }
        innerNode.appendChild(childrenElement);

        innerNodesElement.appendChild(innerNode);
    }
    
    public void printSyntaxTree() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            
            DOMSource source = new DOMSource(syntaxTree);

            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source, consoleResult);
            
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public class TokenNode {
        int state;
        String classType;
        String word;
        Integer id;
        TokenNode parent;

        public TokenNode(int state, String classType, String word) {
            this.state = state;
            this.classType = classType;
            this.word = word;
            this.id = uniqueIdCounter++;
        }

        public String toString() {
            return "" + state;
        }
    }

    public void writeSyntaxTreeToFile(String filePath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            // Set formatting properties for the output
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            
            // Source is the XML syntax tree document
            DOMSource source = new DOMSource(syntaxTree);
            
            // StreamResult now writes to the specified file instead of the console
            StreamResult fileResult = new StreamResult(new File(filePath));
            
            // Transform the syntax tree to the file
            transformer.transform(source, fileResult);
            
            System.out.println("Syntax tree successfully written to: " + filePath);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void initializeSyntaxTree() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            syntaxTree = builder.newDocument();
    
            // Create the root element <SYNTAXTREE>
            rootElement = syntaxTree.createElement("SYNTAXTREE");
            syntaxTree.appendChild(rootElement);
    
            // Add the initial root node to the syntax tree
            Element rootNode = syntaxTree.createElement("ROOT");
    
            

            // Add root UNID
            Element rootIdElement = syntaxTree.createElement("UNID");
            rootIdElement.appendChild(syntaxTree.createTextNode(String.valueOf(uniqueIdCounter)));
            rootNode.appendChild(rootIdElement);
    
            // Add root symbol (start symbol)
            Element rootSymbolElement = syntaxTree.createElement("SYMB");
            rootSymbolElement.appendChild(syntaxTree.createTextNode("PROG")); // Assuming "PROG" is the start symbol
            rootNode.appendChild(rootSymbolElement);
    
            // Add the <CHILDREN> element under the root
            Element rootChildren = syntaxTree.createElement("CHILDREN");
            rootNode.appendChild(rootChildren);
    
            // Now, append the root node to the syntax tree at the correct position (first child)
            rootElement.appendChild(rootNode);
    
            // Create the inner nodes container element <INNERNODES>
            innerNodesElement = syntaxTree.createElement("INNERNODES");
            rootElement.appendChild(innerNodesElement);
    
            // Create the leaf nodes container element <LEAFNODES>
            leafNodesElement = syntaxTree.createElement("LEAFNODES");
            rootElement.appendChild(leafNodesElement);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addTerminalRow(Map<Integer, Map<String, String>> terminalTable, int state, String... entries) {
        Map<String, String> row = new HashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            row.put(entries[i], entries[i + 1]);
        }
        terminalTable.put(state, row);
    }
    
    // Helper method to add a non-terminal row
    private void addNonTerminalRow(Map<Integer, Map<String, Integer>> nonTerminalTable, int state, String... entries) {
        Map<String, Integer> row = new HashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            row.put(entries[i], Integer.parseInt(entries[i + 1]));
        }
        nonTerminalTable.put(state, row);
    }

    private void initializeParsingTable() {

        addTerminalRow(terminalTable, 0, "main", "s1");
        addTerminalRow(terminalTable, 1, "num", "s4", "text", "s5", "begin", "r1");
        addNonTerminalRow(nonTerminalTable, 1, "GLOBVARS", "2", "VTYP", "3");
        addTerminalRow(terminalTable, 2, "begin", "s7");
        addNonTerminalRow(nonTerminalTable, 2, "ALGO", "6");
        addTerminalRow(terminalTable, 3, "V", "s9");
        addNonTerminalRow(nonTerminalTable, 3, "VNAME", "8");
        addTerminalRow(terminalTable, 4, "V", "r3");
        addTerminalRow(terminalTable, 5, "V", "r4");
        addTerminalRow(terminalTable, 6, "num", "s14", "end", "r46", "void", "s15", "$", "r46");
        addNonTerminalRow(nonTerminalTable, 6, "FUNCTIONS", "10", "DECL", "11", "HEADER", "12", "FTYP", "13");
        addTerminalRow(terminalTable, 7, "V", "s9", "end", "r7", "skip", "s18", "halt", "s19", "print", "s20", "if", "s27", "F", "s28", "return", "s24");
        addNonTerminalRow(nonTerminalTable, 7, "VNAME", "25", "INSTRUC", "16", "COMMAND", "17", "ASSIGN", "21", "CALL", "22", "BRANCH", "23", "FNAME", "26");
        addTerminalRow(terminalTable, 8, ",", "s29");
        addTerminalRow(terminalTable, 9, ",", "r5", ";", "r5", "<", "r5", "=", "r5", ")", "r5");
        addTerminalRow(terminalTable, 10, "$", "acc");
        addTerminalRow(terminalTable, 11, "num", "s14", "end", "r46", "void", "s15", "$", "r46");
        addNonTerminalRow(nonTerminalTable, 11, "FUNCTIONS", "30", "DECL", "11", "HEADER", "12", "FTYP", "13");
        addTerminalRow(terminalTable, 12, "{", "s33");
        addNonTerminalRow(nonTerminalTable, 12, "BODY", "31", "PROLOG", "32");
        addTerminalRow(terminalTable, 13, "F", "s28");
        addNonTerminalRow(nonTerminalTable, 13, "FNAME", "34");
        addTerminalRow(terminalTable, 14, "F", "r50");
        addTerminalRow(terminalTable, 15, "F", "r51");
        addTerminalRow(terminalTable, 16, "end", "s35");
        addTerminalRow(terminalTable, 17, ";", "s36");
        addTerminalRow(terminalTable, 18, ";", "r9");
        addTerminalRow(terminalTable, 19, ";", "r10");
        addTerminalRow(terminalTable, 20, "V", "s9", "N", "s40", "T", "s41");
        addNonTerminalRow(nonTerminalTable, 20, "VNAME", "38", "ATOMIC", "37", "CONST", "39");
        addTerminalRow(terminalTable, 21, ";", "r12");
        addTerminalRow(terminalTable, 22, ";", "r13");
        addTerminalRow(terminalTable, 23, ";", "r14");
        addTerminalRow(terminalTable, 24, "V", "s9", "N", "s40", "T", "s41");
        addNonTerminalRow(nonTerminalTable, 24, "VNAME", "38", "ATOMIC", "42", "CONST", "39");
        addTerminalRow(terminalTable, 25, "<", "s43", "=", "s44");
        addTerminalRow(terminalTable, 26, "(", "s45");
        addTerminalRow(terminalTable, 27, "not", "s59", "sqrt", "s60", "or", "s51", "and", "s52", "eq", "s53", "grt", "s54", "add", "s55", "sub", "s56", "mul", "s57", "div", "s58");
        addNonTerminalRow(nonTerminalTable, 27, "COND", "46", "SIMPLE", "47", "COMPOSIT", "48", "UNOP", "50", "BINOP", "49");
        addTerminalRow(terminalTable, 28, "(", "r45");
        addTerminalRow(terminalTable, 29, "num", "s4", "text", "s5", "begin", "r1");
        addNonTerminalRow(nonTerminalTable, 29, "GLOBVARS", "61", "VTYP", "3");
        addTerminalRow(terminalTable, 30, "end", "r47", "$", "r47");
        addTerminalRow(terminalTable, 31, "num", "r48", "end", "r48", "void", "r48", "$", "r48");
        addTerminalRow(terminalTable, 32, "num", "s4", "text", "s5");
        addNonTerminalRow(nonTerminalTable, 32, "VTYP", "63", "LOCVARS", "62");
        addTerminalRow(terminalTable, 33, "num", "r53", "text", "r53");
        addTerminalRow(terminalTable, 34, "(", "s64");
        addTerminalRow(terminalTable, 35, "num", "r6", ";", "r6", "else", "r6", "void", "r6", "}", "r6", "$", "r6");
        addTerminalRow(terminalTable, 36, "V", "s9", "end", "r7", "skip", "s18", "halt", "s19", "print", "s20", "if", "s27", "F", "s28", "return", "s24");
        addNonTerminalRow(nonTerminalTable, 36, "VNAME", "25", "INSTRUC", "65", "COMMAND", "17", "ASSIGN", "21", "CALL", "22", "BRANCH", "23", "FNAME", "26");
        addTerminalRow(terminalTable, 37, ";", "r11");
        addTerminalRow(terminalTable, 38, ",", "r15", ";", "r15", ")", "r15");
        addTerminalRow(terminalTable, 39, ",", "r16", ";", "r16", ")", "r16");
        addTerminalRow(terminalTable, 40, ",", "r17", ";", "r17", ")", "r17");
        addTerminalRow(terminalTable, 41, ",", "r18", ";", "r18", ")", "r18");
        addTerminalRow(terminalTable, 42, ";", "r57");
        addTerminalRow(terminalTable, 43, "input", "s66");
        addTerminalRow(terminalTable, 44, "V", "s9", "N", "s40", "T", "s41", "not", "s59", "sqrt", "s60", "or", "s51", "and", "s52", "eq", "s53", "grt", "s54", "add", "s55", "sub", "s56", "mul", "s57", "div", "s58", "F", "s28");
        addNonTerminalRow(nonTerminalTable, 44, "VNAME", "38", "ATOMIC", "68", "CONST", "39", "CALL", "69", "TERM", "67", "OP", "70", "UNOP", "71", "BINOP", "72", "FNAME", "26");
        addTerminalRow(terminalTable, 45, "V", "s9", "N", "s40", "T", "s41");
        addNonTerminalRow(nonTerminalTable, 45, "VNAME", "38", "ATOMIC", "73", "CONST", "39");
        addTerminalRow(terminalTable, 46, "then", "s74");
        addTerminalRow(terminalTable, 47, "then", "r30");
        addTerminalRow(terminalTable, 48, "then", "r31");
        addTerminalRow(terminalTable, 49, "(", "s75");
        addTerminalRow(terminalTable, 50, "(", "s76");
        addTerminalRow(terminalTable, 51, "(", "r37");
        addTerminalRow(terminalTable, 52, "(", "r38");
        addTerminalRow(terminalTable, 53, "(", "r39");
        addTerminalRow(terminalTable, 54, "(", "r40");
        addTerminalRow(terminalTable, 55, "(", "r41");
        addTerminalRow(terminalTable, 56, "(", "r42");
        addTerminalRow(terminalTable, 57, "(", "r43");
        addTerminalRow(terminalTable, 58, "(", "r44");
        addTerminalRow(terminalTable, 59, "(", "r35");
        addTerminalRow(terminalTable, 60, "(", "r36");
        addTerminalRow(terminalTable, 61, "begin", "r2");
        addTerminalRow(terminalTable, 62, "begin", "s7");
        addNonTerminalRow(nonTerminalTable, 62, "ALGO", "77");
        addTerminalRow(terminalTable, 63, "V", "s9");
        addNonTerminalRow(nonTerminalTable, 63, "VNAME", "78");
        addTerminalRow(terminalTable, 64, "V", "s9");
        addNonTerminalRow(nonTerminalTable, 64, "VNAME", "79");
        addTerminalRow(terminalTable, 65, "end", "r8");
        addTerminalRow(terminalTable, 66, ";", "r19");
        addTerminalRow(terminalTable, 67, ";", "r20");
        addTerminalRow(terminalTable, 68, ";", "r23");
        addTerminalRow(terminalTable, 69, ";", "r24");
        addTerminalRow(terminalTable, 70, ";", "r25");
        addTerminalRow(terminalTable, 71, "(", "s80");
        addTerminalRow(terminalTable, 72, "(", "s81");
        addTerminalRow(terminalTable, 73, ",", "s82");
        addTerminalRow(terminalTable, 74, "begin", "s7");
        addNonTerminalRow(nonTerminalTable, 74, "ALGO", "83");
        addTerminalRow(terminalTable, 75, "V", "s9", "N", "s40", "T", "s41", "or", "s51", "and", "s52", "eq", "s53", "grt", "s54", "add", "s55", "sub", "s56", "mul", "s57", "div", "s58");
        addNonTerminalRow(nonTerminalTable, 75, "VNAME", "38", "ATOMIC", "84", "CONST", "39", "SIMPLE", "85", "BINOP", "86");
        addTerminalRow(terminalTable, 76, "or", "s51", "and", "s52", "eq", "s53", "grt", "s54", "add", "s55", "sub", "s56", "mul", "s57", "div", "s58");
        addNonTerminalRow(nonTerminalTable, 76, "SIMPLE", "87", "BINOP", "86");
        addTerminalRow(terminalTable, 77, "}", "s89");
        addNonTerminalRow(nonTerminalTable, 77, "EPILOG", "88");
        addTerminalRow(terminalTable, 78, ",", "s90");
        addTerminalRow(terminalTable, 79, ",", "s91");
        addTerminalRow(terminalTable, 80, "V", "s9", "N", "s40", "T", "s41", "not", "s59", "sqrt", "s60", "or", "s51", "and", "s52", "eq", "s53", "grt", "s54", "add", "s55", "sub", "s56", "mul", "s57", "div", "s58");
        addNonTerminalRow(nonTerminalTable, 80, "VNAME", "38", "ATOMIC", "93", "CONST", "39", "OP", "94", "ARG", "92", "UNOP", "71", "BINOP", "72");
        addTerminalRow(terminalTable, 81, "V", "s9", "N", "s40", "T", "s41", "not", "s59", "sqrt", "s60", "or", "s51", "and", "s52", "eq", "s53", "grt", "s54", "add", "s55", "sub", "s56", "mul", "s57", "div", "s58");
        addNonTerminalRow(nonTerminalTable, 81, "VNAME", "38", "ATOMIC", "93", "CONST", "39", "OP", "94", "ARG", "95", "UNOP", "71", "BINOP", "72");
        addTerminalRow(terminalTable, 82, "V", "s9", "N", "s40", "T", "s41");
        addNonTerminalRow(nonTerminalTable, 82, "VNAME", "38", "ATOMIC", "96", "CONST", "39");
        addTerminalRow(terminalTable, 83, "else", "s97");
        addTerminalRow(terminalTable, 84, ",", "s98");
        addTerminalRow(terminalTable, 85, ",", "s99");
        addTerminalRow(terminalTable, 86, "(", "s100");
        addTerminalRow(terminalTable, 87, ")", "s101");
        addTerminalRow(terminalTable, 88, "num", "s14", "end", "r46", "void", "s15", "$", "r46");
        addNonTerminalRow(nonTerminalTable, 88, "FUNCTIONS", "103", "DECL", "11", "HEADER", "12", "FTYP", "13", "SUBFUNCS", "102");
        addTerminalRow(terminalTable, 89, "num", "r54", "end", "r54", "void", "r54", "$", "r54");
        addTerminalRow(terminalTable, 90, "num", "s4", "text", "s5");
        addNonTerminalRow(nonTerminalTable, 90, "VTYP", "104");
        addTerminalRow(terminalTable, 91, "V", "s9");
        addNonTerminalRow(nonTerminalTable, 91, "VNAME", "105");
        addTerminalRow(terminalTable, 92, ")", "s106");
        addTerminalRow(terminalTable, 93, ",", "r28", ")", "r28");
        addTerminalRow(terminalTable, 94, ",", "r29", ")", "r29");
        addTerminalRow(terminalTable, 95, ",", "s107");
        addTerminalRow(terminalTable, 96, ",", "s108");
        addTerminalRow(terminalTable, 97, "begin", "s7");
        addNonTerminalRow(nonTerminalTable, 97, "ALGO", "109");
        addTerminalRow(terminalTable, 98, "V", "s9", "N", "s40", "T", "s41");
        addNonTerminalRow(nonTerminalTable, 98, "VNAME", "38", "ATOMIC", "110", "CONST", "39");
        addTerminalRow(terminalTable, 99, "or", "s51", "and", "s52", "eq", "s53", "grt", "s54", "add", "s55", "sub", "s56", "mul", "s57", "div", "s58");
        addNonTerminalRow(nonTerminalTable, 99, "SIMPLE", "111", "BINOP", "86");
        addTerminalRow(terminalTable, 100, "V", "s9", "N", "s40", "T", "s41");
        addNonTerminalRow(nonTerminalTable, 100, "VNAME", "38", "ATOMIC", "84", "CONST", "39");
        addTerminalRow(terminalTable, 101, "then", "r34");
        addTerminalRow(terminalTable, 102, "end", "s112");
        addTerminalRow(terminalTable, 103, "end", "r56");
        addTerminalRow(terminalTable, 104, "V", "s9");
        addNonTerminalRow(nonTerminalTable, 104, "VNAME", "113");
        addTerminalRow(terminalTable, 105, ",", "s114");
        addTerminalRow(terminalTable, 106, ",", "r26", ";", "r26", ")", "r26");
        addTerminalRow(terminalTable, 107, "V", "s9", "N", "s40", "T", "s41", "not", "s59", "sqrt", "s60", "or", "s51", "and", "s52", "eq", "s53", "grt", "s54", "add", "s55", "sub", "s56", "mul", "s57", "div", "s58");
        addNonTerminalRow(nonTerminalTable, 107, "VNAME", "38", "ATOMIC", "93", "CONST", "39", "OP", "94", "ARG", "115", "UNOP", "71", "BINOP", "72");
        addTerminalRow(terminalTable, 108, "V", "s9", "N", "s40", "T", "s41");
        addNonTerminalRow(nonTerminalTable, 108, "VNAME", "38", "ATOMIC", "116", "CONST", "39");
        addTerminalRow(terminalTable, 109, ";", "r22");
        addTerminalRow(terminalTable, 110, ")", "s117");
        addTerminalRow(terminalTable, 111, ")", "s118");
        addTerminalRow(terminalTable, 112, "num", "r52", "end", "r52", "void", "r52", "$", "r52");
        addTerminalRow(terminalTable, 113, ",", "s119");
        addTerminalRow(terminalTable, 114, "V", "s9");
        addNonTerminalRow(nonTerminalTable, 114, "VNAME", "120");
        addTerminalRow(terminalTable, 115, ")", "s121");
        addTerminalRow(terminalTable, 116, ")", "s122");
        addTerminalRow(terminalTable, 117, ",", "r32", ")", "r32", "then", "r32");
        addTerminalRow(terminalTable, 118, "then", "r33");
        addTerminalRow(terminalTable, 119, "num", "s4", "text", "s5");
        addNonTerminalRow(nonTerminalTable, 119, "VTYP", "123");
        addTerminalRow(terminalTable, 120, ")", "s124");
        addTerminalRow(terminalTable, 121, ",", "r27", ";", "r27", ")", "r27");
        addTerminalRow(terminalTable, 122, ";", "r21");
        addTerminalRow(terminalTable, 123, "V", "s9");
        addNonTerminalRow(nonTerminalTable, 123, "VNAME", "125");
        addTerminalRow(terminalTable, 124, "{", "r49");
        addTerminalRow(terminalTable, 125, ",", "s126");
        addTerminalRow(terminalTable, 126, "begin", "r55");


    }
}