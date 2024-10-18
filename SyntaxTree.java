import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

class SyntaxTree {
    public TreeNode root;
    public Map<String, TreeNode> nodeMap; // To map UNIDs to nodes for quick access

    public SyntaxTree() {
        nodeMap = new HashMap<>();
        buildTree("resources/SyntaxTree.xml");
        // printTree();
    }

    // Build the syntax tree from XML
    public void buildTree(String xmlFilePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFilePath);
            Element rootElement = doc.getDocumentElement();

            // Process the root node
            Element rootNodeElement = (Element) rootElement.getElementsByTagName("ROOT").item(0);
            root = processNode(rootNodeElement);
            nodeMap.put(root.id, root);

            // Process inner nodes
            NodeList innerNodes = rootElement.getElementsByTagName("IN");
            for (int i = 0; i < innerNodes.getLength(); i++) {
                Element innerNodeElement = (Element) innerNodes.item(i);
                TreeNode innerNode = processNode(innerNodeElement);
                nodeMap.put(innerNode.id, innerNode);
            }

            // Process leaf nodes
            NodeList leafNodes = rootElement.getElementsByTagName("LEAF");
            for (int i = 0; i < leafNodes.getLength(); i++) {
                Element leafNodeElement = (Element) leafNodes.item(i);
                TreeNode leafNode = processLeafNode(leafNodeElement);
                nodeMap.put(leafNode.id, leafNode);
            }

            // Establish parent-child relationships
            establishParentChildRelationships(rootElement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Process a general node (either root or inner node)
    public TreeNode processNode(Element nodeElement) {
        String id = nodeElement.getElementsByTagName("UNID").item(0).getTextContent();
        String symbol = nodeElement.getElementsByTagName("SYMB").item(0).getTextContent();
        return new TreeNode(id, symbol);
    }

    // Process a leaf node with additional fields like ID, CLASS, and WORD
    public TreeNode processLeafNode(Element leafElement) {
        String id = leafElement.getElementsByTagName("UNID").item(0).getTextContent();
        Element terminal = (Element) leafElement.getElementsByTagName("TERMINAL").item(0);
        Element tok = (Element) terminal.getElementsByTagName("TOK").item(0);

        String word = tok.getElementsByTagName("WORD").item(0).getTextContent();
        String tokenId = tok.getElementsByTagName("ID").item(0).getTextContent();
        String tokenClass = tok.getElementsByTagName("CLASS").item(0).getTextContent();

        // Create a leaf node with extra fields (ID, CLASS, and WORD)
        return new TreeNode(id, word, tokenId, tokenClass);
    }

    // Establish parent-child relationships by reading the "PARENT" and "CHILDREN" fields
    public void establishParentChildRelationships(Element rootElement) {
        // Handle the ROOT node first
        Element rootNodeElement = (Element) rootElement.getElementsByTagName("ROOT").item(0);
        establishChildren(rootNodeElement, root);

        // Handle inner nodes
        NodeList innerNodes = rootElement.getElementsByTagName("IN");
        for (int i = innerNodes.getLength()-1; i >= 0; i--) {
            Element innerNodeElement = (Element) innerNodes.item(i);
            TreeNode childNode = nodeMap.get(innerNodeElement.getElementsByTagName("UNID").item(0).getTextContent());
            String parentId = innerNodeElement.getElementsByTagName("PARENT").item(0).getTextContent();
            TreeNode parentNode = nodeMap.get(parentId);

            if (!parentNode.getChildren().contains(childNode)) {  // Check if child is already added
                parentNode.addChild(childNode);
            }

            establishChildren(innerNodeElement, childNode);
        }
    }

    // Establish the children for a given node
    public void establishChildren(Element nodeElement, TreeNode parentNode) {
        NodeList childrenList = nodeElement.getElementsByTagName("CHILDREN").item(0).getChildNodes();
        for (int i = childrenList.getLength()-1; i >= 0; i--) {
            Node childNode = childrenList.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                String childId = childNode.getTextContent();
                TreeNode childTreeNode = nodeMap.get(childId);
                if (!parentNode.getChildren().contains(childTreeNode)) {
                    parentNode.addChild(childTreeNode);
                }
            }
        }
    }


    public void printTree() {
        printNode(root, 0);
    }

    // Recursive method to print each node and its children
    private void printNode(TreeNode node, int depth) {
        for (int i = 0; i < depth; i++) System.out.print("|"); // Indentation for hierarchy
        System.out.println(node); // Print the current node
        for (TreeNode child : node.getChildren()) {
            printNode(child, depth + 1); 
        }
    }
}

// TreeNode class representing a node in the syntax tree
class TreeNode {
    public String id;
    public String symbol;
    public String tokenId;  
    public String tokenClass; 
    public List<TreeNode> children;
    public TreeNode parent;  

    // Constructor for inner/root nodes
    public TreeNode(String id, String symbol) {
        this.id = id;
        this.symbol = symbol;
        this.children = new ArrayList<>();
        this.parent = null;
    }

    // Constructor for leaf nodes with extra fields (ID, CLASS, WORD)
    public TreeNode(String id, String word, String tokenId, String tokenClass) {
        this.id = id;
        this.symbol = word; // Word as the symbol for leaf nodes
        this.tokenId = tokenId;
        this.tokenClass = tokenClass;
        this.children = new ArrayList<>();
        this.parent = null;
    }

    public void addChild(TreeNode child) {
        children.add(child);
        child.parent = this; 
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        if (tokenId != null && tokenClass != null) {
            return "Leaf [ID=" + id + ", Symbol=" + symbol + ", TokenID=" + tokenId + ", TokenClass=" + tokenClass + ", Parent=" + parent.symbol + "]";
        }
        return "Node [ID=" + id + ", Symbol=" + symbol + "]";
    }
}
