import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.StringReader;
import org.xml.sax.InputSource;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
            // System.out.println("Stack: " + stack);

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

            String action;
            if (isVar) {
                // System.out.println("TempWord: " + tempWord);
                action = terminalTable.get(state).get(tempWord);
            } else {
                action = terminalTable.get(state).get(tokenWord);
            }

            //no defined action
            if (action == null) {
                System.out.println("Syntax Error at token: " + tokenWord);
                break;
            }
            if (action.equals("acc")) {
                List<String> poppedNodeIds = new ArrayList<>();
                for (int j = 0; j < 4; j++) {
                    TokenNode poppedNode = stack.pop();
                    poppedNodeIds.add(String.valueOf(poppedNode.id));
                }
                TokenNode poppedNode = stack.pop();
                
                // Get the <ROOT> node
                Element rootNode = (Element) rootElement.getElementsByTagName("ROOT").item(0);
                
                // Get the <CHILDREN> element under the <ROOT>
                Element rootChildren = (Element) rootNode.getElementsByTagName("CHILDREN").item(0);
                
                // Add each popped node ID as a child ID
                for (String childId : poppedNodeIds) {
                    Element idElement = syntaxTree.createElement("ID");
                    idElement.appendChild(syntaxTree.createTextNode(childId));
                    rootChildren.appendChild(idElement);
                }

                System.out.println("Accepted");
                break;
            }


            if (action.startsWith("s")) {
                int nextState = Integer.parseInt(action.substring(1));
                stack.push(new TokenNode(nextState, tokenClass, tokenWord));
                // System.out.println("Shift "+ nextState + " and " + tokenWord);

                // Create a leaf node for the terminal (token)
                addLeafNode(token);
                i += 1;
            }

            if (action.startsWith("r")) {
                int ruleNumber = Integer.parseInt(action.substring(1));
                applyReduction(ruleNumber);
            }


        }
    }

    private void applyReduction(int ruleNumber) {
        // System.out.println("Applying reduction rule: " + ruleNumber);

        int symbolsToPop;
        String nonTerminal;
        List<String> childrenSymbols = new ArrayList<>();
        List<String> poppedNodeIds = new ArrayList<>();
    
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

        for (int i = 0; i < symbolsToPop; i++) {
            TokenNode poppedNode = stack.pop();
            poppedNodeIds.add(String.valueOf(poppedNode.id));
        }

        addInnerNode(nonTerminal, poppedNodeIds);

        int newState = nonTerminalTable.get(stack.peek().state).get(nonTerminal);
        stack.push(new TokenNode(newState, nonTerminal, nonTerminal));

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

    private void addLeafNode(Node tokenNode) {
        try {
            // Create a leaf node
            Element leafNode = syntaxTree.createElement("LEAF");
    
            // Assign a unique ID to the leaf
            Element unidElement = syntaxTree.createElement("UNID");
            unidElement.appendChild(syntaxTree.createTextNode(String.valueOf(uniqueIdCounter++)));
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
    

    private void addInnerNode(String nonTerminal, List<String> children) {
        Element innerNode = syntaxTree.createElement("IN");

        Element parentElement = syntaxTree.createElement("PARENT");
        parentElement.appendChild(syntaxTree.createTextNode(String.valueOf(uniqueIdCounter - 1))); // Last inserted node is the parent
        innerNode.appendChild(parentElement);

        Element unidElement = syntaxTree.createElement("UNID");
        unidElement.appendChild(syntaxTree.createTextNode(String.valueOf(uniqueIdCounter++)));
        innerNode.appendChild(unidElement);

        Element symbElement = syntaxTree.createElement("SYMB");
        symbElement.appendChild(syntaxTree.createTextNode(nonTerminal));
        innerNode.appendChild(symbElement);

        Element childrenElement = syntaxTree.createElement("CHILDREN");
        for (String child : children) {
            Element idElement = syntaxTree.createElement("ID");
            idElement.appendChild(syntaxTree.createTextNode(child));
            childrenElement.appendChild(idElement);
        }
        innerNode.appendChild(childrenElement);

        innerNodesElement.appendChild(innerNode);
    }

    private void addRootNode(String symbol) {
        Element rootNode = syntaxTree.createElement("ROOT");
    
        // Create the UNID element
        Element unidElement = syntaxTree.createElement("UNID");
        unidElement.appendChild(syntaxTree.createTextNode(String.valueOf(uniqueIdCounter++)));
        rootNode.appendChild(unidElement);
    
        // Create the SYMB element
        Element symbElement = syntaxTree.createElement("SYMB");
        symbElement.appendChild(syntaxTree.createTextNode(symbol));
        rootNode.appendChild(symbElement);
    
        // Create the CHILDREN element (initially empty)
        Element childrenElement = syntaxTree.createElement("CHILDREN");
        rootNode.appendChild(childrenElement);
    
        // Add the ROOT element to the main tree structure
        innerNodesElement.appendChild(rootNode);
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
            rootIdElement.appendChild(syntaxTree.createTextNode(String.valueOf(uniqueIdCounter++)));
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
    

    private void initializeParsingTable() {
        //Populate the TERMINALS and NONTERMINALS
        Map<Integer, Map<String, String>> terminals = new HashMap<>();
        Map<Integer, Map<String, Integer>> nonTerminals = new HashMap<>();

        // State 0 terminal table
        Map<String, String> terminalRow0 = new HashMap<>();
        terminalRow0.put("main", "s1");
        terminalTable.put(0, terminalRow0);

        // State 1 terminal table
        Map<String, String> terminalRow1 = new HashMap<>();
        terminalRow1.put("num", "s4");
        terminalRow1.put("text", "s5");
        terminalRow1.put("begin", "r1");
        terminalTable.put(1, terminalRow1);

        // State 1 nonTerminal table
        Map<String, Integer> nonTerminalRow1 = new HashMap<>();
        nonTerminalRow1.put("GLOBVARS", 2);
        nonTerminalRow1.put("VTYP", 3);
        nonTerminalTable.put(1, nonTerminalRow1);

        // State 2 terminal table
        Map<String, String> terminalRow2 = new HashMap<>();
        terminalRow2.put("begin", "s7");
        terminalTable.put(2, terminalRow2);

        // State 2 nonTerminal table
        Map<String, Integer> nonTerminalRow2 = new HashMap<>();
        nonTerminalRow2.put("ALGO", 6);
        nonTerminalTable.put(2, nonTerminalRow2);

        // State 3 terminal table
        Map<String, String> terminalRow3 = new HashMap<>();
        terminalRow3.put("V", "s9");
        terminalTable.put(3, terminalRow3);

        // State 3 nonTerminal table
        Map<String, Integer> nonTerminalRow3 = new HashMap<>();
        nonTerminalRow3.put("VNAME", 8);
        nonTerminalTable.put(3, nonTerminalRow3);

        // State 4 terminal table
        Map<String, String> terminalRow4 = new HashMap<>();
        terminalRow4.put("V", "r3");
        terminalTable.put(4, terminalRow4);

        // State 5 terminal table
        Map<String, String> terminalRow5 = new HashMap<>();
        terminalRow5.put("V", "r4");
        terminalTable.put(5, terminalRow5);

        // State 6 terminal table
        Map<String, String> terminalRow6 = new HashMap<>();
        terminalRow6.put("num", "s14");
        terminalRow6.put("end", "r46");
        terminalRow6.put("void", "s15");
        terminalRow6.put("$", "r46");
        terminalTable.put(6, terminalRow6);

        // State 6 nonTerminal table
        Map<String, Integer> nonTerminalRow6 = new HashMap<>();
        nonTerminalRow6.put("FUNCTIONS", 10);
        nonTerminalRow6.put("DECL", 11);
        nonTerminalRow6.put("HEADER", 12);
        nonTerminalRow6.put("FTYP", 13);
        nonTerminalTable.put(6, nonTerminalRow6);

        // State 7 terminal table
        Map<String, String> terminalRow7 = new HashMap<>();
        terminalRow7.put("V", "s9");
        terminalRow7.put("end", "r7");
        terminalRow7.put("skip", "s18");
        terminalRow7.put("halt", "s19");
        terminalRow7.put("print", "s20");
        terminalRow7.put("if", "s27");
        terminalRow7.put("F", "s28");
        terminalRow7.put("return", "s24");
        terminalTable.put(7, terminalRow7);

        // State 7 nonTerminal table
        Map<String, Integer> nonTerminalRow7 = new HashMap<>();
        nonTerminalRow7.put("VNAME", 25);
        nonTerminalRow7.put("INSTRUC", 16);
        nonTerminalRow7.put("COMMAND", 17);
        nonTerminalRow7.put("ASSIGN", 21);
        nonTerminalRow7.put("CALL", 22);
        nonTerminalRow7.put("BRANCH", 23);
        nonTerminalRow7.put("FNAME", 26);
        nonTerminalTable.put(7, nonTerminalRow7);

        // State 8 terminal table
        Map<String, String> terminalRow8 = new HashMap<>();
        terminalRow8.put(",", "s29");
        terminalTable.put(8, terminalRow8);

        // State 9 terminal table
        Map<String, String> terminalRow9 = new HashMap<>();
        terminalRow9.put(",", "r5");
        terminalRow9.put(";", "r5");
        terminalRow9.put("<", "r5");
        terminalRow9.put("=", "r5");
        terminalRow9.put(")", "r5");
        terminalTable.put(9, terminalRow9);

        // State 10 terminal table
        Map<String, String> terminalRow10 = new HashMap<>();
        terminalRow10.put("$", "acc");
        terminalTable.put(10, terminalRow10);

        // State 11 terminal table
        Map<String, String> terminalRow11 = new HashMap<>();
        terminalRow11.put("num", "s14");
        terminalRow11.put("end", "r46");
        terminalRow11.put("void", "s15");
        terminalRow11.put("$", "r46");
        terminalTable.put(11, terminalRow11);

        // State 11 nonTerminal table
        Map<String, Integer> nonTerminalRow11 = new HashMap<>();
        nonTerminalRow11.put("FUNCTIONS", 30);
        nonTerminalRow11.put("DECL", 11);
        nonTerminalRow11.put("HEADER", 12);
        nonTerminalRow11.put("FTYP", 13);
        nonTerminalTable.put(11, nonTerminalRow11);

        // State 12 terminal table
        Map<String, String> terminalRow12 = new HashMap<>();
        terminalRow12.put("{", "s33");
        terminalTable.put(12, terminalRow12);

        // State 12 nonTerminal table
        Map<String, Integer> nonTerminalRow12 = new HashMap<>();
        nonTerminalRow12.put("BODY", 31);
        nonTerminalRow12.put("PROLOG", 32);
        nonTerminalTable.put(12, nonTerminalRow12);

        // State 13 terminal table
        Map<String, String> terminalRow13 = new HashMap<>();
        terminalRow13.put("F", "s28");
        terminalTable.put(13, terminalRow13);

        // State 13 nonTerminal table
        Map<String, Integer> nonTerminalRow13 = new HashMap<>();
        nonTerminalRow13.put("FNAME", 34);
        nonTerminalTable.put(13, nonTerminalRow13);

        // State 14 terminal table
        Map<String, String> terminalRow14 = new HashMap<>();
        terminalRow14.put("F", "r50");
        terminalTable.put(14, terminalRow14);

        // State 15 terminal table
        Map<String, String> terminalRow15 = new HashMap<>();
        terminalRow15.put("F", "r51");
        terminalTable.put(15, terminalRow15);

        // State 16 terminal table
        Map<String, String> terminalRow16 = new HashMap<>();
        terminalRow16.put("end", "s35");
        terminalTable.put(16, terminalRow16);

        // State 17 terminal table
        Map<String, String> terminalRow17 = new HashMap<>();
        terminalRow17.put(";", "s36");
        terminalTable.put(17, terminalRow17);

        // State 18 terminal table
        Map<String, String> terminalRow18 = new HashMap<>();
        terminalRow18.put(";", "r9");
        terminalTable.put(18, terminalRow18);

        // State 19 terminal table
        Map<String, String> terminalRow19 = new HashMap<>();
        terminalRow19.put(";", "r10");
        terminalTable.put(19, terminalRow19);

        // State 20 terminal table
        Map<String, String> terminalRow20 = new HashMap<>();
        terminalRow20.put("V", "s9");
        terminalRow20.put("N", "s40");
        terminalRow20.put("T", "s41");
        terminalTable.put(20, terminalRow20);

        // State 20 nonTerminal table
        Map<String, Integer> nonTerminalRow20 = new HashMap<>();
        nonTerminalRow20.put("VNAME", 38);
        nonTerminalRow20.put("ATOMIC", 37);
        nonTerminalRow20.put("CONST", 39);
        nonTerminalTable.put(20, nonTerminalRow20);

        // State 21 terminal table
        Map<String, String> terminalRow21 = new HashMap<>();
        terminalRow21.put(";", "r12");
        terminalTable.put(21, terminalRow21);

        // State 22 terminal table
        Map<String, String> terminalRow22 = new HashMap<>();
        terminalRow22.put(";", "r13");
        terminalTable.put(22, terminalRow22);

        // State 23 terminal table
        Map<String, String> terminalRow23 = new HashMap<>();
        terminalRow23.put(";", "r14");
        terminalTable.put(23, terminalRow23);

        // State 24 terminal table
        Map<String, String> terminalRow24 = new HashMap<>();
        terminalRow24.put("V", "s9");
        terminalRow24.put("N", "s40");
        terminalRow24.put("T", "s41");
        terminalTable.put(24, terminalRow24);

        // State 24 nonTerminal table
        Map<String, Integer> nonTerminalRow24 = new HashMap<>();
        nonTerminalRow24.put("VNAME", 38);
        nonTerminalRow24.put("ATOMIC", 42);
        nonTerminalRow24.put("CONST", 39);
        nonTerminalTable.put(24, nonTerminalRow24);

        // State 25 terminal table
        Map<String, String> terminalRow25 = new HashMap<>();
        terminalRow25.put("<", "s43");
        terminalRow25.put("=", "s44");
        terminalTable.put(25, terminalRow25);

        // State 26 terminal table
        Map<String, String> terminalRow26 = new HashMap<>();
        terminalRow26.put("(", "s45");
        terminalTable.put(26, terminalRow26);

        // State 27 terminal table
        Map<String, String> terminalRow27 = new HashMap<>();
        terminalRow27.put("not", "s59");
        terminalRow27.put("sqrt", "s60");
        terminalRow27.put("or", "s51");
        terminalRow27.put("and", "s52");
        terminalRow27.put("eq", "s53");
        terminalRow27.put("grt", "s54");
        terminalRow27.put("add", "s55");
        terminalRow27.put("sub", "s56");
        terminalRow27.put("mul", "s57");
        terminalRow27.put("div", "s58");
        terminalTable.put(27, terminalRow27);

        // State 27 nonTerminal table
        Map<String, Integer> nonTerminalRow27 = new HashMap<>();
        nonTerminalRow27.put("COND", 46);
        nonTerminalRow27.put("SIMPLE", 47);
        nonTerminalRow27.put("COMPOSIT", 48);
        nonTerminalRow27.put("UNOP", 50);
        nonTerminalRow27.put("BINOP", 49);
        nonTerminalTable.put(27, nonTerminalRow27);

        // State 28 terminal table
        Map<String, String> terminalRow28 = new HashMap<>();
        terminalRow28.put("(", "r45");
        terminalTable.put(28, terminalRow28);

        // State 29 terminal table
        Map<String, String> terminalRow29 = new HashMap<>();
        terminalRow29.put("num", "s4");
        terminalRow29.put("text", "s5");
        terminalRow29.put("begin", "r1");
        terminalTable.put(29, terminalRow29);

        // State 29 nonTerminal table
        Map<String, Integer> nonTerminalRow29 = new HashMap<>();
        nonTerminalRow29.put("GLOBVARS", 61);
        nonTerminalRow29.put("VTYP", 3);
        nonTerminalTable.put(29, nonTerminalRow29);

        // State 30 terminal table
        Map<String, String> terminalRow30 = new HashMap<>();
        terminalRow30.put("end", "r47");
        terminalRow30.put("$", "r47");
        terminalTable.put(30, terminalRow30);

        // State 31 terminal table
        Map<String, String> terminalRow31 = new HashMap<>();
        terminalRow31.put("num", "r48");
        terminalRow31.put("end", "r48");
        terminalRow31.put("void", "r48");
        terminalRow31.put("$", "r48");
        terminalTable.put(31, terminalRow31);

        // State 32 terminal table
        Map<String, String> terminalRow32 = new HashMap<>();
        terminalRow32.put("num", "s4");
        terminalRow32.put("text", "s5");
        terminalTable.put(32, terminalRow32);

        // State 32 nonTerminal table
        Map<String, Integer> nonTerminalRow32 = new HashMap<>();
        nonTerminalRow32.put("VTYP", 63);
        nonTerminalRow32.put("LOCVARS", 62);
        nonTerminalTable.put(32, nonTerminalRow32);

        // State 33 terminal table
        Map<String, String> terminalRow33 = new HashMap<>();
        terminalRow33.put("num", "r53");
        terminalRow33.put("text", "r53");
        terminalTable.put(33, terminalRow33);

        // State 34 terminal table
        Map<String, String> terminalRow34 = new HashMap<>();
        terminalRow34.put("(", "s64");
        terminalTable.put(34, terminalRow34);

        // State 35 terminal table
        Map<String, String> terminalRow35 = new HashMap<>();
        terminalRow35.put("num", "r6");
        terminalRow35.put(";", "r6");
        terminalRow35.put("else", "r6");
        terminalRow35.put("void", "r6");
        terminalRow35.put("}", "r6");
        terminalRow35.put("$", "r6");
        terminalTable.put(35, terminalRow35);

        // State 36 terminal table
        Map<String, String> terminalRow36 = new HashMap<>();
        terminalRow36.put("V", "s9");
        terminalRow36.put("end", "r7");
        terminalRow36.put("skip", "s18");
        terminalRow36.put("halt", "s19");
        terminalRow36.put("print", "s20");
        terminalRow36.put("if", "s27");
        terminalRow36.put("F", "s28");
        terminalRow36.put("return", "s24");
        terminalTable.put(36, terminalRow36);

        // State 36 nonTerminal table
        Map<String, Integer> nonTerminalRow36 = new HashMap<>();
        nonTerminalRow36.put("VNAME", 25);
        nonTerminalRow36.put("INSTRUC", 65);
        nonTerminalRow36.put("COMMAND", 17);
        nonTerminalRow36.put("ASSIGN", 21);
        nonTerminalRow36.put("CALL", 22);
        nonTerminalRow36.put("BRANCH", 23);
        nonTerminalRow36.put("FNAME", 26);
        nonTerminalTable.put(36, nonTerminalRow36);

        // State 37 terminal table
        Map<String, String> terminalRow37 = new HashMap<>();
        terminalRow37.put(";", "r11");
        terminalTable.put(37, terminalRow37);

        // State 38 terminal table
        Map<String, String> terminalRow38 = new HashMap<>();
        terminalRow38.put(",", "r15");
        terminalRow38.put(";", "r15");
        terminalRow38.put(")", "r15");
        terminalTable.put(38, terminalRow38);

        // State 39 terminal table
        Map<String, String> terminalRow39 = new HashMap<>();
        terminalRow39.put(",", "r16");
        terminalRow39.put(";", "r16");
        terminalRow39.put(")", "r16");
        terminalTable.put(39, terminalRow39);

        // State 40 terminal table
        Map<String, String> terminalRow40 = new HashMap<>();
        terminalRow40.put(",", "r17");
        terminalRow40.put(";", "r17");
        terminalRow40.put(")", "r17");
        terminalTable.put(40, terminalRow40);

        // State 41 terminal table
        Map<String, String> terminalRow41 = new HashMap<>();
        terminalRow41.put(",", "r18");
        terminalRow41.put(";", "r18");
        terminalRow41.put(")", "r18");
        terminalTable.put(41, terminalRow41);

        // State 42 terminal table
        Map<String, String> terminalRow42 = new HashMap<>();
        terminalRow42.put(";", "r57");
        terminalTable.put(42, terminalRow42);

        // State 43 terminal table
        Map<String, String> terminalRow43 = new HashMap<>();
        terminalRow43.put("input", "s66");
        terminalTable.put(43, terminalRow43);

        // State 44 terminal table
        Map<String, String> terminalRow44 = new HashMap<>();
        terminalRow44.put("V", "s9");
        terminalRow44.put("N", "s40");
        terminalRow44.put("T", "s41");
        terminalRow44.put("not", "s59");
        terminalRow44.put("sqrt", "s60");
        terminalRow44.put("or", "s51");
        terminalRow44.put("and", "s52");
        terminalRow44.put("eq", "s53");
        terminalRow44.put("grt", "s54");
        terminalRow44.put("add", "s55");
        terminalRow44.put("sub", "s56");
        terminalRow44.put("mul", "s57");
        terminalRow44.put("div", "s58");
        terminalRow44.put("F", "s28");
        terminalTable.put(44, terminalRow44);

        // State 44 nonTerminal table
        Map<String, Integer> nonTerminalRow44 = new HashMap<>();
        nonTerminalRow44.put("VNAME", 38);
        nonTerminalRow44.put("ATOMIC", 68);
        nonTerminalRow44.put("CONST", 39);
        nonTerminalRow44.put("CALL", 69);
        nonTerminalRow44.put("TERM", 67);
        nonTerminalRow44.put("OP", 70);
        nonTerminalRow44.put("UNOP", 71);
        nonTerminalRow44.put("BINOP", 72);
        nonTerminalRow44.put("FNAME", 26);
        nonTerminalTable.put(44, nonTerminalRow44);

        // State 45 terminal table
        Map<String, String> terminalRow45 = new HashMap<>();
        terminalRow45.put("V", "s9");
        terminalRow45.put("N", "s40");
        terminalRow45.put("T", "s41");
        terminalTable.put(45, terminalRow45);

        // State 45 nonTerminal table
        Map<String, Integer> nonTerminalRow45 = new HashMap<>();
        nonTerminalRow45.put("VNAME", 38);
        nonTerminalRow45.put("ATOMIC", 73);
        nonTerminalRow45.put("CONST", 39);
        nonTerminalTable.put(45, nonTerminalRow45);

        // State 46 terminal table
        Map<String, String> terminalRow46 = new HashMap<>();
        terminalRow46.put("then", "s74");
        terminalTable.put(46, terminalRow46);

        // State 47 terminal table
        Map<String, String> terminalRow47 = new HashMap<>();
        terminalRow47.put("then", "r30");
        terminalTable.put(47, terminalRow47);

        // State 48 terminal table
        Map<String, String> terminalRow48 = new HashMap<>();
        terminalRow48.put("then", "r31");
        terminalTable.put(48, terminalRow48);

        // State 49 terminal table
        Map<String, String> terminalRow49 = new HashMap<>();
        terminalRow49.put("(", "s75");
        terminalTable.put(49, terminalRow49);

        // State 50 terminal table
        Map<String, String> terminalRow50 = new HashMap<>();
        terminalRow50.put("(", "s76");
        terminalTable.put(50, terminalRow50);

        // State 51 terminal table
        Map<String, String> terminalRow51 = new HashMap<>();
        terminalRow51.put("(", "r37");
        terminalTable.put(51, terminalRow51);

        // State 52 terminal table
        Map<String, String> terminalRow52 = new HashMap<>();
        terminalRow52.put("(", "r38");
        terminalTable.put(52, terminalRow52);

        // State 53 terminal table
        Map<String, String> terminalRow53 = new HashMap<>();
        terminalRow53.put("(", "r39");
        terminalTable.put(53, terminalRow53);

        // State 54 terminal table
        Map<String, String> terminalRow54 = new HashMap<>();
        terminalRow54.put("(", "r40");
        terminalTable.put(54, terminalRow54);

        // State 55 terminal table
        Map<String, String> terminalRow55 = new HashMap<>();
        terminalRow55.put("(", "r41");
        terminalTable.put(55, terminalRow55);

        // State 56 terminal table
        Map<String, String> terminalRow56 = new HashMap<>();
        terminalRow56.put("(", "r42");
        terminalTable.put(56, terminalRow56);

        // State 57 terminal table
        Map<String, String> terminalRow57 = new HashMap<>();
        terminalRow57.put("(", "r43");
        terminalTable.put(57, terminalRow57);

        // State 58 terminal table
        Map<String, String> terminalRow58 = new HashMap<>();
        terminalRow58.put("(", "r44");
        terminalTable.put(58, terminalRow58);

        // State 59 terminal table
        Map<String, String> terminalRow59 = new HashMap<>();
        terminalRow59.put("(", "r35");
        terminalTable.put(59, terminalRow59);

        // State 60 terminal table
        Map<String, String> terminalRow60 = new HashMap<>();
        terminalRow60.put("(", "r36");
        terminalTable.put(60, terminalRow60);

        // State 61 terminal table
        Map<String, String> terminalRow61 = new HashMap<>();
        terminalRow61.put("begin", "r2");
        terminalTable.put(61, terminalRow61);

        // State 62 terminal table
        Map<String, String> terminalRow62 = new HashMap<>();
        terminalRow62.put("begin", "s7");
        terminalTable.put(62, terminalRow62);

        // State 62 nonTerminal table
        Map<String, Integer> nonTerminalRow62 = new HashMap<>();
        nonTerminalRow62.put("ALGO", 77);
        nonTerminalTable.put(62, nonTerminalRow62);

        // State 63 terminal table
        Map<String, String> terminalRow63 = new HashMap<>();
        terminalRow63.put("V", "s9");
        terminalTable.put(63, terminalRow63);

        // State 63 nonTerminal table
        Map<String, Integer> nonTerminalRow63 = new HashMap<>();
        nonTerminalRow63.put("VNAME", 78);
        nonTerminalTable.put(63, nonTerminalRow63);

        // State 64 terminal table
        Map<String, String> terminalRow64 = new HashMap<>();
        terminalRow64.put("V", "s9");
        terminalTable.put(64, terminalRow64);

        // State 64 nonTerminal table
        Map<String, Integer> nonTerminalRow64 = new HashMap<>();
        nonTerminalRow64.put("VNAME", 79);
        nonTerminalTable.put(64, nonTerminalRow64);

        // State 65 terminal table
        Map<String, String> terminalRow65 = new HashMap<>();
        terminalRow65.put("end", "r8");
        terminalTable.put(65, terminalRow65);

        // State 66 terminal table
        Map<String, String> terminalRow66 = new HashMap<>();
        terminalRow66.put(";", "r19");
        terminalTable.put(66, terminalRow66);

        // State 67 terminal table
        Map<String, String> terminalRow67 = new HashMap<>();
        terminalRow67.put(";", "r20");
        terminalTable.put(67, terminalRow67);

        // State 68 terminal table
        Map<String, String> terminalRow68 = new HashMap<>();
        terminalRow68.put(";", "r23");
        terminalTable.put(68, terminalRow68);

        // State 69 terminal table
        Map<String, String> terminalRow69 = new HashMap<>();
        terminalRow69.put(";", "r24");
        terminalTable.put(69, terminalRow69);

        // State 70 terminal table
        Map<String, String> terminalRow70 = new HashMap<>();
        terminalRow70.put(";", "r25");
        terminalTable.put(70, terminalRow70);

        // State 71 terminal table
        Map<String, String> terminalRow71 = new HashMap<>();
        terminalRow71.put("(", "s80");
        terminalTable.put(71, terminalRow71);

        // State 72 terminal table
        Map<String, String> terminalRow72 = new HashMap<>();
        terminalRow72.put("(", "s81");
        terminalTable.put(72, terminalRow72);

        // State 73 terminal table
        Map<String, String> terminalRow73 = new HashMap<>();
        terminalRow73.put(",", "s82");
        terminalTable.put(73, terminalRow73);

        // State 74 terminal table
        Map<String, String> terminalRow74 = new HashMap<>();
        terminalRow74.put("begin", "s7");
        terminalTable.put(74, terminalRow74);

        // State 74 nonTerminal table
        Map<String, Integer> nonTerminalRow74 = new HashMap<>();
        nonTerminalRow74.put("ALGO", 83);
        nonTerminalTable.put(74, nonTerminalRow74);

        // State 75 terminal table
        Map<String, String> terminalRow75 = new HashMap<>();
        terminalRow75.put("V", "s9");
        terminalRow75.put("N", "s40");
        terminalRow75.put("T", "s41");
        terminalRow75.put("or", "s51");
        terminalRow75.put("and", "s52");
        terminalRow75.put("eq", "s53");
        terminalRow75.put("grt", "s54");
        terminalRow75.put("add", "s55");
        terminalRow75.put("sub", "s56");
        terminalRow75.put("mul", "s57");
        terminalRow75.put("div", "s58");
        terminalTable.put(75, terminalRow75);

        // State 75 nonTerminal table
        Map<String, Integer> nonTerminalRow75 = new HashMap<>();
        nonTerminalRow75.put("VNAME", 38);
        nonTerminalRow75.put("ATOMIC", 84);
        nonTerminalRow75.put("CONST", 39);
        nonTerminalRow75.put("SIMPLE", 85);
        nonTerminalRow75.put("BINOP", 86);
        nonTerminalTable.put(75, nonTerminalRow75);

        // State 76 terminal table
        Map<String, String> terminalRow76 = new HashMap<>();
        terminalRow76.put("or", "s51");
        terminalRow76.put("and", "s52");
        terminalRow76.put("eq", "s53");
        terminalRow76.put("grt", "s54");
        terminalRow76.put("add", "s55");
        terminalRow76.put("sub", "s56");
        terminalRow76.put("mul", "s57");
        terminalRow76.put("div", "s58");
        terminalTable.put(76, terminalRow76);

        // State 76 nonTerminal table
        Map<String, Integer> nonTerminalRow76 = new HashMap<>();
        nonTerminalRow76.put("SIMPLE", 87);
        nonTerminalRow76.put("BINOP", 86);
        nonTerminalTable.put(76, nonTerminalRow76);

        // State 77 terminal table
        Map<String, String> terminalRow77 = new HashMap<>();
        terminalRow77.put("}", "s89");
        terminalTable.put(77, terminalRow77);

        // State 77 nonTerminal table
        Map<String, Integer> nonTerminalRow77 = new HashMap<>();
        nonTerminalRow77.put("EPILOG", 88);
        nonTerminalTable.put(77, nonTerminalRow77);

        // State 78 terminal table
        Map<String, String> terminalRow78 = new HashMap<>();
        terminalRow78.put(",", "s90");
        terminalTable.put(78, terminalRow78);

        // State 79 terminal table
        Map<String, String> terminalRow79 = new HashMap<>();
        terminalRow79.put(",", "s91");
        terminalTable.put(79, terminalRow79);

        // State 80 terminal table
        Map<String, String> terminalRow80 = new HashMap<>();
        terminalRow80.put("V", "s9");
        terminalRow80.put("N", "s40");
        terminalRow80.put("T", "s41");
        terminalRow80.put("not", "s59");
        terminalRow80.put("sqrt", "s60");
        terminalRow80.put("or", "s51");
        terminalRow80.put("and", "s52");
        terminalRow80.put("eq", "s53");
        terminalRow80.put("grt", "s54");
        terminalRow80.put("add", "s55");
        terminalRow80.put("sub", "s56");
        terminalRow80.put("mul", "s57");
        terminalRow80.put("div", "s58");
        terminalTable.put(80, terminalRow80);

        // State 80 nonTerminal table
        Map<String, Integer> nonTerminalRow80 = new HashMap<>();
        nonTerminalRow80.put("VNAME", 38);
        nonTerminalRow80.put("ATOMIC", 93);
        nonTerminalRow80.put("CONST", 39);
        nonTerminalRow80.put("OP", 94);
        nonTerminalRow80.put("ARG", 92);
        nonTerminalRow80.put("UNOP", 71);
        nonTerminalRow80.put("BINOP", 72);
        nonTerminalTable.put(80, nonTerminalRow80);

        // State 81 terminal table
        Map<String, String> terminalRow81 = new HashMap<>();
        terminalRow81.put("V", "s9");
        terminalRow81.put("N", "s40");
        terminalRow81.put("T", "s41");
        terminalRow81.put("not", "s59");
        terminalRow81.put("sqrt", "s60");
        terminalRow81.put("or", "s51");
        terminalRow81.put("and", "s52");
        terminalRow81.put("eq", "s53");
        terminalRow81.put("grt", "s54");
        terminalRow81.put("add", "s55");
        terminalRow81.put("sub", "s56");
        terminalRow81.put("mul", "s57");
        terminalRow81.put("div", "s58");
        terminalTable.put(81, terminalRow81);

        // State 81 nonTerminal table
        Map<String, Integer> nonTerminalRow81 = new HashMap<>();
        nonTerminalRow81.put("VNAME", 38);
        nonTerminalRow81.put("ATOMIC", 93);
        nonTerminalRow81.put("CONST", 39);
        nonTerminalRow81.put("OP", 94);
        nonTerminalRow81.put("ARG", 95);
        nonTerminalRow81.put("UNOP", 71);
        nonTerminalRow81.put("BINOP", 72);
        nonTerminalTable.put(81, nonTerminalRow81);

        // State 82 terminal table
        Map<String, String> terminalRow82 = new HashMap<>();
        terminalRow82.put("V", "s9");
        terminalRow82.put("N", "s40");
        terminalRow82.put("T", "s41");
        terminalTable.put(82, terminalRow82);

        // State 82 nonTerminal table
        Map<String, Integer> nonTerminalRow82 = new HashMap<>();
        nonTerminalRow82.put("VNAME", 38);
        nonTerminalRow82.put("ATOMIC", 96);
        nonTerminalRow82.put("CONST", 39);
        nonTerminalTable.put(82, nonTerminalRow82);

        // State 83 terminal table
        Map<String, String> terminalRow83 = new HashMap<>();
        terminalRow83.put("else", "s97");
        terminalTable.put(83, terminalRow83);

        // State 84 terminal table
        Map<String, String> terminalRow84 = new HashMap<>();
        terminalRow84.put(",", "s98");
        terminalTable.put(84, terminalRow84);

        // State 85 terminal table
        Map<String, String> terminalRow85 = new HashMap<>();
        terminalRow85.put(",", "s99");
        terminalTable.put(85, terminalRow85);

        // State 86 terminal table
        Map<String, String> terminalRow86 = new HashMap<>();
        terminalRow86.put("(", "s100");
        terminalTable.put(86, terminalRow86);

        // State 87 terminal table
        Map<String, String> terminalRow87 = new HashMap<>();
        terminalRow87.put(")", "s101");
        terminalTable.put(87, terminalRow87);

        // State 88 terminal table
        Map<String, String> terminalRow88 = new HashMap<>();
        terminalRow88.put("num", "s14");
        terminalRow88.put("end", "r46");
        terminalRow88.put("void", "s15");
        terminalRow88.put("$", "r46");
        terminalTable.put(88, terminalRow88);

        // State 88 nonTerminal table
        Map<String, Integer> nonTerminalRow88 = new HashMap<>();
        nonTerminalRow88.put("FUNCTIONS", 103);
        nonTerminalRow88.put("DECL", 11);
        nonTerminalRow88.put("HEADER", 12);
        nonTerminalRow88.put("FTYP", 13);
        nonTerminalRow88.put("SUBFUNCS", 102);
        nonTerminalTable.put(88, nonTerminalRow88);

        // State 89 terminal table
        Map<String, String> terminalRow89 = new HashMap<>();
        terminalRow89.put("num", "r54");
        terminalRow89.put("end", "r54");
        terminalRow89.put("void", "r54");
        terminalRow89.put("$", "r54");
        terminalTable.put(89, terminalRow89);

        // State 90 terminal table
        Map<String, String> terminalRow90 = new HashMap<>();
        terminalRow90.put("num", "s4");
        terminalRow90.put("text", "s5");
        terminalTable.put(90, terminalRow90);

        // State 90 nonTerminal table
        Map<String, Integer> nonTerminalRow90 = new HashMap<>();
        nonTerminalRow90.put("VTYP", 104);
        nonTerminalTable.put(90, nonTerminalRow90);

        // State 91 terminal table
        Map<String, String> terminalRow91 = new HashMap<>();
        terminalRow91.put("V", "s9");
        terminalTable.put(91, terminalRow91);

        // State 91 nonTerminal table
        Map<String, Integer> nonTerminalRow91 = new HashMap<>();
        nonTerminalRow91.put("VNAME", 105);
        nonTerminalTable.put(91, nonTerminalRow91);

        // State 92 terminal table
        Map<String, String> terminalRow92 = new HashMap<>();
        terminalRow92.put(")", "s106");
        terminalTable.put(92, terminalRow92);

        // State 93 terminal table
        Map<String, String> terminalRow93 = new HashMap<>();
        terminalRow93.put(",", "r28");
        terminalRow93.put(")", "r28");
        terminalTable.put(93, terminalRow93);

        // State 94 terminal table
        Map<String, String> terminalRow94 = new HashMap<>();
        terminalRow94.put(",", "r29");
        terminalRow94.put(")", "r29");
        terminalTable.put(94, terminalRow94);

        // State 95 terminal table
        Map<String, String> terminalRow95 = new HashMap<>();
        terminalRow95.put(",", "s107");
        terminalTable.put(95, terminalRow95);

        // State 96 terminal table
        Map<String, String> terminalRow96 = new HashMap<>();
        terminalRow96.put(",", "s108");
        terminalTable.put(96, terminalRow96);

        // State 97 terminal table
        Map<String, String> terminalRow97 = new HashMap<>();
        terminalRow97.put("begin", "s7");
        terminalTable.put(97, terminalRow97);

        // State 97 nonTerminal table
        Map<String, Integer> nonTerminalRow97 = new HashMap<>();
        nonTerminalRow97.put("ALGO", 109);
        nonTerminalTable.put(97, nonTerminalRow97);

        // State 98 terminal table
        Map<String, String> terminalRow98 = new HashMap<>();
        terminalRow98.put("V", "s9");
        terminalRow98.put("N", "s40");
        terminalRow98.put("T", "s41");
        terminalTable.put(98, terminalRow98);

        // State 98 nonTerminal table
        Map<String, Integer> nonTerminalRow98 = new HashMap<>();
        nonTerminalRow98.put("VNAME", 38);
        nonTerminalRow98.put("ATOMIC", 110);
        nonTerminalRow98.put("CONST", 39);
        nonTerminalTable.put(98, nonTerminalRow98);

        // State 99 terminal table
        Map<String, String> terminalRow99 = new HashMap<>();
        terminalRow99.put("or", "s51");
        terminalRow99.put("and", "s52");
        terminalRow99.put("eq", "s53");
        terminalRow99.put("grt", "s54");
        terminalRow99.put("add", "s55");
        terminalRow99.put("sub", "s56");
        terminalRow99.put("mul", "s57");
        terminalRow99.put("div", "s58");
        terminalTable.put(99, terminalRow99);

        // State 99 nonTerminal table
        Map<String, Integer> nonTerminalRow99 = new HashMap<>();
        nonTerminalRow99.put("SIMPLE", 111);
        nonTerminalRow99.put("BINOP", 86);
        nonTerminalTable.put(99, nonTerminalRow99);

        // State 100 terminal table
        Map<String, String> terminalRow100 = new HashMap<>();
        terminalRow100.put("V", "s9");
        terminalRow100.put("N", "s40");
        terminalRow100.put("T", "s41");
        terminalTable.put(100, terminalRow100);

        // State 100 nonTerminal table
        Map<String, Integer> nonTerminalRow100 = new HashMap<>();
        nonTerminalRow100.put("VNAME", 38);
        nonTerminalRow100.put("ATOMIC", 84);
        nonTerminalRow100.put("CONST", 39);
        nonTerminalTable.put(100, nonTerminalRow100);

        // State 101 terminal table
        Map<String, String> terminalRow101 = new HashMap<>();
        terminalRow101.put("then", "r34");
        terminalTable.put(101, terminalRow101);

        // State 102 terminal table
        Map<String, String> terminalRow102 = new HashMap<>();
        terminalRow102.put("end", "s112");
        terminalTable.put(102, terminalRow102);

        // State 103 terminal table
        Map<String, String> terminalRow103 = new HashMap<>();
        terminalRow103.put("end", "r56");
        terminalTable.put(103, terminalRow103);

        // State 104 terminal table
        Map<String, String> terminalRow104 = new HashMap<>();
        terminalRow104.put("V", "s9");
        terminalTable.put(104, terminalRow104);

        // State 104 nonTerminal table
        Map<String, Integer> nonTerminalRow104 = new HashMap<>();
        nonTerminalRow104.put("VNAME", 113);
        nonTerminalTable.put(104, nonTerminalRow104);

        // State 105 terminal table
        Map<String, String> terminalRow105 = new HashMap<>();
        terminalRow105.put(",", "s114");
        terminalTable.put(105, terminalRow105);

        // State 106 terminal table
        Map<String, String> terminalRow106 = new HashMap<>();
        terminalRow106.put(",", "r26");
        terminalRow106.put(";", "r26");
        terminalRow106.put(")", "r26");
        terminalTable.put(106, terminalRow106);

        // State 107 terminal table
        Map<String, String> terminalRow107 = new HashMap<>();
        terminalRow107.put("V", "s9");
        terminalRow107.put("N", "s40");
        terminalRow107.put("T", "s41");
        terminalRow107.put("not", "s59");
        terminalRow107.put("sqrt", "s60");
        terminalRow107.put("or", "s51");
        terminalRow107.put("and", "s52");
        terminalRow107.put("eq", "s53");
        terminalRow107.put("grt", "s54");
        terminalRow107.put("add", "s55");
        terminalRow107.put("sub", "s56");
        terminalRow107.put("mul", "s57");
        terminalRow107.put("div", "s58");
        terminalTable.put(107, terminalRow107);

        // State 107 nonTerminal table
        Map<String, Integer> nonTerminalRow107 = new HashMap<>();
        nonTerminalRow107.put("VNAME", 38);
        nonTerminalRow107.put("ATOMIC", 93);
        nonTerminalRow107.put("CONST", 39);
        nonTerminalRow107.put("OP", 94);
        nonTerminalRow107.put("ARG", 115);
        nonTerminalRow107.put("UNOP", 71);
        nonTerminalRow107.put("BINOP", 72);
        nonTerminalTable.put(107, nonTerminalRow107);

        // State 108 terminal table
        Map<String, String> terminalRow108 = new HashMap<>();
        terminalRow108.put("V", "s9");
        terminalRow108.put("N", "s40");
        terminalRow108.put("T", "s41");
        terminalTable.put(108, terminalRow108);

        // State 108 nonTerminal table
        Map<String, Integer> nonTerminalRow108 = new HashMap<>();
        nonTerminalRow108.put("VNAME", 38);
        nonTerminalRow108.put("ATOMIC", 116);
        nonTerminalRow108.put("CONST", 39);
        nonTerminalTable.put(108, nonTerminalRow108);

        // State 109 terminal table
        Map<String, String> terminalRow109 = new HashMap<>();
        terminalRow109.put(";", "r22");
        terminalTable.put(109, terminalRow109);

        // State 110 terminal table
        Map<String, String> terminalRow110 = new HashMap<>();
        terminalRow110.put(")", "s117");
        terminalTable.put(110, terminalRow110);

        // State 111 terminal table
        Map<String, String> terminalRow111 = new HashMap<>();
        terminalRow111.put(")", "s118");
        terminalTable.put(111, terminalRow111);

        // State 112 terminal table
        Map<String, String> terminalRow112 = new HashMap<>();
        terminalRow112.put("num", "r52");
        terminalRow112.put("end", "r52");
        terminalRow112.put("void", "r52");
        terminalRow112.put("$", "r52");
        terminalTable.put(112, terminalRow112);

        // State 113 terminal table
        Map<String, String> terminalRow113 = new HashMap<>();
        terminalRow113.put(",", "s119");
        terminalTable.put(113, terminalRow113);

        // State 114 terminal table
        Map<String, String> terminalRow114 = new HashMap<>();
        terminalRow114.put("V", "s9");
        terminalTable.put(114, terminalRow114);

        // State 114 nonTerminal table
        Map<String, Integer> nonTerminalRow114 = new HashMap<>();
        nonTerminalRow114.put("VNAME", 120);
        nonTerminalTable.put(114, nonTerminalRow114);

        // State 115 terminal table
        Map<String, String> terminalRow115 = new HashMap<>();
        terminalRow115.put(")", "s121");
        terminalTable.put(115, terminalRow115);

        // State 116 terminal table
        Map<String, String> terminalRow116 = new HashMap<>();
        terminalRow116.put(")", "s122");
        terminalTable.put(116, terminalRow116);

        // State 117 terminal table
        Map<String, String> terminalRow117 = new HashMap<>();
        terminalRow117.put(",", "r32");
        terminalRow117.put(")", "r32");
        terminalRow117.put("then", "r32");
        terminalTable.put(117, terminalRow117);

        // State 118 terminal table
        Map<String, String> terminalRow118 = new HashMap<>();
        terminalRow118.put("then", "r33");
        terminalTable.put(118, terminalRow118);

        // State 119 terminal table
        Map<String, String> terminalRow119 = new HashMap<>();
        terminalRow119.put("num", "s4");
        terminalRow119.put("text", "s5");
        terminalTable.put(119, terminalRow119);

        // State 119 nonTerminal table
        Map<String, Integer> nonTerminalRow119 = new HashMap<>();
        nonTerminalRow119.put("VTYP", 123);
        nonTerminalTable.put(119, nonTerminalRow119);

        // State 120 terminal table
        Map<String, String> terminalRow120 = new HashMap<>();
        terminalRow120.put(")", "s124");
        terminalTable.put(120, terminalRow120);

        // State 121 terminal table
        Map<String, String> terminalRow121 = new HashMap<>();
        terminalRow121.put(",", "r27");
        terminalRow121.put(";", "r27");
        terminalRow121.put(")", "r27");
        terminalTable.put(121, terminalRow121);

        // State 122 terminal table
        Map<String, String> terminalRow122 = new HashMap<>();
        terminalRow122.put(";", "r21");
        terminalTable.put(122, terminalRow122);

        // State 123 terminal table
        Map<String, String> terminalRow123 = new HashMap<>();
        terminalRow123.put("V", "s9");
        terminalTable.put(123, terminalRow123);

        // State 123 nonTerminal table
        Map<String, Integer> nonTerminalRow123 = new HashMap<>();
        nonTerminalRow123.put("VNAME", 125);
        nonTerminalTable.put(123, nonTerminalRow123);

        // State 124 terminal table
        Map<String, String> terminalRow124 = new HashMap<>();
        terminalRow124.put("{", "r49");
        terminalTable.put(124, terminalRow124);

        // State 125 terminal table
        Map<String, String> terminalRow125 = new HashMap<>();
        terminalRow125.put(",", "s126");
        terminalTable.put(125, terminalRow125);

        // State 126 terminal table
        Map<String, String> terminalRow126 = new HashMap<>();
        terminalRow126.put("begin", "r55");
        terminalTable.put(126, terminalRow126);

    }
}