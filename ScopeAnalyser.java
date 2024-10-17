import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class ScopeAnalyser {
    public Node root;
    public NodeList innerNodes;
    public NodeList leafNodes;

    public ScopeAnalyser() {
        getNodes("resources/SyntaxTree.xml");
        traverseTree(root);
    }

    public void analyze(String xmlPath) {
        // To be implemented later if needed
    }

    public void traverseTree(Node currentNode) {
        System.out.println("=== IN NEW NODE ===");
        System.out.println("Node: " + currentNode.getNodeName());
    
        // Check if the current node is a leaf node
        if (currentNode.getNodeName().equals("LEAF")) {
            System.out.println("Leaf Reached");
            System.out.println(currentNode.getTextContent());
            return;
        }
    
        Element symbElement = (Element) ((Element) currentNode).getElementsByTagName("SYMB").item(0);
        Element childrenElement = (Element) ((Element) currentNode).getElementsByTagName("CHILDREN").item(0);
    
        if (symbElement != null) {
            String symb = symbElement.getTextContent();
            System.out.println("Symbol: " + symb);
        }
    
        if (childrenElement == null) {
            System.out.println("No children element found.");
            return;
        }
    
        NodeList children = childrenElement.getChildNodes();
        if (children.getLength() == 0) {
            System.out.println("No children of node");
            return;
        }
    
        // Traverse children
        List<String> childrenIDs = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                childrenIDs.add(childElement.getTextContent());
                System.out.println("Child ID: " + childElement.getTextContent());
            }
        }
    
        for (String childID : childrenIDs) {
            Element child = (Element) getNodeByUNID(childID);
            if (child != null) {
                System.out.println("Traversing Child: " + child.getElementsByTagName("UNID").item(0).getTextContent());
                traverseTree(child);
            }
        }
    }
    
    

    public Node getNodeByUNID(String unid) {
        // Search in inner nodes
        for (int i = 0; i < innerNodes.getLength(); i++) {
            Element node = (Element) innerNodes.item(i);
            if (unid.equals(node.getElementsByTagName("UNID").item(0).getTextContent())) {
                return node;
            }
        }

        // Search in leaf nodes
        for (int i = 0; i < leafNodes.getLength(); i++) {
            Element node = (Element) leafNodes.item(i);  // Fixed from innerNodes to leafNodes
            if (unid.equals(node.getElementsByTagName("UNID").item(0).getTextContent())) {
                return node;
            }
        }

        return null;  // Return null if no matching node is found
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
